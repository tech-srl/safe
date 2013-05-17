package com.ibm.safe.ant;

import java.util.HashMap;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.ibm.safe.controller.AbstractSafeController;
import com.ibm.safe.controller.GenericSafeController;
import com.ibm.safe.internal.reporting.ReporterFactory;
import com.ibm.safe.lightweight.options.LightweightProperties;
import com.ibm.safe.options.WholeProgramProperties;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.typestate.options.TypestateProperties;

/**
 * Implementation of an Ant Task dedicated to run SAFE analysis. The set of
 * options are the same than command line mode. Excepted that boolean options
 * have to be explicitly initialized to true to be activated.
 * 
 * @author egeay
 */
public final class SafeAntTask extends Task {

  // --- Overridden methods

  /**
   * Method called by Ant framework in order to execute this particular task.
   */
  public void execute() {
    try {
      final PropertiesManager propertiesManager = PropertiesManager.initFromMap(this.options);
      this.options = null;

      final String analysisResultFile = propertiesManager.getStringValue(CommonProperties.Props.RESULT_FILENAME);

      final AbstractSafeController controller = createController(propertiesManager);
      controller.execute(controller.getRules(), ReporterFactory.createXMLAndConsoleReporter(analysisResultFile),
          new NullProgressMonitor());
    } catch (Throwable except) {
      except.printStackTrace();
      throw new BuildException(except.getMessage());
    }
  }

  protected AbstractSafeController createController(final PropertiesManager properties) {
    return new GenericSafeController(properties);
  }

  // --- Task options

  /**
   * Specifies if in the case of an Archive file dependent jars should be
   * analyzed.
   */
  public void setAnalyzeDependentJars(final boolean shouldAnalyzeDependentJars) {
    if (shouldAnalyzeDependentJars) {
      this.options.put(CommonProperties.Props.ANALYZE_DEPENDENT_JARS.getName(), this.activatedOption.toString());
    }
  }

  /**
   * Specifies the directories where SAFE will walk through to collect byte code
   * to analyze.
   */
  public void setAuto_Search_In_Dirs(final String searchingDirectories) {
    this.options.put(CommonProperties.Props.AUTO_SEARCH_IN_DIRS.getName(), searchingDirectories);
  }

  /**
   * Specifies if analysis for J2EE modules is close-world or not.
   */
  public void setCloseWorldAnalysis(final boolean isCloseWorldAnalysis) {
    if (isCloseWorldAnalysis) {
      this.options.put(CommonProperties.Props.CLOSE_WORLD.getName(), this.activatedOption.toString());
    }
  }

  /**
   * Specifies if end-user wants to create DOMO report. File name is in Main
   * SAFE properties file.
   * 
   * @param domoReport
   *            Value of this string can be either a boolean value, either a
   *            valid file name.
   */
  public void setDomo_Report(final String domoReport) {
    final Boolean bool = getValueOf(domoReport);
    if (bool == null) {
      this.options.put(CommonProperties.Props.DOMO_REPORT.toString(), domoReport);
    } else if (bool.booleanValue()) {
      this.options.put(CommonProperties.Props.DOMO_REPORT.toString(), this.activatedOption.toString());
    }
  }

  /**
   * Specifies if end-user wants to dump XML files for ASTs parsed during
   * structural analysis.
   * 
   * @param dumpXMLDir
   *            Value of this string can be either a boolean value, either a
   *            valid directory.
   */
  public void setDumpXML(final String dumpXMLDir) {
    final Boolean bool = getValueOf(dumpXMLDir);
    if (bool == null) {
      this.options.put(LightweightProperties.Props.DUMP_XML_DIRECTORY.toString(), dumpXMLDir);
    } else if (bool.booleanValue()) {
      this.options.put(LightweightProperties.Props.DUMP_XML_DIRECTORY.toString(), this.activatedOption.toString());
    }
  }

  /**
   * Specifies a string containing globally a sequence of main classes used as
   * entry points for J2SE code analysis.
   */
  public void setMain_Classes(final String mainClasses) {
    this.options.put(CommonProperties.Props.MAIN_CLASSES.toString(), mainClasses);
  }

  /**
   * Specifies if end-user wants all classes as entry points for J2SE code
   * analysis.
   */
  public void setMakeAllMainClassesEntryPoint(final boolean makeAllMainClassesEntryPoints) {
    if (makeAllMainClassesEntryPoints) {
      this.options.put(WholeProgramProperties.Props.ALL_MAIN_CLASSES_ENTRY_POINT.toString(), this.activatedOption.toString());
    }
  }

  /**
   * Specifies the modules that end-user wants to analyze.
   */
  public void setModules(final String modules) {
    this.options.put(CommonProperties.Props.MODULES.toString(), modules);
  }

  /**
   * Specifies the directories where to load the modules.
   */
  public void setModules_Dirs(final String modulesDirectories) {
    this.options.put(CommonProperties.Props.MODULES_DIRS.toString(), modulesDirectories);
  }

  /**
   * Specifies the output directory where all generated files will be located by
   * default. This option is also specified in SAFE properties file.
   */
  public void setOutputDir(final String outputDir) {
    this.options.put(CommonProperties.Props.OUTPUT_DIR.toString(), outputDir);
  }

  /**
   * Specifies if end-user wants to dump points-to graphs in a dot file.
   * 
   * @param pointsToDotFileName
   *            Value of this string can be either a boolean value, either a
   *            valid file name.
   */
  public void setPointsTo_Graph(final String pointsToDotFileName) {
    final Boolean bool = getValueOf(pointsToDotFileName);
    if (bool == null) {
      this.options.put(WholeProgramProperties.Props.POINTS_TO_GRAPH.toString(), pointsToDotFileName);
    } else if (bool.booleanValue()) {
      this.options.put(WholeProgramProperties.Props.POINTS_TO_GRAPH.toString(), this.activatedOption.toString());
    }
  }

  /**
   * Specifies if end-user wants to dump typestate automaton in a dot file.
   * 
   * @param propertyGraphDotFileName
   *            Value of this string can be either a boolean value, either a
   *            valid file name.
   */
  public void setProperty_Graph(final String propertyGraphDotFileName) {
    final Boolean bool = getValueOf(propertyGraphDotFileName);
    if (bool == null) {
      this.options.put(TypestateProperties.Props.PROPERTY_GRAPH.toString(), propertyGraphDotFileName);
    } else if (bool.booleanValue()) {
      this.options.put(TypestateProperties.Props.PROPERTY_GRAPH.toString(), this.activatedOption.toString());
    }
  }

  /**
   * Specifies if yes or not end-user wants to report typestate call graph in
   * DOMO trace file.
   */
  public void setReport_Call_Graph(final boolean shouldReportTypeStateCallGraph) {
    if (shouldReportTypeStateCallGraph) {
      this.options.put(WholeProgramProperties.Props.REPORT_CALL_GRAPH.toString(), this.activatedOption.toString());
    }
  }

  /**
   * Specifies file name for XML result file.
   */
  public void setResult_Filename(final String filename) {
    this.options.put(CommonProperties.Props.RESULT_FILENAME.toString(), filename);
  }

  /**
   * Specifies rules directories where to load SAFE EMF rules.
   */
  public void setRules_Dirs(final String directories) {
    this.options.put(CommonProperties.Props.RULES_DIRS.toString(), directories);
  }

  /**
   * Specifies the regular expression or list of qualified names identifying the
   * classes to analyze for structural rules.
   */
  public void setSelect_Classes(final String pattern) {
    this.options.put(LightweightProperties.Props.SELECT_CLASSES.toString(), pattern);
  }

  /**
   * Notifies to the set of existing options the name of solver he wants to use
   * for typestate analysis.
   */
  public void setSolver_Kind(final String solverKind) {
    this.options.put(TypestateProperties.Props.TYPESTATE_SOLVER_KIND.toString(), solverKind);
  }

  /**
   * sets the maximal path length for must accesspath solvers.
   * 
   * @param limit
   */
  public void setMust_ap_klimit(final String limit) {
    this.options.put(TypestateProperties.Props.MUST_AP_KLIMIT.toString(), limit);
  }

  /**
   * Specifies if end-user wants to dump statistics results during structural
   * analysis.
   * 
   * @param statisticsFileName
   *            Value of this string can be either a boolean value, either a
   *            valid file name.
   */
  public void setStatistics(final String statisticsFileName) {
    final Boolean bool = getValueOf(statisticsFileName);
    if (bool == null) {
      this.options.put(CommonProperties.Props.COLLECT_STATISTICS.toString(), statisticsFileName);
    } else if (bool.booleanValue()) {
      this.options.put(CommonProperties.Props.COLLECT_STATISTICS.toString(), this.activatedOption.toString());
    }
  }

  /**
   * Specifies if end-user wants structural analysis activated.
   */
  public void setStructural(final boolean shouldActivateStructural) {
    if (shouldActivateStructural) {
      this.options.put(CommonProperties.Props.STRUCTURAL.toString(), this.activatedOption.toString());
    }
  }

  /**
   * Specifies the structural rules end-user wants especially load.
   */
  public void setStructural_Rules(final String structuralRules) {
    this.options.put(LightweightProperties.Props.SELECT_STRUCTURAL_RULES.toString(), structuralRules);
  }

  /**
   * Specifies if end-user wants to dump some time and memory tracking results
   * for all analysis nature.
   */
  public void setPerformance_Tracking(final boolean shouldUsePerformanceTracking) {
    if (shouldUsePerformanceTracking) {
      this.options.put(CommonProperties.Props.PERFORMANCE_TRACKING.toString(), this.activatedOption.toString());
    }
  }

  /**
   * Specifies the timeout for limit of processing time of each rule.
   */
  public void setTimeout_Secs(final String seconds) {
    this.options.put(CommonProperties.Props.TIMEOUT_SECS.toString(), seconds);
  }

  /**
   * Specifies if end-user wants typestate analysis activated.
   */
  public void setTypeState(final boolean shouldActivateTypeState) {
    if (shouldActivateTypeState) {
      this.options.put(CommonProperties.Props.TYPESTATE.toString(), this.activatedOption.toString());
    }
  }

  /**
   * Specifies the call graph kind to build for typestate analysis.
   */
  public void setTypeState_CG_Kind(final String cgKind) {
    this.options.put(WholeProgramProperties.Props.CG_KIND.toString(), cgKind);
  }

  /**
   * Specifies the typestate rules end-user wants especially load.
   */
  public void setTypeState_Rules(final String typeStateRules) {
    this.options.put(TypestateProperties.Props.SELECT_TYPESTATE_RULES.toString(), typeStateRules);
  }

  /**
   * Specifies if end-user wants verbose mode.
   */
  public void setVerbose(final boolean isVerbose) {
    if (isVerbose) {
      this.options.put(CommonProperties.Props.VERBOSE.toString(), this.activatedOption.toString());
    }
  }

  // --- Private code

  private Boolean getValueOf(final String strValue) {
    return ((strValue.compareToIgnoreCase("true") == 0) || //$NON-NLS-1$ 
    (strValue.compareToIgnoreCase("false") == 0)) ? //$NON-NLS-1$ 
    Boolean.valueOf(strValue)
        : null;
  }

  private Map<String, String> options = new HashMap<String, String>(15);

  private final Boolean activatedOption = Boolean.TRUE;

}
