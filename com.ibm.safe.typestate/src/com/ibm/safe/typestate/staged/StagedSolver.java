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
package com.ibm.safe.typestate.staged;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.controller.ISafeSolver;
import com.ibm.safe.internal.exceptions.MaxFindingsException;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.internal.exceptions.SolverTimeoutException;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.reporting.message.ISolverResult;
import com.ibm.safe.typestate.controller.TypeStateSolverKind;
import com.ibm.safe.typestate.core.AbstractTypestateSolver;
import com.ibm.safe.typestate.core.BenignOracle;
import com.ibm.safe.typestate.core.TypeStateProblem;
import com.ibm.safe.typestate.core.TypeStateProperty;
import com.ibm.safe.typestate.core.TypestateSolverFactory;
import com.ibm.safe.typestate.core.WholeProgramSupergraph;
import com.ibm.safe.typestate.metrics.TypeStateMetrics;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.rules.InstanceBatchIterator;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.Iterator2Collection;

/**
 * Execute different solvers in a staged manner
 * 
 * @author yahave
 * 
 */
public class StagedSolver extends AbstractTypestateSolver {

  /**
   * definition of the sequence of stages, in order TODO: can expose that as a
   * property with a defined default, but don't need it now
   */
  final static TypeStateSolverKind[] stageKinds = { TypeStateSolverKind.LOCAL_MUST_MUSTNOT, TypeStateSolverKind.UNIQUE,
      TypeStateSolverKind.AP_MUST_MUSTNOT };

  /**
   * we could have stored only results of last stage, but for now, at least for
   * the sake of tracability, keep all results
   * 
   * Be very careful that this doesn't cause a massive leak! Never ever store a
   * lot of data in a SafeSolverResult!
   */
  private ISolverResult[] stageResults = new ISolverResult[stageKinds.length];

  private final AnalysisOptions domoOptions;

  private final PerformanceTracker perfTracker;

  public StagedSolver(AnalysisOptions domoOptions, CallGraph cg, PointerAnalysis pointerAnalysis, TypeStateProperty property,
      TypeStateOptions options, BenignOracle ora, TypeStateMetrics metrics, IReporter reporter, PerformanceTracker perfTracker) {
    super(cg, pointerAnalysis, property, options, null, ora, metrics, reporter, null, null);
    this.domoOptions = domoOptions;
    this.perfTracker = perfTracker;

  }

  /**
   * Perfom the analysis
   * 
   * @throws WalaException
   * @throws SetUpException
   * @throws PropertiesException
   * @throws CancelException
   */
  public ISolverResult perform(final IProgressMonitor monitor) throws WalaException, SolverTimeoutException, SetUpException,
      PropertiesException, CancelException {
    monitor.beginTask(null, 1);
    monitor.subTask(toString());

    optionalStatistics();

    for (int i = 0; i < stageKinds.length; i++) {
      String timingKey = null;
      try {
        if (DEBUG_LEVEL > 1) {
          System.err.println(stageKinds[i]);
        }
        ISafeSolver s = TypestateSolverFactory.getSolver(stageKinds[i], domoOptions, getCallGraph(), getPointerAnalysis(),
            getPointerAnalysis().getHeapGraph(), getDFA(), getBenignOracle(), getOptions(), getMetrics(), getReporter(),
            perfTracker, null);
        timingKey = stageKinds[i] + " " + s.toString();
        if (perfTracker != null) {
          perfTracker.startTracking(timingKey);
        }
        stageResults[i] = s.perform(monitor);
      } catch (MaxFindingsException e) {
        // TODO: handle this [EY]
      } finally {
        if (perfTracker != null) {
          perfTracker.stopTracking(timingKey);
        }
      }
    }

    if (Thread.interrupted()) {
      throw new SolverTimeoutException(stageResults[stageResults.length - 1]);
    }

    monitor.done();

    return stageResults[stageResults.length - 1];
  }

  private void optionalStatistics() throws PropertiesException {
    if (getOptions().shouldCollectStatistics()) {
      initializeProperty();
      Collection<InstanceKey> instances = computeTrackedInstances();
      getMetrics().setNumberOfDFASliceCandidateStatements(getPropertyName(), countCandidateStatements(instances));

      if (getMetrics().getUnoptimizedSupergraphSize() == 0) {
        initializeNoCollapseSet();
        AnalysisCache ac = new AnalysisCacheImpl();
        WholeProgramSupergraph g = buildSupergraph(ac, Iterator2Collection.toList(getCallGraph().iterator()));
        getMetrics().setUnoptimizedSupergraphSize(g.getNumberOfNodes());
      }
    }
  }

  @Override
  protected TypeStateProblem createTypeStateProblem(ICFGSupergraph supergraph, Collection<InstanceKey> instances)
      throws WalaException, PropertiesException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void initializeDomain(Collection<InstanceKey> instances) {
    // TODO Auto-generated method stub
    
  }

  @Override
  protected InstanceBatchIterator makeBatchIterator(Collection<InstanceKey> allInstances) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected boolean supportsWitnessGeneration() {
    // TODO Auto-generated method stub
    return false;
  }

}
