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
package com.ibm.safe.lightweight.controller;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ibm.safe.controller.AbstractSafeController;
import com.ibm.safe.controller.BasicResultObserver;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.internal.runners.CompositeSolverRunner;
import com.ibm.safe.lightweight.options.LightWeightOptions;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.perf.PerformanceTracker.Kind;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.properties.CommonProperties.Props;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.rules.StructuralRule;
import com.ibm.safe.utils.SafeLogger;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.util.CancelException;

/**
 * Still a lot of redundancy with the various controllers.
 * 
 * @author egeay
 * @author yahave
 */
public class LightweightController extends AbstractSafeController {

  public static final String NO_SOLVER_OPTIONS = "No solver options have been identified.\nAt least one structural or typestate option and rule should be activated.";

  public LightweightController(final PropertiesManager thePropertiesManager) {
    super(thePropertiesManager);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.controller.ISafeController#execute(com.ibm.safe.emf.rules.IRule[],
   *      com.ibm.safe.reporting.IReporter,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  public void execute(final IRule[] rules, final IReporter reporter, final IProgressMonitor monitor) throws SafeException,
      CancelException, IllegalArgumentException, CoreException {
    if ((rules == null) || (rules.length == 0)) {
      throw new SetUpException("No SAFE rules have been provided."); //$NON-NLS-1$
    }

    final CompositeSolverRunner solverRunner = new CompositeSolverRunner();

    final PerformanceTracker perfoTracker = (shouldUsePerfomanceTracker()) ? new PerformanceTracker(
        "Safe engine tracking report\n", Kind.GLOBAL) : //$NON-NLS-1$
        null;

    final LightweightRulesManager lwRulesManager = new LightweightRulesManager(rules);
    lwRulesManager.applyFilters(this.propertiesManager);

    final StructuralRule[] structuralRules = lwRulesManager.getStructuralRules();

    reportStructuralRulesActivated(reporter, structuralRules);

    if (hasStructuralOptionsActivated() && (structuralRules.length > 0)) {
      LightWeightOptions lwo = new LightWeightOptions(this.propertiesManager);
      solverRunner.addSolverRunner(new StructuralSolverRunner(lwo, structuralRules, perfoTracker, reporter));
    }

    if (solverRunner.isEmpty()) {
      throw new SetUpException(NO_SOLVER_OPTIONS); //$NON-NLS-1$
    }
    execute(solverRunner, perfoTracker, reporter, reporter, monitor);
  }

  // --- Private code

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
      }

      try {
        realReporter.produceFinalReport();
      } catch (Exception except) {
        except.printStackTrace();
        SafeLogger.severe("Unable to create XML report.", except); //$NON-NLS-1$
      }
    }
  }

  /**
   * @param reporter
   * @param structuralRules
   * @param typeStateRules
   * @throws SetUpException
   */
  protected void reportStructuralRulesActivated(final IReporter reporter, final StructuralRule[] structuralRules)
      throws SetUpException {
    if (structuralRules.length == 0) {
      throw new SetUpException("No SAFE rules have been activated."); //$NON-NLS-1$
    }
    reportRulesActivated(reporter, structuralRules);
  }

  /**
   * @param reporter
   * @param rules
   */
  protected void reportRulesActivated(final IReporter reporter, final IRule[] rules) {
    for (int i = 0; i < rules.length; i++) {
      reporter.reportRuleLoading(rules[i]);
    }
    if (rules.length > 0) {
      reporter.reportNumberOfRulesActivated(rules.length);
    }
  }

  protected static final String VERSION_PROPERTIES_FILE = "com/ibm/safe/version.properties"; //$NON-NLS-1$

}
