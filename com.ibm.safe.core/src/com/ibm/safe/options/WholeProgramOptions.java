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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;

import com.ibm.safe.callgraph.CallGraphEngine.CallGraphKind;
import com.ibm.safe.controller.RulesManager;
import com.ibm.safe.internal.entrypoints.EntryPointDefinition;
import com.ibm.safe.internal.entrypoints.MainClassesEntryPointsReader;
import com.ibm.safe.internal.entrypoints.StringEntryPointsReader;
import com.ibm.safe.internal.entrypoints.XMLEntryPointsReader;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.utils.SafeHome;

/**
 * @author egeay
 * @author yahave
 */
public class WholeProgramOptions extends CommonOptions {

  public WholeProgramOptions(WholeProgramOptions wpo) {
    super(wpo);
  }

  public WholeProgramOptions(PropertiesManager propertiesManager) {
    super(propertiesManager);
  }

  public boolean isContradictionAnalysis() throws PropertiesException {
    return getBooleanValue(WholeProgramProperties.Props.CONTRADICTION_ANALYSIS);
  }

  public CallGraphKind getCallGraphKind() throws PropertiesException {
    final String cgKindName = getStringValue(WholeProgramProperties.Props.CG_KIND);

    final CallGraphKind cgKind = Enum.valueOf(CallGraphKind.class, cgKindName);
    if (cgKind == null) {
      throw new PropertiesException("Bad call graph kind " + cgKindName);
    }
    return cgKind;
  }

  public String getPointsToDotFile() throws PropertiesException {
    return getStringValue(WholeProgramProperties.Props.POINTS_TO_GRAPH);
  }

  public boolean shouldDumpCallGraph() throws PropertiesException {
    return getBooleanValue(WholeProgramProperties.Props.REPORT_CALL_GRAPH);
  }

  public boolean shouldCollectStatistics() throws PropertiesException {
    return getBooleanValue(CommonProperties.Props.COLLECT_STATISTICS);
  }

  public boolean shouldCreatePointsToDotFile() throws PropertiesException {
    return getPointsToDotFile() != null;
  }

  public boolean shouldSliceSupergraph() throws PropertiesException {
    return getBooleanValue(WholeProgramProperties.Props.SLICE_SUPERGRAPH);
  }

  public boolean shouldGenerateWitness() throws PropertiesException {
    return getBooleanValue(WholeProgramProperties.Props.GENERATE_WITNESS);
  }

  public boolean shouldUseLiveAnalysis() throws PropertiesException {
    return getBooleanValue(WholeProgramProperties.Props.LIVE_ANALYSIS);
  }

  public final Pattern[] getMainClassesSelector() throws PropertiesException {
    final String option = getStringValue(WholeProgramProperties.Props.SELECT_MAIN_CLASSES);
    if (option == null) {
      return new Pattern[0];
    } else {
      final String[] regExpressions = getStringValue(WholeProgramProperties.Props.SELECT_MAIN_CLASSES).split(
          RulesManager.LIST_REGEX_SEPARATOR);
      final Pattern[] selector = new Pattern[regExpressions.length];
      for (int i = 0; i < selector.length; i++) {
        selector[i] = Pattern.compile(regExpressions[i]);
      }
      return selector;
    }
  }

  public EntryPointDefinition[] getEntryPointDefinitions() throws PropertiesException, SetUpException {
    final Collection<EntryPointDefinition> entryPoints = new ArrayList<EntryPointDefinition>(10);
    final String[] mainClasses = getMainClasses();
    final String entryPointsFile = getStringValue(WholeProgramProperties.Props.ENTRY_POINTS_FILE);
    final String entryPointsOption = getStringValue(WholeProgramProperties.Props.ENTRY_POINTS);

    if (mainClasses != null) {
      entryPoints.addAll(Arrays.asList(new MainClassesEntryPointsReader(mainClasses).getEntryPointDefinitions()));
    }

    if ((entryPointsFile != null) && (entryPointsOption != null)) {
      throw new PropertiesException("Options 'entry_points' and 'entry_points_file' can't be used together.");
    }

    if (entryPointsOption != null) {
      entryPoints.addAll(Arrays.asList(new StringEntryPointsReader(entryPointsOption).getEntryPointDefinitions()));
    } else if (entryPointsFile != null) {
      try {
        entryPoints.addAll(Arrays.asList(new XMLEntryPointsReader(getXMLFile(entryPointsFile)).getEntryPointDefinitions()));
      } catch (Exception except) {
        throw new PropertiesException("Unable to read valid entry points in " + entryPointsFile, except);
      }
    }

    System.out.println("Entry point definitions:");
    for (EntryPointDefinition entryPoint : entryPoints) {
      System.out.println(entryPoint);
    }
    return entryPoints.toArray(new EntryPointDefinition[entryPoints.size()]);
  }

  protected File getXMLFile(final String fileName) throws PropertiesException {
    File file = new File(fileName);
    if (!file.isAbsolute()) {
      final URL url = getClass().getClassLoader().getResource(fileName);
      if (url == null) {
        file = new File(SafeHome.getSafeHomeDir(getClass().getClassLoader()), fileName);
      } else {
        file = new File(url.getFile());
      }
    }
    if (!file.exists()) {
      throw new PropertiesException("Cannot find xml file " + file.getPath());
    }
    return file;
  }

  public boolean allMainClassesEntrypoints() throws PropertiesException {
    return getBooleanValue(WholeProgramProperties.Props.ALL_MAIN_CLASSES_ENTRY_POINT);
  }

  public final boolean isJ2SEOptionsActivated() throws PropertiesException {
    return ((getStringValue(CommonProperties.Props.MAIN_CLASSES) != null) || allMainClassesEntrypoints());
  }

}
