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
package com.ibm.safe.structural.statistics;

import java.util.Iterator;

import com.ibm.safe.metrics.ClassStatistics;
import com.ibm.safe.metrics.ProgramStatistics;
import com.ibm.safe.processors.BaseClassProcessor;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/**
 * This processor collects simple statistics about classes in the application.
 * 
 * @author Eran Yahav (yahave)
 */
public class StatisticsClassProcessor extends BaseClassProcessor {

  private final ProgramStatistics programStats;

  public StatisticsClassProcessor(IClassHierarchy hierarchy, CallGraph callGraph, ProgramStatistics programStatistics) {
    super(hierarchy, callGraph);
    this.programStats = programStatistics;
  }

  public void process(IClass klass) {
    ClassStatistics cs = new ClassStatistics();
    int methodNumber = 0;
    int staticMethodNumber = 0;
    int syncMethodNumber = 0;
    // int unresolvedFields =0;
    int numberOfFinalStaticFields = 0;
    int numberOfFinalInstanceFields = 0;
    long lob = 0;

    cs.className = klass.getName().toString();
    cs.numberOfStaticFields = klass.getDeclaredStaticFields().size();
    cs.numberOfInstanceFields = klass.getDeclaredInstanceFields().size();

    for (Iterator<IField> it = klass.getDeclaredStaticFields().iterator(); it.hasNext();) {
      IField fld = it.next();

      if (fld != null && fld.isFinal()) {
        numberOfFinalStaticFields++;
      }
    }
    cs.numberOfFinalStaticFields = numberOfFinalStaticFields;

    for (Iterator<IField> it = klass.getDeclaredInstanceFields().iterator(); it.hasNext();) {
      IField fld = it.next();

      if (fld != null && fld.isFinal()) {
        numberOfFinalInstanceFields++;
      }
    }
    cs.numberOfFinalInstanceFields = numberOfFinalInstanceFields;

    if (klass.isAbstract()) {
      programStats.incrementEntry(ProgramStatistics.NUM_ABSTRACT_CLASSES);
    }
    if (klass.isInterface()) {
      programStats.incrementEntry(ProgramStatistics.NUM_INTERFACES);
    }
    programStats.incrementEntry(ProgramStatistics.NUM_CLASSES);

    for (Iterator<IMethod> it = klass.getDeclaredMethods().iterator(); it.hasNext();) {
      IMethod method = (IMethod) it.next();
      if (method instanceof ShrikeCTMethod) {
        ShrikeCTMethod shrikeMethod = (ShrikeCTMethod) method;
        if (shrikeMethod.getBytecodeStream() != null) {
          lob = lob + shrikeMethod.getBytecodeStream().length();
        }
      }
      if (method.isStatic()) {
        staticMethodNumber++;
      }
      if (method.isSynchronized()) {
        syncMethodNumber++;
      }
      methodNumber++;
    }
    cs.numberOfMethods = methodNumber;
    programStats.incrementEntry(ProgramStatistics.NUM_METHODS, methodNumber);

    cs.numberOfStaticMethods = staticMethodNumber;
    cs.numberOfSynchronizedMethods = syncMethodNumber;
    cs.numberOfByteCodeLocs = lob;
    programStats.incrementEntry(ProgramStatistics.TOTAL_LOB, lob);
    programStats.classStats.put(cs.className, cs);
  }

}
