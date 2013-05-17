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
package com.ibm.safe.core.tests;

import java.util.Collection;
import java.util.Stack;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.ibm.safe.controller.AbstractSafeController;
import com.ibm.safe.controller.AbstractSafeJavaApplication;
import com.ibm.safe.controller.GenericSafeController;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.reporting.StandardOutputReporter;
import com.ibm.safe.internal.runners.AbstractSolverRunner.AnalysisKind;
import com.ibm.safe.internal.runners.AbstractSolverRunner.AnalysisStatus;
import com.ibm.safe.metrics.IMetrics;
import com.ibm.safe.metrics.ProgramStatistics;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.reporting.CompositeReporter;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.rules.IRule;
import com.ibm.wala.classLoader.IClass;

/**
 * Common class for all SAFE test cases that want to test SAFE engines
 * functionalities.
 * 
 * @author egeay
 * @author yahave
 * @author sfink
 */
public class SafeRegressionDriver {

  /**
   * Run a described regression test.
   * 
   * @param test
   * @throws SafeException
   *             if there's some problem
   * @throws Exception
   *             if there's some problem
   */
  public static int run(SafeRegressionUnit test) throws SafeException, Exception {

    return run(test, null);
  }

  /**
   * Run a described regression test.
   * 
   * @param test
   * @throws SafeException
   *             if there's some problem
   * @throws Exception
   *             if there's some problem
   */
  public static int run(SafeRegressionUnit test, SafeMessageCheck check) throws SafeException, Exception {
    initVMOptions();
    final PropertiesManager propManager = PropertiesManager.initFromMap(test.getOptions());

    final AbstractSafeController controller = new GenericSafeController(propManager);
    final IRule[] rules = controller.getRules();

    final CompositeReporter reporter = new CompositeReporter();
    final TestOrientedReporter r = new TestOrientedReporter();
    reporter.addReporter(r);
    reporter.addReporter(new StandardOutputReporter());
    controller.execute(rules, reporter, new NullProgressMonitor());
    boolean shouldCompare = test.getExpectedNumberOfFindings() >= 0;
    if (shouldCompare && r.nFindings != test.getExpectedNumberOfFindings()) {
      throw new IncorrectNumberOfFindings(test.getExpectedNumberOfFindings(), r.nFindings);
    }
    if (check != null) {
      check.check(r.messages);
    }
    if (r.status != AnalysisStatus.NORMAL) {
      throw new SafeException("Abnormal Termination of Analysis " + r.status);
    }
    return r.nFindings;
  }

  // --- Private code

  private static void initVMOptions() {
    SafeRegressionDriver.class.getClassLoader().setDefaultAssertionStatus(true);
    final String loggingConfigFile = System.getProperty(LOGGING_CONFIG_FILE);
    if (loggingConfigFile == null) {
      System.setProperty(LOGGING_CONFIG_FILE, AbstractSafeJavaApplication.class.getClassLoader().getResource(SAFE_LOG_FILE)
          .getFile());
    }
  }

  /**
   * A utility class to track results of regression tests as they run.
   */
  private final static class TestOrientedReporter implements IReporter {

    private Collection<Message> messages = new Stack<Message>();

    private int nFindings;

    private AnalysisStatus status;

    // --- Interface methods implementation
    public void process(final IClass clazz) {
      // Do nothing here !
    }

    public void produceFinalReport() throws Exception {
      // Do nothing here !
    }

    public void reportException(final Throwable exception) {
      exception.printStackTrace();
    }

    public void reportMessage(final Message message) {
      this.messages.add(message);
    }

    public void reportNumberOfFindings(final int numberOfFindings) {
      this.nFindings = numberOfFindings;
    }

    public void reportNumberOfRulesActivated(final int numberOfRules) {
      // Do nothing here !
    }

    public void reportPerformanceTracking(final PerformanceTracker perfoTracker) {
      // Do nothing here !
    }

    public void reportStatistics(final ProgramStatistics programStat) {
      // Do nothing here !
    }

    public void reportStatistics(final IMetrics typeStateMetrics) {
      // Do nothing here !
    }

    public void reportRuleLoading(final IRule rule) {
      // Do nothing here !
    }

    public void reportRuleInstances(final IRule rule, int instances) {
      // Do nothing here !
    }

    public void startAnalysis(final AnalysisKind nature) {
      // Do nothing here !
    }

    public void stopAnalysis(final AnalysisKind nature) {
      // Do nothing here !
    }

    public void reportAnalysisStatus(final AnalysisStatus status) {
      this.status = status;
    }

    public void version(final String versionNumber) {
      // Do nothing here !
    }
  }

  private static final String LOGGING_CONFIG_FILE = "java.util.logging.config.file"; //$NON-NLS-1$

  private static final String SAFE_LOG_FILE = "safelog.properties"; //$NON-NLS-1$

}
