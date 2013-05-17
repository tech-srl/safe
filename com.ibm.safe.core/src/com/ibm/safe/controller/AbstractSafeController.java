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
package com.ibm.safe.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.internal.runners.CompositeSolverRunner;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.properties.CommonProperties.Props;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.rules.CommandLineRulesReader;
import com.ibm.safe.rules.DummyRule;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.rules.IRulesReader;
import com.ibm.safe.utils.SafeLogger;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.util.CancelException;

/**
 * @author egeay
 * 
 */
public abstract class AbstractSafeController {

  protected AbstractSafeController(final PropertiesManager thePropertiesManager) {
    this.propertiesManager = thePropertiesManager;
  }

  protected boolean isMining() throws PropertiesException {
    return propertiesManager.getBooleanValue(Props.MINE_DFA);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.controller.ISafeController#execute(com.ibm.safe.emf.rules.IRule[],
   *      com.ibm.safe.reporting.IReporter,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  public abstract void execute(final IRule[] rules, final IReporter reporter, final IProgressMonitor monitor) throws SafeException,
      CancelException, IllegalArgumentException, CoreException;

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.controller.ISafeController#getRules()
   */
  public IRule[] getRules() throws PropertiesException {
    return createRulesReader().getRules();
  }

  protected IRulesReader createRulesReader() throws PropertiesException {
    final String rulesDirsProperty = this.propertiesManager.getPathValue(Props.RULES_DIRS);
    assert rulesDirsProperty != null;
    final IRulesReader rulesReader = new CommandLineRulesReader(rulesDirsProperty
        .split(RulesManager.LIST_REGEX_SEPARATOR));
    try {
      rulesReader.load(getClass().getClassLoader());
    } catch (IOException except) {
      throw new PropertiesException("Unable to read rules in directory " + rulesDirsProperty, except);
    }

    return rulesReader;
  }

  protected void displaySafeVersion(final IReporter reporter) {

    final Properties versionProperties = new Properties();
    final InputStream inStream = getClass().getClassLoader().getResourceAsStream(VERSION_PROPERTIES_FILE);
    if (inStream != null) {
      try {
        versionProperties.load(inStream);
        final String version = versionProperties.getProperty("version");
        if (version != null) {
          reporter.version(version);
        } else {
          reporter.version("Unknown (Property 'version' in 'version.properties' is not set)");
        }
      } catch (IOException except) {
        reporter.version("Unknown (Properties loading of 'version.properties' failed)");
      }
    } else {
      reporter.version("Unknown (Reading of 'version.properties' failed)"); //$NON-NLS-1$ 
    }
  }

  protected void execute(final CompositeSolverRunner solverRunner, final PerformanceTracker perfoTracker,
      final IReporter originalReporter, final IReporter realReporter, final IProgressMonitor monitor) throws SafeException,
      CancelException, IllegalArgumentException, CoreException {
    try {
      if (shouldCreateDomoReport()) {
        Trace.setTraceFile(this.propertiesManager.getStringValue(Props.DOMO_REPORT));
      }
      if (isVerboseMode()) {
        displaySafeVersion(realReporter);
      }
      final BasicResultObserver resultObserver = new BasicResultObserver(realReporter);
      monitor.beginTask(null, 1);
      solverRunner.run(resultObserver, new SubProgressMonitor(monitor, 1));
      // Number of findings is always reported !
      originalReporter.reportNumberOfFindings(resultObserver.getNumberOfFindings());
    } catch (SafeException except) {
      realReporter.reportException(except);
      throw except;
    } finally {
      if (perfoTracker != null) {
        realReporter.reportPerformanceTracking(perfoTracker);
        if (shouldCreateDomoReport()) {
          Trace.print(perfoTracker.reportPerformanceTracking());
        }
      }

      try {
        realReporter.produceFinalReport();
      } catch (Exception except) {
        except.printStackTrace();
        SafeLogger.severe("Unable to create XML report.");
      }
    }
  }

  protected boolean hasStructuralOptionsActivated() throws PropertiesException {
    return this.propertiesManager.getBooleanValue(Props.STRUCTURAL);
  }

  protected boolean hasTypeStateOptionsActivated() throws PropertiesException {
    return this.propertiesManager.getBooleanValue(Props.TYPESTATE);
  }

  protected boolean hasNullDerefOptionsActivated() throws PropertiesException {
    return this.propertiesManager.getBooleanValue(Props.NULLDEREF);
  }

  protected boolean isVerboseMode() throws PropertiesException {
    return this.propertiesManager.getBooleanValue(Props.VERBOSE);
  }

  /**
   * @param reporter
   * @param structuralRules
   * @param typeStateRules
   * @throws SetUpException
   */
  protected void reportRulesActivated(final IReporter reporter, final IRule[] structuralRules,
      final IRule[] typeStateRules) throws PropertiesException, SetUpException {
    if (!isMining() && (structuralRules.length == 0) && (typeStateRules.length == 0)) {
      throw new SetUpException("No SAFE rules have been activated after the selection of the ones loaded.");
    }
    reportRulesActivated(reporter, structuralRules);
    reportRulesActivated(reporter, typeStateRules);
  }

  /**
   * @param reporter
   * @param rules
   * @throws PropertiesException
   */
  protected void reportRulesActivated(final IReporter reporter, final IRule[] rules) throws PropertiesException {
    if (isMining()) {
      // ugh. the reporting is fragile and barfs if there are no rules.
      // a hack to avoid this
      IRule miningRule = new DummyRule("mining");
      reporter.reportRuleLoading(miningRule);
      reporter.reportNumberOfRulesActivated(1);
    } else {
      for (int i = 0; i < rules.length; i++) {
        reporter.reportRuleLoading(rules[i]);
      }
      if (rules.length > 0) {
        reporter.reportNumberOfRulesActivated(rules.length);
      }
    }
  }

  protected boolean shouldCreateDomoReport() throws PropertiesException {
    return (this.propertiesManager.getStringValue(Props.DOMO_REPORT) != null);
  }

  protected boolean shouldUsePerfomanceTracker() throws PropertiesException {
    return this.propertiesManager.getBooleanValue(Props.PERFORMANCE_TRACKING);
  }

  protected static final String VERSION_PROPERTIES_FILE = "com/ibm/safe/version.properties"; //$NON-NLS-1$

  protected final PropertiesManager propertiesManager;

}
