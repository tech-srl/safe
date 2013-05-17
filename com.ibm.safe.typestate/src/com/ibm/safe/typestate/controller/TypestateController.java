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
package com.ibm.safe.typestate.controller;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ibm.safe.controller.AbstractSafeController;
import com.ibm.safe.controller.BasicResultObserver;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.internal.runners.CompositeSolverRunner;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.perf.PerformanceTracker.Kind;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.rules.TypestateRule;
import com.ibm.safe.runners.ISolverRunner;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.options.TypestateProperties;
import com.ibm.safe.utils.SafeLogger;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.util.CancelException;

/**
 * @author egeay
 * @author eyahav
 */
public class TypestateController extends AbstractSafeController {

  public TypestateController(final PropertiesManager properties) {
    super(properties);
  }

  protected boolean isMining() throws PropertiesException {
    return propertiesManager.getBooleanValue(TypestateProperties.Props.MINE_DFA);
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
      throw new SetUpException("No SAFE rules have been provided.");
    }

    final CompositeSolverRunner solverRunner = new CompositeSolverRunner();

    final PerformanceTracker perfoTracker = (shouldUsePerfomanceTracker()) ? new PerformanceTracker(
        "Safe engine tracking report\n", Kind.GLOBAL) : //$NON-NLS-1$
        null;

    final TypestateRulesManager rulesManager = new TypestateRulesManager(rules);
    rulesManager.applyFilters(this.propertiesManager);
    final TypestateRule[] typeStateRules = rulesManager.getTypeStateRules();
    reportRulesActivated(reporter, typeStateRules);

    if (hasTypeStateOptionsActivated()
        && (typeStateRules.length > 0 || this.propertiesManager.getBooleanValue(TypestateProperties.Props.MINE_DFA))) {
      TypeStateOptions tso = new TypeStateOptions(this.propertiesManager, typeStateRules);
      solverRunner.addSolverRunner(getTypeStateSolverRunner(tso, typeStateRules, perfoTracker, reporter));
    }
    if (solverRunner.isEmpty()) {
      throw new SetUpException(
          "No solver options have been identified.\nAt least one structural or typestate option and rule should be activated.");
    }
    execute(solverRunner, perfoTracker, reporter, reporter, monitor);
  }

  public ISolverRunner getTypeStateSolverRunner(TypeStateOptions typestateOptions, TypestateRule[] rules,
      PerformanceTracker perfoTracker, IReporter reporter) {
    return new TypeStateSolverRunner(typestateOptions, rules, perfoTracker, reporter);
  }

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

  /**
   * @param reporter
   * @param structuralRules
   * @param typeStateRules
   * @throws SetUpException
   * @throws PropertiesException
   */
  protected void reportRulesActivated(final IReporter reporter, final TypestateRule[] typeStateRules) throws SetUpException,
      PropertiesException {
    if (!isMining() && (typeStateRules.length == 0)) {
      throw new SetUpException("No SAFE rules have been activated after the selection of the ones loaded.");
    }
    reportRulesActivated(reporter, (IRule[]) typeStateRules);
  }

}
