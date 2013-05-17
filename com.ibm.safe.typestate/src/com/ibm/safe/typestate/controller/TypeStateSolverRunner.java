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
package com.ibm.safe.typestate.controller;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ibm.safe.callgraph.CallGraphEngine;
import com.ibm.safe.controller.ISafeSolver;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.runners.WholeProgramSolverRunner;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.perf.PerformanceTracker.Stages;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.reporting.message.Messages;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.rules.TypestateRule;
import com.ibm.safe.runners.IResultObserver;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashSetFactory;

/**
 * @author egeay
 * @author yahave
 */
public class TypeStateSolverRunner extends WholeProgramSolverRunner {

  protected TypestateRule[] rules;

  public TypeStateSolverRunner(final TypeStateOptions properties, final IRule[] typeStateRules,
      final PerformanceTracker perfoTracker, final IReporter reporter) {
    super(properties, perfoTracker, reporter);
    this.rules = (TypestateRule[]) typeStateRules;
  }

  public void run(final IResultObserver observer, final IProgressMonitor monitor) throws SafeException, IllegalArgumentException,
      CancelException, CoreException {
    TraceReporter tReporter = (shouldMineDFA()) ? makeTraceReporter() : null;
    SubProgressMonitor cgMonitor = new SubProgressMonitor(monitor, 8);
    CallGraphEngine cge = createCallGraphEngine(cgMonitor);
    cge.setInterestingTypes(getPropertyTypes());
    TypeStateSolverCreator creator = new TypeStateSolverCreator(cge, getManager(), getReporter(), getPerformanceTracker(),
        tReporter);

    monitor.beginTask(null, 20);
    monitor.subTask(Messages.TypeStateSolverRunner_CreateCallGraph);

    final ISafeSolver[] solvers = creator.createSolvers();
    runSolvers(solvers, observer, AnalysisKind.TYPESTATE, Stages.TYPESTATE, new SubProgressMonitor(monitor, 12));
    if (creator.getMetrics() != null) {
      getReporter().reportStatistics(creator.getMetrics());
    }
    if (shouldMineDFA()) {
      try {
        tReporter.persist();
      } catch (WalaException e) {
        throw new SafeException("trace persist failed", e);
      }
    }
  }

  // --- Public services

  public void setRules(final TypestateRule[] typestateRules) {
    this.rules = typestateRules;
  }

  public String toString() {
    return "TypeState"; //$NON-NLS-1$
  }

  protected String getTVLAFileName(String dir, String fileName) {
    if (fileName == null)
      return null;

    File file = new File(fileName);
    if (file.isAbsolute())
      return fileName;

    if (dir == null) {
      Trace.println("Expected to find here the output dir - wierd!");
      return fileName;
    }

    String fullName = dir + File.separator + fileName;

    return fullName;
  }

  protected String computeTVLAAnalysisDir(String rootDir, String subDir) {
    String dir = null;
    if (rootDir == null)
      dir = subDir;
    else if (subDir == null)
      dir = rootDir;
    else
      dir = rootDir + subDir;

    return dir;
  }

  protected TraceReporter makeTraceReporter() throws PropertiesException {
    TypeStateOptions tso = this.getManager();

    assert tso.getShortProgramName() != null : "mining: must specify -" + CommonProperties.Props.SHORT_PROGRAM_NAME.getName();

    return new TraceReporter(tso.getTypeStateSolverKindString(), tso.getMineMergeKindString(), tso.getMineType(), tso
        .getShortProgramName(), tso.getOutputDirectory(), tso.getAbstractTraceFileName());
  }

  protected TypeStateOptions getManager() {
    return (TypeStateOptions) this.propertiesManager;
  }

  private String[] getPropertyTypes() {
    Collection<String> propertyTypes = HashSetFactory.make();
    for (int i = 0; i < rules.length; i++) {
      for (Iterator<String> it = rules[i].getTypes().iterator(); it.hasNext();) {
        final String typeDef = it.next();
        propertyTypes.add(typeDef);
      }
    }
    return propertyTypes.toArray(new String[propertyTypes.size()]);
  }
}