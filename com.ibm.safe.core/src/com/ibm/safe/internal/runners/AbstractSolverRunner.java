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
package com.ibm.safe.internal.runners;

import java.io.IOException;
import java.util.Timer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ibm.safe.controller.ISafeSolver;
import com.ibm.safe.internal.exceptions.MaxFindingsException;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.internal.exceptions.SolverTimeoutException;
import com.ibm.safe.options.CommonOptions;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.perf.PerformanceTracker.Kind;
import com.ibm.safe.perf.PerformanceTracker.Stages;
import com.ibm.safe.perf.SolverPerfTracker;
import com.ibm.safe.properties.CommonProperties.Props;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.reporting.message.ISolverResult;
import com.ibm.safe.runners.IResultObserver;
import com.ibm.safe.runners.ISolverRunner;
import com.ibm.safe.utils.SafeLogger;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.debug.Assertions;

/**
 * Abstract base class that runs a safe solver
 */
public abstract class AbstractSolverRunner implements ISolverRunner {

	public static enum CodeKind {
		J2SE, J2EE, J2SE_J2EE_MIX
	}

	public static enum AnalysisKind {
		STRUCTURAL, NULLDEREF, TYPESTATE, SYMBOLIC
	}

	public static enum AnalysisStatus {
		INCOMPLETE, NORMAL, ABORT_TIMEOUT, ABORT_MAX_FIND
	}

	protected final PropertiesManager propertiesManager;

	private final PerformanceTracker perfoTracker;

	private final IReporter reporter;

	protected AbstractSolverRunner(
			final PropertiesManager thePropertiesManager,
			final PerformanceTracker performanceTracker,
			final IReporter safeReporter) {
		this.propertiesManager = thePropertiesManager;
		this.perfoTracker = performanceTracker;
		this.reporter = safeReporter;
	}

	protected final CommonOptions getCommonOptions() throws PropertiesException {
		return new CommonOptions(this.propertiesManager);
	}

	private int getTimeoutSecs() throws PropertiesException {
		return getManager().getIntValue(Props.TIMEOUT_SECS);
	}

	protected PropertiesManager getManager() {
		return this.propertiesManager;
	}

	protected final IReporter getReporter() {
		return this.reporter;
	}

	protected final CodeKind getModulesCodeKind(
			final CommonOptions commonOptions) throws PropertiesException,
			IOException, CoreException {
		if (this.propertiesManager.getBooleanValue(Props.J2EE)) {
			return CodeKind.J2EE;
		} else {
			CodeKind codeKind = null;
//			final List<Module> appModules = commonOptions
//					.getOrCreateAnalysisScope().getModules(
//							ClassLoaderReference.Application);
			// for (final Module module : appModules) {
			// if (module instanceof TopLevelArchiveModule) {
			// // It's a J2EE module.
			// if (codeKind == CodeKind.J2SE) {
			// codeKind = CodeKind.J2SE_J2EE_MIX;
			// break;
			// } else {
			// codeKind = CodeKind.J2EE;
			// }
			// } else {
			// if (codeKind == CodeKind.J2EE) {
			// codeKind = CodeKind.J2SE_J2EE_MIX;
			// break;
			// } else {
			// codeKind = CodeKind.J2SE;
			// }
			// }
			// }
			codeKind = CodeKind.J2SE;
			return codeKind;
		}
	}

	protected final PerformanceTracker getPerformanceTracker() {
		return this.perfoTracker;
	}

	protected final void runSolvers(final ISafeSolver[] solvers,
			final IResultObserver observer, final AnalysisKind analysisNature,
			final Stages timerName, final IProgressMonitor monitor)
			throws PropertiesException, SetUpException, CancelException {
		getReporter().startAnalysis(analysisNature);
		startTracking(timerName.toString());
		final SolverPerfTracker solverPerfoTracker = ((this.perfoTracker == null) || (solvers.length == 1)) ? null
				: new SolverPerfTracker(
						"Solvers tracking report\n", Kind.SOLVERS); //$NON-NLS-1$
		SafeTimeoutTask timeout = null;
		Timer timeoutTimer = null;
		try {
			monitor.beginTask(null, solvers.length);
			for (int i = 0; i < solvers.length; i++) {
				try {
					if (getTimeoutSecs() > 0) {
						/**
						 * call interrupted to clear interrupted status that may
						 * be carried-over from a previous (uncaught) interrupt.
						 * More specifically, if the interrupt occurs after we
						 * exist the solver internal loop, we will only catch it
						 * on the next solver. Note that the call the
						 * "interruped()" clears the interrupted flag.
						 */
						if (Thread.interrupted()) {
							Trace.print("*** Interrupt Carried Over ***");
						}
						/**
						 * Creates a deamon timer. Since this is a timeout timer
						 * we do not want it to keep the program running if its
						 * the only reamining thread.
						 */
						timeoutTimer = new Timer(true /* isDaemon */);
						timeout = new SafeTimeoutTask(Thread.currentThread());
						long timeoutMillis = 1000 * getTimeoutSecs();
						timeoutTimer.schedule(timeout, timeoutMillis);
					}

					if (monitor.isCanceled()) {
						break;
					}

					runSolver(solvers[i], solverPerfoTracker, observer,
							new SubProgressMonitor(monitor, 1));
				} catch (WalaException exception) {
					SafeLogger.severe(
							"Error occured during SAFE solver running.",
							exception);
				} catch (SolverTimeoutException exception) {
					if (solverPerfoTracker != null) {
						solverPerfoTracker.timeout(solvers[i].toString(),
								exception.getResult());
					}
					observer.notify(exception.getResult(),
							AnalysisStatus.ABORT_TIMEOUT);
					SafeLogger.warning("SAFE Solver timed out.");
				} finally {
					// cancel task between solvers.
					if (timeout != null) {
						timeout.cancel(); // cancel the task
					}
				}
			}
		} finally {
			monitor.done();
			stopTracking(timerName.toString());
			if (timeout != null) {
				timeout.cancel(); // cancel the task
			}
			if (timeoutTimer != null) {
				timeoutTimer.cancel(); // cancel the entire timer
			}

			getReporter().stopAnalysis(analysisNature);
			if (solverPerfoTracker != null) {
				getReporter().reportPerformanceTracking(solverPerfoTracker);
			}
		}
	}

	protected final boolean shouldCollectStatistics()
			throws PropertiesException {
		return this.propertiesManager.getBooleanValue(Props.COLLECT_STATISTICS);
	}

	protected final boolean shouldMineDFA() throws PropertiesException {
		return this.propertiesManager.getBooleanValue(Props.MINE_DFA);
	}

	/**
	 * @param solver
	 * @param solverPerfoTracker
	 * @param observer
	 * @param monitor
	 * @throws WalaException
	 * @throws SolverTimeoutException
	 * @throws PropertiesException
	 * @throws SetUpException
	 * @throws SetUpException
	 * @throws CancelException
	 */
	private void runSolver(final ISafeSolver solver,
			final SolverPerfTracker solverPerfoTracker,
			final IResultObserver observer, final IProgressMonitor monitor)
			throws WalaException, SolverTimeoutException, PropertiesException,
			SetUpException, CancelException {
		Assertions.productionAssertion(solver != null);
		if (solverPerfoTracker != null) {
			solverPerfoTracker.startTracking(solver.toString());
		}
		ISolverResult results = null;
		try {
			results = solver.perform(monitor);
			if (results != null) {
				observer.notify(results, AnalysisStatus.NORMAL);
			}
		} catch (MaxFindingsException e) {
			observer.notify(e.getResult(), AnalysisStatus.ABORT_MAX_FIND);
		} finally {
			if (solverPerfoTracker != null) {
				solverPerfoTracker.stopTracking(solver.toString(), results);
			}
		}
	}

	private void startTracking(final String timerName) {
		if (this.perfoTracker != null) {
			this.perfoTracker.startTracking(timerName);
		}
	}

	private void stopTracking(final String timerName) {
		if (this.perfoTracker != null) {
			this.perfoTracker.stopTracking(timerName);
		}
	}

}
