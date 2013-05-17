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
package com.ibm.safe.reporting;

import com.ibm.safe.internal.runners.AbstractSolverRunner.AnalysisKind;
import com.ibm.safe.internal.runners.AbstractSolverRunner.AnalysisStatus;
import com.ibm.safe.metrics.IMetrics;
import com.ibm.safe.metrics.ProgramStatistics;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.rules.IRule;
import com.ibm.wala.classLoader.IClass;

/**
 * Implementations of this interface receives the following events coming from
 * SAFE analysis and have to save or report them in a way or another. Use
 * {@link com.ibm.safe.internal.reporting.ReporterFactory} to get instances of
 * this interface.
 * 
 * @author egeay
 */
public interface IReporter {

  /**
   * Analysis event specifying that next class is being analyzed.
   * 
   * @param clazz
   *            The class that is being analyzed by SAFE.
   */
  public void process(final IClass clazz);

  /**
   * Main method that is called once by SAFE framework to produce final report
   * once all the other event methods have been already called.
   * 
   * @throws Exception
   *             Occurs if during the phase of producing the report an I/O error
   *             happens.
   */
  public void produceFinalReport() throws Exception;

  /**
   * Analysis event notifying that an exception preventing analysis continuation
   * has been thrown.
   * 
   * @param exception
   *            The exception occurred during analysis.
   */
  public void reportException(final Throwable exception);

  /**
   * Analysis event specifying that next finding have been found.
   * 
   * @param message
   *            The message for the finding related to the rule considered.
   */
  public void reportMessage(final Message message);

  /**
   * Analysis event specifying the number of findings found during analysis.
   * 
   * @param numberOfFindings
   *            The number in question.
   */
  public void reportNumberOfFindings(final int numberOfFindings);

  /**
   * Analysis event indicating that <i>numberOfRules</i> have been activated
   * with the specific kind transmitted by parameter.
   */
  public void reportNumberOfRulesActivated(final int numberOfRules);

  /**
   * Analysis event indicating that the rule transmitted has been loaded.
   */
  public void reportRuleLoading(final IRule rule);

  /**
   * report the number of instances found for a given rule
   * 
   * @param rule
   * @param instances
   */
  public void reportRuleInstances(final IRule rule, int instances);

  /**
   * Analysis event that is called with appropriate instance <b>only if</b>
   * option to produce performance tracking has been selected.
   * 
   * @param perfoTracker
   *            The performance tracker used for report.
   */
  public void reportPerformanceTracking(final PerformanceTracker perfoTracker);

  /**
   * Analysis event that is called with appropriate instance <b>only if</b>
   * option to produce statistics has been selected.
   * 
   * @param programStat
   *            The appropriate statistics instance that contains the relevant
   *            results when structural analysis terminates.
   */
  public void reportStatistics(final ProgramStatistics programStat);

  /**
   * Analysis event that is called with appropriate instance <b>only if</b>
   * option to produce statistics has been selected.
   * 
   * @param typeStateMetrics
   *            Contains metrics data regarding to typestate analysis.
   */
  public void reportStatistics(final IMetrics typeStateMetrics);

  /**
   * Analysis event that simply specify that analysis has just started.
   * 
   * @param nature
   *            The nature of analysis that has just begun.
   * @see AnalysisNature
   */
  public void startAnalysis(final AnalysisKind nature);

  /**
   * Analysis event that simply specify that analysis has just stopped.
   * 
   * @param nature
   *            The nature of analysis that has just stopped.
   * @see AnalysisNature
   */
  public void stopAnalysis(final AnalysisKind nature);

  /**
   * Analysis event that reports how the analysis terminated.
   * 
   * @param status
   */
  public void reportAnalysisStatus(final AnalysisStatus status);

  /**
   * Reports SAFE version number.
   */
  public void version(final String versionNumber);

}
