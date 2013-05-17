/*******************************************************************************
 * Copyright (c) 2004-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.options;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.ibm.safe.controller.RulesManager;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.runners.AbstractSolverRunner;
import com.ibm.safe.internal.runners.RuntimeDirWalkVisitor;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.properties.CommonProperties.Props;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.utils.DirectoryWalk;
import com.ibm.safe.utils.IDirectoryWalkVisitor;
import com.ibm.safe.utils.SafeHome;
import com.ibm.safe.utils.SafeLogger;
import com.ibm.wala.classLoader.ClassFileModule;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.Module;
import com.ibm.wala.ide.util.EclipseProjectPath;
import com.ibm.wala.ide.util.JavaEclipseProjectPath;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.io.FileProvider;

/**
 * @author egeay
 * @author Stephen Fink
 * @author Eran Yahav
 */
public class CommonOptions extends PropertiesManager {

  static final String CLASS_EXT = ".class"; //$NON-NLS-1$

  static final String JAR_EXT = ".jar"; //$NON-NLS-1$

  static final String EAR_EXT = ".ear"; //$NON-NLS-1$

  static final String WAR_EXT = ".war"; //$NON-NLS-1$

  static final String RAR_EXT = ".rar"; //$NON-NLS-1$

  private AnalysisScope scope;

  private FileProvider fp = new FileProvider();
  
  private static final String SAFE_EXCLUSIONS = "SafeClassHierarchyExclusions.txt";

  public CommonOptions(final PropertiesManager p) {
    super(p);
  }

  public AnalysisScope getOrCreateAnalysisScope() throws PropertiesException, IOException, CoreException {
    if (this.scope == null) {
      if (getStringValue(Props.PROJECT) != null) {
        this.scope = createEclipseAnalyseScope();

      } else {

        // TODO: Should we also include WALA's
        // J2SEClassHierarchyExclusions.xml/J2EEClassHierarchyExclusions.xml?
        String exclusionFile = isNoExclusion() ? null : ((specificExclusionFile() == null) ? SAFE_EXCLUSIONS
            : specificExclusionFile());

        // We could reuse a lot of code between the J2SE & J2EE versions
        // but the interfaces are a bit too different.
        if (getBooleanValue(Props.J2EE)) { // J2EE analysis
          // This call takes care of adding standard libraries.
          // TODO: Is CLOSE_WORLD (really CLOSED_WORLD) intended to
          // mean lifecycleEntrypoints?
          // //////////////////////////////////////
          // TODO: EY - ignore this for now, add later
          // this.scope =
          // J2EEAnalysisScope.make(getStandardLibraries(Props.J2SE_DIR),
          // getStandardLibraries(Props.J2SE_DIR),
          // exclusionFile, getClass().getClassLoader(),
          // getBooleanValue(Props.CLOSE_WORLD));
          // ////////////////////////////////////
          return null;
        } else { // J2SE analysis
          // TODO: Clean up by creating a make method similar to the
          // J2EEAnalysisScope's to avoid code duplication.
          this.scope = AnalysisScopeReader.makePrimordialScope(fp.getFile(exclusionFile));
          // (CommonOptions.BASIC_FILE, exclusionFile,
          // getClass().getClassLoader());

          for (final Module jarFileModule : getStandardLibraries(Props.J2SE_DIR)) {
            this.scope.addToScope(ClassLoaderReference.Primordial, jarFileModule);
          }
        }

        // Now, for J2SE or J2EE analysis, add the actual application
        // modules.
        final Collection<Module> appModules = collectModules(getAppropriateClassLoader(), getModules(), getAutoSearchDirectories());
        for (final Module module : appModules) {
          this.scope.addToScope(ClassLoaderReference.Application, module);
        }
      }
    }
    return this.scope;
  }

  private final boolean isNoExclusion() throws PropertiesException {
    return getBooleanValue(CommonProperties.Props.NO_EXCLUSIONS);
  }

  private final String specificExclusionFile() throws PropertiesException {
    return getStringValue(CommonProperties.Props.EXCLUSION_FILE);
  }

  public final boolean isVerboseMode() throws PropertiesException {
    return getBooleanValue(CommonProperties.Props.VERBOSE);
  }

  public final boolean shouldUsePerfomanceTracker() throws PropertiesException {
    return getBooleanValue(CommonProperties.Props.PERFORMANCE_TRACKING);
  }

  public int getMaxFindingsPerRule() throws PropertiesException {
    return getIntValue(CommonProperties.Props.MAX_FINDINGS_PER_RULE);
  }

  public String getShortProgramName() throws PropertiesException {
    return getStringValue(CommonProperties.Props.SHORT_PROGRAM_NAME);
  }

  public boolean analyzeDependentJars() throws PropertiesException {
    return getBooleanValue(CommonProperties.Props.ANALYZE_DEPENDENT_JARS);
  }

  public boolean isClosedWorld() throws PropertiesException {
    return getBooleanValue(CommonProperties.Props.CLOSE_WORLD);
  }

  public boolean isJ2EEOptionsActivated() throws PropertiesException {
    return (isClosedWorld() || analyzeDependentJars());
  }

  public String[] getMainClasses() throws PropertiesException {
    final String mainClassesOption = getStringValue(CommonProperties.Props.MAIN_CLASSES);
    if (mainClassesOption == null)
      return null;

    final String[] mainClasses = mainClassesOption.split(RulesManager.LIST_REGEX_SEPARATOR);
    if (mainClasses.length == 0) {
      SafeLogger.severe("Unable to identify main Java classes with 'main_classes' option.");
    }
    return mainClasses;
  }

  public final String[] getModules() throws PropertiesException {
    final String modules = getStringValue(Props.MODULES);
    if ((modules == null) && (getStringValue(Props.AUTO_SEARCH_IN_DIRS) == null)) {
      throw new PropertiesException("At least one of '-modules' and '-auto_search_in_dirs' options has to be used.");
    }
    return (modules == null) ? new String[0] : modules.split(RulesManager.LIST_REGEX_SEPARATOR);
  }

  // --- Private code

  private AnalysisScope createEclipseAnalyseScope() throws PropertiesException, IOException, CoreException {
    final String projectName = getStringValue(Props.PROJECT);
    if (projectName == null) {
      throw new InvalidParameterException("'project' option should be initialized/provided when you use 'workspace' option.");
    }
    final IJavaProject project = getJavaProject(projectName);
    //final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final JavaEclipseProjectPath epPath = JavaEclipseProjectPath.make(project,EclipseProjectPath.AnalysisScopeType.SOURCE_FOR_PROJ);

    return mergeProjectPaths(Collections.singleton(epPath));
  }

  private Collection<Module> collectModules(final ClassLoader classLoader, final String[] modules, final String[] searchingDirs)
      throws PropertiesException {
    final Collection<Module> allModules = new LinkedList<Module>();

    loadModulesSpecified(classLoader, modules, allModules, (searchingDirs.length > 0));
    searchModulesToLoad(classLoader, searchingDirs, allModules);

    if (allModules.isEmpty()) {
      throw new PropertiesException(
          "None of the elements listed from '-modules' parameter had been loaded. Please, check classpath.");
    }

    return allModules;
  }

  /**
   * create an analysis scope as the union of a bunch of EclipseProjectPath
   * 
   * @throws IOException
   */
  private AnalysisScope mergeProjectPaths(Collection<JavaEclipseProjectPath> projectPaths) throws IOException {
    AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();

    Collection<Module> seen = HashSetFactory.make();
    // to avoid duplicates, we first add all application modules, then
    // extension
    // modules, then primordial
    buildScope(ClassLoaderReference.Application, projectPaths, scope, seen);
    buildScope(ClassLoaderReference.Extension, projectPaths, scope, seen);
    buildScope(ClassLoaderReference.Primordial, projectPaths, scope, seen);
    return scope;
  }

  private void buildScope(ClassLoaderReference loader, Collection<JavaEclipseProjectPath> projectPaths, AnalysisScope scope,
      Collection<Module> seen) throws IOException {
    for (EclipseProjectPath path : projectPaths) {
      AnalysisScope pScope = path.toAnalysisScope();
      for (Module m : pScope.getModules(loader)) {
        if (!seen.contains(m)) {
          seen.add(m);
          scope.addToScope(loader, m);
        }
      }
    }
  }

  private ClassLoader getAppropriateClassLoader() throws PropertiesException {
    final ClassLoader defaultClassLoader = AbstractSolverRunner.class.getClassLoader();

    if (this.getPathValue(Props.MODULES_DIRS) != null) {
      final String modulesDirsOpt = getPathValue(Props.MODULES_DIRS);
      return getNewClassLoader(modulesDirsOpt.split(RulesManager.LIST_REGEX_SEPARATOR), defaultClassLoader);
    }
    return defaultClassLoader;
  }

  private String[] getAutoSearchDirectories() throws PropertiesException {
    final String searchingDirs = getStringValue(Props.AUTO_SEARCH_IN_DIRS);
    return (searchingDirs == null) ? new String[0] : searchingDirs.split(RulesManager.LIST_REGEX_SEPARATOR);
  }

  private File getFile(final ClassLoader classLoader, final String fileName) throws IOException {
    final URL url = classLoader.getResource(fileName);
    if (url != null) {
      return new File(url.getFile());
    } else {
      final File file = new File(fileName);
      if (file.isAbsolute()) {
        if (file.exists()) {
          return file;
        } else {
          throw new IOException("Module name given via absolute path " + fileName + " is wrong.");
        }
      } else {
        final File f = new File(System.getProperty("user.dir"), fileName);
        if (f.exists()) {
          return f;
        }
      }
    }
    throw new IOException("Unable to access via given classpath to " + fileName + " file.");
  }

  private IJavaProject getJavaProject(final String projectName) {
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IJavaProject javaProject = JavaCore.create(root.getProject(projectName));
    if (javaProject == null) {
      throw new InvalidParameterException("We could not find project '" + projectName + "' withing workspace provided.");
    } else {
      return javaProject;
    }
  }

  private ClassLoader getNewClassLoader(final String[] modulesDirs, final ClassLoader parentClassLoader) {
    final Collection<URL> urls = new ArrayList<URL>(modulesDirs.length);
    boolean oneValidURL = false;
    final String safeHomeDir = SafeHome.getSafeHomeDir(getClass().getClassLoader());
    for (int i = 0; i < modulesDirs.length; i++) {
      try {
        File dir = new File(modulesDirs[i]);
        if (!dir.isAbsolute()) {
          dir = new File(safeHomeDir, modulesDirs[i]);
        }
        if (!dir.exists()) {
          SafeLogger.severe("Directory " + modulesDirs[i] + " specified from -modules_dirs option does not exist.");
          continue;
        }
        if (!dir.isDirectory()) {
          SafeLogger.severe("Element " + modulesDirs[i] + "(from -modules_dirs option) doesn't represent a directory.");
          continue;
        }
        urls.add(dir.toURI().toURL());

        oneValidURL = true;
      } catch (MalformedURLException except) {
        SafeLogger.severe("Directory " + modulesDirs[i] + " can't be converted to a valid file URL (Internal Error).", except);
      }
    }
    return (oneValidURL) ? new URLClassLoader(urls.toArray(new URL[urls.size()]), parentClassLoader) : parentClassLoader;
  }

  private Module[] getStandardLibraries(final IPropertyDescriptor d) throws PropertiesException {
    final String directory = getPathValue(d);
    if (directory == null) {
      throw new PropertiesException("Reading of " + d.getName()
          + " property failed. Value must be set for this variable in properties file.");
    }
    final File dirFile = new File(directory);
    if (!dirFile.exists()) {
      throw new PropertiesException("Directory " + dirFile.getPath() + " specified for " + d.getName()
          + " property does not exist.");
    }

    final Collection<Module> jarFiles = new Stack<Module>();
    DirectoryWalk.walk(dirFile, new JarFileFilter(), new RuntimeDirWalkVisitor(jarFiles));
    if (jarFiles.size() == 0) {
      SafeLogger.warning("No jars files detected from directory " + dirFile.getPath() + " for " + d.getName() + " property.");
    }

    return jarFiles.toArray(new Module[jarFiles.size()]);
  }

  private boolean isJ2EEModule(final String module) throws PropertiesException {
    boolean isJ2EECode = getBooleanValue(Props.J2EE);
    return (isJ2EECode || module.endsWith(EAR_EXT) || module.endsWith(WAR_EXT) || module.endsWith(RAR_EXT));
  }

  private void loadModulesSpecified(final ClassLoader classLoader, final String[] modules, final Collection<Module> allModules,
      final boolean autoSearchInDirs) throws PropertiesException {
    for (int i = 0; i < modules.length; i++) {
      if (isJ2EEModule(modules[i])) {
        // EY: TODO: handle this later
        return;
      } else if (modules[i].endsWith(CLASS_EXT)) {
        try {
          allModules.add(new ClassFileModule(getFile(classLoader, modules[i])));
        } catch (IOException except) {
          if (!autoSearchInDirs) {
            SafeLogger.severe("Unable to add " + modules[i] + " class file in modules from string given.", except);
          }
        } catch (InvalidClassFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      } else if (modules[i].endsWith(JAR_EXT)) {
        try {
          allModules.add(new JarFileModule(new JarFile(getFile(classLoader, modules[i]))));
        } catch (IOException except) {
          if (!autoSearchInDirs) {
            SafeLogger.severe("Unable to add " + modules[i] + " jar file in modules from string given.", except);
          }
        }
      }
    }
  }

  private void searchModulesToLoad(final ClassLoader classLoader, final String[] searchingDirs, final Collection<Module> allModules) {
    final FileFilter byteCodeFilter = new ByteCodeFilter();
    final IDirectoryWalkVisitor walkVisitor = new DirectoryWalkVisitor(allModules);
    for (int i = 0; i < searchingDirs.length; i++) {
      try {
        final File dir = getFile(classLoader, searchingDirs[i]);
        DirectoryWalk.walk(dir, byteCodeFilter, walkVisitor);
      } catch (IOException except) {
        SafeLogger.severe("Unable to add searching directory " + searchingDirs[i] + " in the list of research.", except);
      }
    }
  }

  // --- Inner classes

  private static final class DirectoryWalkVisitor implements IDirectoryWalkVisitor {

    DirectoryWalkVisitor(final Collection<Module> theModules) {
      this.modules = theModules;
    }

    // --- Interface methods implementation

    public void visitDirectory(final File directory) {
      // Nothing to do !
    }

    public void visitFile(final File file) {

      if (file.getName().endsWith(CLASS_EXT)) {
        System.out.println(file.getName() + " found.");
        try {
			this.modules.add(new ClassFileModule(file));
		} catch (InvalidClassFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      } else if (file.getName().endsWith(JAR_EXT)) {
        try {
          this.modules.add(new JarFileModule(new JarFile(file)));
          System.out.println(file.getName() + " found.");
        } catch (IOException except) {
          SafeLogger.warning("Unable to add " + file.getName() + " jar file in modules from string given.", except);
        }
      } else {
        // TODO: EY handle this later
        return;
      }

    }

    // --- Private code

    private final Collection<Module> modules;

  }

  private static final class JarFileFilter implements FileFilter {

    // --- Interface method implementation

    public boolean accept(final File file) {
      return (file.isDirectory() || file.getName().endsWith(".jar")); //$NON-NLS-1$
    }

  }

}
