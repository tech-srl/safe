/*******************************************************************************
 * Copyright (c) 2004, 2010-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.controller;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.internal.runners.CompositeSolverRunner;
import com.ibm.safe.lightweight.controller.LightweightRulesManager;
import com.ibm.safe.lightweight.controller.StructuralSolverRunner;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.perf.PerformanceTracker.Kind;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.rules.DummyRule;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.runners.ISolverRunner;
import com.ibm.safe.typestate.controller.TypeStateSolverRunner;
import com.ibm.safe.typestate.controller.TypestateRulesManager;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.options.TypestateProperties;
import com.ibm.safe.utils.SafeLogger;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.util.CancelException;

/**
 * @author egeay
 * @author yahave
 */
public class GenericSafeController extends AbstractSafeController {

  public static final String NO_SOLVER_OPTIONS = "No solver options have been identified.\nAt least one structural or typestate option and rule should be activated.";

  public GenericSafeController(final PropertiesManager thePropertiesManager) {
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
    if (!isMining() && ((rules == null) || (rules.length == 0))) {
      throw new SetUpException("No rules");
    }

    final CompositeSolverRunner solverRunner = new CompositeSolverRunner();

    final PerformanceTracker perfoTracker = (shouldUsePerfomanceTracker()) ? new PerformanceTracker(
        "Safe engine tracking report\n", Kind.GLOBAL) : //$NON-NLS-1$
        null;

    IRule[] typeStateRules = {};
    if (hasTypeStateOptionsActivated()) {
      final TypestateRulesManager typestateRulesManager = new TypestateRulesManager(rules);
      typestateRulesManager.applyFilters(this.propertiesManager);
      typeStateRules = typestateRulesManager.getTypeStateRules();
    }

    IRule[] structuralRules = {};
    if (hasStructuralOptionsActivated()) {
      final LightweightRulesManager lwRulesManager = new LightweightRulesManager(rules);
      lwRulesManager.applyFilters(this.propertiesManager);
      structuralRules = lwRulesManager.getStructuralRules();
    }

    reportRulesActivated(reporter, structuralRules, typeStateRules);

    if (hasStructuralOptionsActivated() && (structuralRules.length > 0)) {
      solverRunner.addSolverRunner(new StructuralSolverRunner(this.propertiesManager, structuralRules, perfoTracker, reporter));
    }
    if (hasTypeStateOptionsActivated()
        && (typeStateRules.length > 0 || this.propertiesManager.getBooleanValue(TypestateProperties.Props.MINE_DFA))) {
      // solverRunner.addSolverRunner(new
      // TypeStateSolverRunner(this.propertiesManager, typeStateRules,
      // perfoTracker, reporter));
      solverRunner.addSolverRunner(getTypeStateSolverRunner(this.propertiesManager, typeStateRules, perfoTracker, reporter));
    }
    if (solverRunner.isEmpty()) {
      throw new SetUpException(NO_SOLVER_OPTIONS);
    }
    execute(solverRunner, perfoTracker, reporter, reporter, monitor);
  }

  public ISolverRunner getTypeStateSolverRunner(PropertiesManager properties, IRule[] rules, PerformanceTracker perfoTracker,
      IReporter reporter) {
    TypeStateOptions tso = new TypeStateOptions(properties, rules);

    return new TypeStateSolverRunner(tso, rules, perfoTracker, reporter);
  }

  // --- Private code

  protected void execute(final CompositeSolverRunner solverRunner, final PerformanceTracker perfoTracker,
      final IReporter originalReporter, final IReporter realReporter, final IProgressMonitor monitor) throws SafeException,
      CancelException, IllegalArgumentException, CoreException {
    try {
      if (shouldCreateDomoReport()) {
        Trace.setTraceFile(this.propertiesManager.getStringValue(CommonProperties.Props.DOMO_REPORT));
      }
      if (isVerboseMode()) {
        displaySafeVersion(realReporter);
      }
      final BasicResultObserver resultObserver = new BasicResultObserver(realReporter);
      monitor.beginTask(null, 1);
      solverRunner.run(resultObserver, new SubProgressMonitor(monitor, 1));
      // Status and Number of findings are always reported !
      originalReporter.reportAnalysisStatus(resultObserver.getAnalysisStatus());
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
  protected void reportRulesActivated(final IReporter reporter, final IRule[] structuralRules, final IRule[] typeStateRules)
      throws PropertiesException {
    if (!isMining() && !hasNullDerefOptionsActivated() && (structuralRules.length == 0) && (typeStateRules.length == 0)) {
      throw new PropertiesException("No SAFE rules have been activated.");
    }
    if (hasStructuralOptionsActivated()) {
      reportStructuralRulesActivated(reporter, structuralRules);
    }
    if (hasTypeStateOptionsActivated()) {
      reportStructuralRulesActivated(reporter, typeStateRules);
    }
  }

  /**
   * @param reporter
   * @param rules
   * @throws PropertiesException
   */
  protected void reportStructuralRulesActivated(final IReporter reporter, final IRule[] rules) throws PropertiesException {
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
}
