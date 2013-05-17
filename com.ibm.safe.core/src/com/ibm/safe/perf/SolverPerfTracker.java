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
package com.ibm.safe.perf;

import java.util.Map;

import com.ibm.safe.reporting.message.AggregateSolverResult;
import com.ibm.safe.reporting.message.ISolverResult;
import com.ibm.wala.util.collections.HashMapFactory;

public final class SolverPerfTracker extends PerformanceTracker {

  public SolverPerfTracker(final String trackerName, final Kind trackerKind) {
    super(trackerName, trackerKind);
  }

  // --- Interface methods implementation

  public int getProcessedInstances(final String trackerName) {
    final Integer value = this.solverProcessedInstances.get(trackerName);
    if (value == null) {
      return 0;
    } else {
      return value.intValue();
    }
  }

  public int getTotalInstances(final String trackerName) {
    final Integer value = this.solverTotalInstances.get(trackerName);
    if (value == null) {
      return 0;
    } else {
      return value.intValue();
    }
  }

  public void stopTracking(final String timerName, final ISolverResult result) {
    super.stopTracking(timerName);
    updateProgress(timerName, result);
  }

  public void timeout(final String timerName, ISolverResult result) {
    final TimeoutStopwatch current = (TimeoutStopwatch) super.timers.get(timerName);
    assert current != null;
    current.timeout();
    updateProgress(timerName, result);
  }

  // --- Overridden methods

  protected NamedTimer createTimerInstance(final String timerName) {
    return new TimeoutStopwatch(timerName);
  }

  private void updateProgress(final String timerName, final ISolverResult result) {
    if (result instanceof AggregateSolverResult && result != null) { // short
      // works for now
      AggregateSolverResult r = (AggregateSolverResult) result;
      this.solverTotalInstances.put(timerName, r.totalInstancesNum());
      this.solverProcessedInstances.put(timerName, r.processedInstancesNum());
    }
  }

  private Map<String, Integer> solverTotalInstances = HashMapFactory.make();

  private Map<String, Integer> solverProcessedInstances = HashMapFactory.make();

}
