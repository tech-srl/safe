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
package com.ibm.safe.typestate.options;

import java.util.Map;

import com.ibm.safe.dfa.IDFAStateFactory;
import com.ibm.safe.typestate.merge.FutureMerge;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.mine.EventNameStateFactory;
import com.ibm.safe.typestate.mine.LossLessMerge;
import com.ibm.safe.typestate.mine.StateSimulationMerge;
import com.ibm.safe.typestate.mine.UnifyMerge;
import com.ibm.safe.typestate.mine.UniqueNameStateFactory;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.debug.Assertions;

/**
 * Mining merge algorithms
 */
public class MineMergeKind {

  private static Map<String, MineMergeKind> allKinds = HashMapFactory.make();

  public static final MineMergeKind NONE = createMergeKind("None");

  public static final MineMergeKind TOTAL = createMergeKind("Total");

  public static final MineMergeKind SIMULATION = createMergeKind("Simulation");

  public static final MineMergeKind LOSSLESS = createMergeKind("Lossless");

  public static final MineMergeKind FUTURE = createMergeKind("Future");

  public String toString() {
    return this.mergeKind;
  }

  private MineMergeKind(final String mergeKind) {
    this.mergeKind = mergeKind;
  }

  private static MineMergeKind createMergeKind(final String mergeKind) {
    MineMergeKind mmk = new MineMergeKind(mergeKind);
    allKinds.put(mergeKind, mmk);
    return mmk;
  }

  private final String mergeKind;

  public static MineMergeKind getMergeKindFromString(String mineMergeKindString) {
    return allKinds.get(mineMergeKindString);
  }

  public static IMergeFunctionFactory getMergeFactory(MineMergeKind mmk) {
    if (mmk == MineMergeKind.NONE) {
      return null;
    } else if (mmk == MineMergeKind.TOTAL) {
      return UnifyMerge.factory();
    } else if (mmk == MineMergeKind.SIMULATION) {
      return StateSimulationMerge.factory();
    } else if (mmk == MineMergeKind.LOSSLESS) {
      return LossLessMerge.factory();
    } else if (mmk == MineMergeKind.FUTURE) {
      return FutureMerge.factory();
    } else {
      Assertions.UNREACHABLE();
      return null;
    }
  }

  public static IDFAStateFactory getStateFactory(MineMergeKind mmk) {
    if (mmk == MineMergeKind.FUTURE) {
      return new UniqueNameStateFactory();
    } else {
      return new EventNameStateFactory();
    }
  }
}