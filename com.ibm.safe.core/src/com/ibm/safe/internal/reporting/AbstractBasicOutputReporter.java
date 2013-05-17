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
package com.ibm.safe.internal.reporting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

import com.ibm.safe.internal.runners.AbstractSolverRunner.AnalysisKind;
import com.ibm.safe.internal.runners.AbstractSolverRunner.AnalysisStatus;
import com.ibm.safe.metrics.ClassStatistics;
import com.ibm.safe.metrics.IMetrics;
import com.ibm.safe.metrics.ProgramStatistics;
import com.ibm.safe.perf.NamedTimer;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.reporting.message.SignatureUtils;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.rules.TypestateRule;
import com.ibm.wala.classLoader.IClass;

abstract class AbstractBasicOutputReporter implements IReporter {

  // --- Abstract methods defnition

  protected abstract void errorWrite(final String message);

  protected abstract void write(final String message);

  // --- Interface methods implementation

  public final void process(final IClass clazz) {
    write("Processing class " + getClassName(clazz));
  }

  public final void reportException(final Throwable exception) {
    final StringWriter strWriter = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(strWriter);
    exception.printStackTrace(printWriter);
    printWriter.close();
    errorWrite(strWriter.toString());
  }

  public final void reportMessage(final Message message) {
    final StringBuilder msgBuilder = new StringBuilder("    - "); //$NON-NLS-1$
    if (message.getRule() != null) {
      msgBuilder.append(message.getRule().getSeverity().toString()).append(": "); //$NON-NLS-1$
    }
    msgBuilder.append(message.getText()).append(" (") //$NON-NLS-1$
        .append(SignatureUtils.getClassName(message.getLocation()));
    if (message.getLocation().getByteCodeLocation().startsWith("<init>")) {
      final int parenIndex = message.getLocation().getSourceLocation().indexOf('(');
      msgBuilder.append(message.getLocation().getSourceLocation().substring(parenIndex));
    } else {
      msgBuilder.append('.').append(message.getLocation().getSourceLocation());
    }
    if (message.getLocation().getLocationLineNumber() != -1) {
      msgBuilder.append(':').append(message.getLocation().getLocationLineNumber());
    }
    if (message.getLocation().getByteCodeIndex() != -1) {
      msgBuilder.append(";bci:").append(message.getLocation().getByteCodeIndex());
    }
    msgBuilder.append(')');

    write(msgBuilder.toString());
  }

  public final void reportAnalysisStatus(final AnalysisStatus status) {
    if (status == AnalysisStatus.NORMAL) {
      write("All analyses terminated normally");
    } else {
      write("Done. Some analyses terminated with status: " + status);
    }
  }

  public final void reportNumberOfFindings(final int numberOfFindings) {
    write("Done with " + numberOfFindings + " finding(s).");
  }

  public final void reportNumberOfRulesActivated(final int numberOfRules) {
    write(numberOfRules + " rule(s) activated.");
  }

  public final void reportRuleLoading(final IRule rule) {
    write("Rule " + rule.getName() + " loaded");
  }

  public final void reportRuleInstances(final IRule rule, int instances) {
    if (!(rule instanceof TypestateRule)) {
      return;
    }
    write("TypeState rule " + rule.getName() + " has " + instances + " instances.");
  }

  public final void reportPerformanceTracking(final PerformanceTracker perfoTracker) {
    final NamedTimer[] timers = perfoTracker.getTimers();
    if (timers.length == 0)
      return;

    write(EMPTY_STRING);
    write(perfoTracker.getTrackerName());
    final TotalResultsWrapper totalResultsWrapper = new TotalResultsWrapper();
    for (int i = 0; i < timers.length; i++) {
      writePerformanceResult(timers[i], totalResultsWrapper);
    }

    write("Total time: " + totalResultsWrapper.totalTime + " ms");
  }

  public final void reportStatistics(final ProgramStatistics programStat) {
    write(EMPTY_STRING);
    write("Structural Statistics:\n");
    write("Number of classes\t\t: " + programStat.getEntry(ProgramStatistics.NUM_CLASSES));
    write("Number of interfaces\t\t: " + programStat.getEntry(ProgramStatistics.NUM_INTERFACES));
    write("Number of abstract classes\t\t: " + programStat.getEntry(ProgramStatistics.NUM_ABSTRACT_CLASSES));

    long numberOfInstanceFields = 0;
    long numberOfFinalInstanceFields = 0;
    long numberOfStaticFields = 0;
    long numberOfFinalStaticFields = 0;
    long numberOfMethods = 0;
    long numberOfSynchronizedMethods = 0;
    long numberOfStaticMethods = 0;
    long numberOfByteCodeLocs = 0;
    for (Iterator<ClassStatistics> iter = programStat.classStats.values().iterator(); iter.hasNext();) {
      final ClassStatistics classStats = iter.next();
      numberOfInstanceFields += classStats.numberOfInstanceFields;
      numberOfFinalInstanceFields += classStats.numberOfFinalInstanceFields;
      numberOfStaticFields += classStats.numberOfStaticFields;
      numberOfFinalStaticFields += classStats.numberOfFinalStaticFields;
      numberOfMethods += classStats.numberOfMethods;
      numberOfSynchronizedMethods += classStats.numberOfSynchronizedMethods;
      numberOfStaticMethods += classStats.numberOfStaticMethods;
      numberOfByteCodeLocs += classStats.numberOfByteCodeLocs;
    }

    write("Number of Instance Fields: " + numberOfInstanceFields);
    write("Number of Final Instance Fields: " + numberOfFinalInstanceFields);
    write("Number of Static Fields: " + numberOfStaticFields);
    write("Number of Final Static Fields: " + numberOfFinalStaticFields);
    write("Number of Methods: " + numberOfMethods);
    write("Number of Synchronized Methods: " + numberOfSynchronizedMethods);
    write("Number of Static Methods: " + numberOfStaticMethods);
    write("Number of ByteCode Locs Fields: " + numberOfByteCodeLocs);
  }

  public void reportStatistics(final IMetrics metrics) {
  }

  public final void startAnalysis(final AnalysisKind nature) {
    write(EMPTY_STRING);
    write("***** " + nature + " Analysis *****");
  }

  public final void stopAnalysis(final AnalysisKind nature) {
    write(EMPTY_STRING);
  }

  public final void version(final String versionNumber) {
    write(EMPTY_STRING);
    write("Version: " + versionNumber);
  }

  // --- Private code

  private String getClassName(final IClass currentClass) {
    return currentClass.getName().toString().substring(1).replace('/', '.');
  }

  private void writePerformanceResult(final NamedTimer timer, final TotalResultsWrapper totalResultsWrapper) {
    totalResultsWrapper.totalTime += timer.getElapsedMillis();

    write(timer.getName() + " \t Time = " + timer.getElapsedMillis() + " ms");
  }

  private static class TotalResultsWrapper {

    private long totalTime = 0;

  }

  private static final String EMPTY_STRING = ""; //$NON-NLS-1$

}
