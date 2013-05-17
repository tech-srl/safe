/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.properties;

import com.ibm.safe.properties.PropertiesManager.IPropertyDescriptor;
import com.ibm.safe.properties.PropertiesManager.Type;
import com.ibm.wala.util.debug.Assertions;

/**
 * Properties which are common to all SAFE configurations
 * 
 * @author sjfink
 * @author yahave
 */
public class CommonProperties {

  public static enum Props implements IPropertyDescriptor {
    ANALYZE_DEPENDENT_JARS("analyze_dependent_jars", Type.BOOLEAN, "Specifies if we should analyze dependent jars in a WAR file."), AUTO_SEARCH_IN_DIRS(
        "auto_search_in_dirs", Type.STRING,
        "Specifies a list of directories SAFE will walk through in depth to collect all .class, .jar, .ear, .war and .rar."), CLOSE_WORLD(
        "close_world", Type.BOOLEAN, "Specifies that we consider EJBs (local or home) as entry points."), COLLECT_STATISTICS(
        "collect_statistics", Type.BOOLEAN, "Specifies to collect different statistics during analyses."), DOMO_REPORT(
        "domo_report", Type.STRING, "Specifies that you want to create DOMO trace file (default file name in properties file)."), DOT_EXE(
        "dot_exe", Type.PATH, "Identifies the path where DOT executable file is installed."), 
        GHOSTVIEW_EXE("ghostview_exe", Type.PATH, "Identifies the path where ghostview executable file is installed."),
        PDFVIEW_EXE("pdfview_exe", Type.PATH, "Identifies the path where PDF viewer file is installed."),
        J2SE_DIR("java_runtime_dir", Type.PATH,
        "Identifies the path where the Java Runtime environment is installed."), J2EE("j2ee", Type.BOOLEAN,
        "Should be set to true if we analyze J2EE code."), J2EE_DIR("j2ee_runtime_dir", Type.PATH,
        "Identifies the path where the J2EE Runtime environment is installed."), MAIN_CLASSES("main_classes", Type.STRING,
        "Specifies a list of main classes to choose as entry poinst for the analysis."), MAX_FINDINGS_PER_RULE(
        "max_findings_per_rule", Type.INT, 50, "Specifies the maximum number of findings per rule."), MINE_DFA("mine_dfa",
        Type.BOOLEAN, ""), MODULES("modules", Type.STRING, "Identifies the modules to analyze by SAFE."), MODULES_DIRS(
        "modules_dirs", Type.PATH,
        "Specifies a list of directories where modules can be loaded (otherwise use current class loader)."), NULLDEREF(
        "nullderef", Type.BOOLEAN, "Activates or not null-dereference analysis on code transmitted."), PERFORMANCE_TRACKING(
        "performance_tracking", Type.BOOLEAN,
        "Specifies that some time and memory tracking for graphs building and analysis should be done."), PROJECT("project",
        Type.STRING, "Specifies an Eclipse Java project name to analyze (see 'workspace' option)."), RESULT_FILENAME(
        "result_filename", Type.STRING, "analysis_results.xml",
        "Specifies XML file name where to dump analysis result (default file name in properties file)."), RULES_DIRS("rules_dirs",
        Type.PATH, "nl/en/rules", "Specifies the root directories where SAFE rules are located."), OUTPUT_DIR("output_dir",
        Type.PATH, "results", "Specifies directory where all generated files without absolute path will be located."), SHORT_PROGRAM_NAME(
        "short_program_name", Type.STRING, ""), STRUCTURAL("structural", Type.BOOLEAN,
        "Activates or not structural analysis on code transmitted."), TIMEOUT_SECS("timeout_secs", Type.INT, 60,
        "Specifies a time after which current typestate solver is cancelled if analysis produces nothing."), TYPESTATE("typestate",
        Type.BOOLEAN, "Activates or not typestate analysis on code transmitted."), VERBOSE("verbose", Type.BOOLEAN,
        "Specifies verbose mode for SAFE analysis."), WORKSPACE("workspace", Type.PATH,
        "Specifies the workspace directory where we could find Eclipse Java projects."), NO_EXCLUSIONS("no_exclusions",
        Type.BOOLEAN, false, "avoid SAFE exclusions."), EXCLUSION_FILE("exclusion_file", Type.STRING, null,
        "specific exclusion file");

    private final String name;

    private final boolean cmdLine;

    private final Type type;

    private final String defaultString;

    private final String description;

    Props(final String name, final Type type, final String defaultString, final String description) {
      this.name = name;
      this.type = type;
      this.cmdLine = true;
      this.defaultString = defaultString;
      this.description = description;
    }

    Props(final String name, final Type type, final int defaultInt, final String description) {
      this(name, type, String.valueOf(defaultInt), description);
    }

    Props(final String name, final Type type, final boolean defaultInt, final String description) {
      this(name, type, String.valueOf(defaultInt), description);
    }

    Props(final String name, final Type type, final String description) {
      this.name = name;
      this.type = type;
      this.cmdLine = true;
      this.description = description;
      switch (type) {
      case BOOLEAN:
        this.defaultString = "false";
        break;
      case INT:
      case STRING:
      case PATH:
        this.defaultString = null;
        break;
      default:
        Assertions.UNREACHABLE();
        this.defaultString = null;
      }
    }

    public String getName() {
      return name;
    }

    public boolean isCommandLineOption() {
      return cmdLine;
    }

    public Type getType() {
      return type;
    }

    public String getDefaultAsString() {
      return defaultString;
    }

    public String getDescription() {
      return this.description;
    }
  }

  public static void register() {
    PropertiesManager.registerProperties(CommonProperties.Props.values());
  }

}
