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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.ibm.safe.internal.runners.AbstractSolverRunner.AnalysisKind;
import com.ibm.safe.internal.runners.AbstractSolverRunner.AnalysisStatus;
import com.ibm.safe.metrics.IMetrics;
import com.ibm.safe.metrics.ProgramStatistics;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.rules.IRule;
import com.ibm.wala.classLoader.IClass;

public final class CompositeReporter implements IReporter {

  // --- Interface methods implementation

  public void process(final IClass clazz) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().process(clazz);
    }
  }

  public void produceFinalReport() throws Exception {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().produceFinalReport();
    }
  }

  public void reportException(final Throwable exception) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().reportException(exception);
    }
  }

  public void reportMessage(final Message message) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().reportMessage(message);
    }
  }

  public void reportStatistics(final ProgramStatistics programStat) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().reportStatistics(programStat);
    }
  }

  public void reportStatistics(final IMetrics typeStateMetrics) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().reportStatistics(typeStateMetrics);
    }
  }

  public void reportNumberOfFindings(final int numberOfFindings) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().reportNumberOfFindings(numberOfFindings);
    }
  }

  public void reportNumberOfRulesActivated(final int numberOfRules) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().reportNumberOfRulesActivated(numberOfRules);
    }
  }

  public void reportRuleLoading(final IRule rule) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().reportRuleLoading(rule);
    }
  }

  public void reportRuleInstances(final IRule rule, int instances) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().reportRuleInstances(rule, instances);
    }
  }

  public void reportPerformanceTracking(final PerformanceTracker perfoTracker) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().reportPerformanceTracking(perfoTracker);
    }
  }

  public void startAnalysis(final AnalysisKind nature) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().startAnalysis(nature);
    }
  }

  public void stopAnalysis(final AnalysisKind nature) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().stopAnalysis(nature);
    }
  }

  public void reportAnalysisStatus(final AnalysisStatus status) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().reportAnalysisStatus(status);
    }
  }

  public void version(final String versionNumber) {
    for (Iterator<IReporter> iter = this.reports.iterator(); iter.hasNext();) {
      iter.next().version(versionNumber);
    }
  }

  // --- Public services

  public void addReporter(final IReporter reporter) {
    this.reports.add(reporter);
  }

  // --- Private code

  private final Collection<IReporter> reports = new ArrayList<IReporter>(3);

}
