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
package com.ibm.safe.lightweight.controller;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ibm.safe.cha.J2SEClassHierarchyEngine;
import com.ibm.safe.controller.ISafeSolver;
import com.ibm.safe.controller.RulesManager;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.internal.runners.AbstractSolverRunner;
import com.ibm.safe.lightweight.options.IStructuralOptions;
import com.ibm.safe.lightweight.options.LightWeightOptions;
import com.ibm.safe.lightweight.options.StructuralOptions;
import com.ibm.safe.lightweight.options.LightweightProperties.Props;
import com.ibm.safe.options.CommonOptions;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.perf.PerformanceTracker.Stages;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.reporting.message.Messages;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.rules.StructuralRule;
import com.ibm.safe.runners.IResultObserver;
import com.ibm.safe.structural.StructuralSolver;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.Filter;

public final class StructuralSolverRunner extends AbstractSolverRunner {

  public StructuralSolverRunner(final PropertiesManager properties, final IRule[] structuralRules,
      final PerformanceTracker perfoTracker, final IReporter reporter) {
    super(new LightWeightOptions(properties), perfoTracker, reporter);
    this.rules = structuralRules;
  }

  public void run(final IResultObserver observer, final IProgressMonitor monitor) throws SafeException, SetUpException,
      CancelException, CoreException {
    final CommonOptions commonOptions = getCommonOptions();

    ISafeSolver[] solvers = null;
    StructuralOptions structuralOptions = new StructuralOptions(this.rules, getClassFilterList(), getDumpXMLDirectory(),
        shouldCollectStatistics(), pessimisticEval());
    SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, 5);

    try {
      if (getModulesCodeKind(commonOptions) == CodeKind.J2SE) {
        IClassHierarchy cha = StructuralSolverRunner.createJ2SEClassHierarchy(getManager(), getPerformanceTracker(), monitor);
        solvers = StructuralSolverRunner.createSolversStatic(cha, structuralOptions.getClassFilter(), structuralOptions,
            getReporter());
      } else {
        IClassHierarchy cha = StructuralSolverRunner.createJ2EEClassHierarchy(getManager(), getPerformanceTracker(), monitor);
        solvers = StructuralSolverRunner.createSolversStatic(cha, structuralOptions.getClassFilter(), structuralOptions,
            getReporter());
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    monitor.beginTask(null, 10);
    monitor.subTask(Messages.StructuralSolverRunner_ClassHierarchyConstruction);

    runSolvers(solvers, observer, AnalysisKind.STRUCTURAL, Stages.STRUCTURAL, subProgressMonitor);
  }

  protected final String[] getClassFilterList() throws PropertiesException {
    final String filterCommand = this.propertiesManager.getStringValue(Props.SELECT_CLASSES);
    return (filterCommand == null) ? new String[0] : filterCommand.split(RulesManager.LIST_REGEX_SEPARATOR);
  }

  // --- Public services

  public void setRules(final StructuralRule[] structuralRules) {
    this.rules = structuralRules;
  }

  // --- Overridden methods

  public String toString() {
    return "Structural"; //$NON-NLS-1$
  }

  // --- Private code

  private String getDumpXMLDirectory() throws PropertiesException {
    return getManager().getStringValue(Props.DUMP_XML_DIRECTORY);
  }

  protected final boolean pessimisticEval() throws PropertiesException {
    return this.propertiesManager.getBooleanValue(Props.PESSIMISTIC_EVAL);
  }

  public static ISafeSolver[] createSolversStatic(IClassHierarchy cha, Filter<IClass> classFilter, IStructuralOptions sOptions,
      IReporter reporter) {
    return new ISafeSolver[] { new StructuralSolver(cha, classFilter, null /* callGraph */, null /* pointerAnalysis */, sOptions,
        reporter) };
  }

  public static IClassHierarchy createJ2SEClassHierarchy(final LightWeightOptions analysisOptions,
      final PerformanceTracker perfoTracker, final IProgressMonitor monitor) throws SafeException {
    J2SEClassHierarchyEngine x = new J2SEClassHierarchyEngine(analysisOptions, perfoTracker, monitor);
    return x.buildClassHierarchy();
  }

  public static IClassHierarchy createJ2EEClassHierarchy(final LightWeightOptions analysisOptions,
      final PerformanceTracker perfoTracker, final IProgressMonitor monitor) throws PropertiesException {
    //J2EEClassHierarchyEngine x = new J2EEClassHierarchyEngine(analysisOptions, perfoTracker, monitor);
    //return x.buildClassHierarchy();
    return null;
  }

  private IRule[] rules;

  protected LightWeightOptions getManager() {
    return (LightWeightOptions) this.propertiesManager;
  }

}
