/*******************************************************************************
 * Copyright (c) 2002-2010 IBM Corporation.
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

import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.perf.Stopwatch;

public class PerformanceTracker {

  public static enum Stages {
    CHA, CALLGRAPH, HEAPGRAPH, CALLGRAPH_REACH, STRUCTURAL, NULLDEREF, TYPESTATE
  };

  public static enum Kind {
    GLOBAL, SOLVERS
  }

  public PerformanceTracker(final String trackerName, final Kind trackerKind) {
    this.name = trackerName;
    this.kind = trackerKind;
  }

  public final NamedTimer[] getTimers() {
    return (NamedTimer[]) this.timers.values().toArray(new NamedTimer[this.timers.size()]);
  }

  public final Kind getTrackerKind() {
    return this.kind;
  }

  public final String getTrackerName() {
    return this.name;
  }

  public final void startTracking(final String timerName) {
    assert !this.timers.containsKey(timerName);

    final NamedTimer timer = createTimerInstance(timerName);
    this.timers.put(timerName, timer);
    timer.start();
  }

  public final void stopTracking(final String timerName) {
    final NamedTimer timer = this.timers.get(timerName);
    assert timer != null;
    timer.stop();
  }

  public final Stopwatch getTimer(String timerName) {
    return (Stopwatch) this.timers.get(timerName);
  }

  protected NamedTimer createTimerInstance(final String timerName) {
    return new NamedTimer(timerName);
  }

  public String reportPerformanceTracking() {
    StringBuffer result = new StringBuffer();
    final NamedTimer[] timers = getTimers();
    if (timers.length == 0) {
      return result.toString();
    }
    result.append("\n");
    result.append(getTrackerName());
    int totalTime = 0;
    for (int i = 0; i < timers.length; i++) {
      NamedTimer current = timers[i];
      totalTime += current.getElapsedMillis();
      result.append(current.getName() + " \t Time = " + current.getElapsedMillis() + " ms \n");
    }
    result.append("Total time: " + totalTime + " ms");
    return result.toString();
  }

  protected final Map<String, NamedTimer> timers = HashMapFactory.make();

  private final String name;

  private final Kind kind;

}
