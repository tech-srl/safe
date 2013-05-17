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
package com.ibm.safe.metrics;

import java.util.Iterator;
import java.util.Map;

import com.ibm.wala.util.collections.HashMapFactory;

public class ProgramStatistics {
  private final static int SIZE = 5;

  private final static String[] NAMES = { "Num of classes", "Num interfaces", "Num abstract classes", "Total LOB", "Num of methods" };

  private static long[] values = new long[SIZE];

  public static final int NUM_CLASSES = 0;

  public static final int NUM_INTERFACES = 1;

  public static final int NUM_ABSTRACT_CLASSES = 2;

  public static final int TOTAL_LOB = 3;

  public static final int NUM_METHODS = 4;

  public String getName(int entry) {
    return NAMES[entry];
  }

  public void incrementEntry(int entry) {
    values[entry]++;
  }

  public void incrementEntry(int entry, long increment) {
    values[entry] += increment;
  }

  public void setEntry(int entry, long value) {
    values[entry] = value;
  }

  public long getEntry(int entry) {
    return values[entry];
  }

  public Map<String, ClassStatistics> classStats = HashMapFactory.make();

  public String toString() {
    StringBuffer result = new StringBuffer();

    result.append("Number of classes: " + getEntry(NUM_CLASSES));
    result.append("\n");
    result.append("Number of abstract classes: " + getEntry(NUM_ABSTRACT_CLASSES));
    result.append("\n");
    result.append("Number of interfaces: " + getEntry(NUM_INTERFACES));
    result.append("\n");
    result.append("\n");

    for (Iterator<ClassStatistics> it = classStats.values().iterator(); it.hasNext();) {
      ClassStatistics curr = it.next();
      result.append(curr.toString());
      result.append("\n");
    }

    return result.toString();
  }

}