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
package com.ibm.safe.typestate.quad;

import java.util.Collection;

import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.typestate.core.AbstractTypestateSolver;
import com.ibm.safe.typestate.core.BenignOracle;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.metrics.TypeStateMetrics;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.typestate.rules.InstanceBatchIterator;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;

/**
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public abstract class QuadSolver extends AbstractTypestateSolver {

  /**
   * Instantiate a new base-safe-solver.
   * 
   * @param cg
   *          - underlying callgraph
   * @param pointerAnalysis
   *          - results of pointer-analysis
   * @param warnings
   *          - collector of produced warnings
   */
  public QuadSolver(AnalysisOptions domoOptions, CallGraph cg, PointerAnalysis pointerAnalysis, ITypeStateDFA dfa,
      TypeStateOptions options, ILiveObjectAnalysis live, BenignOracle ora, TypeStateMetrics metrics, IReporter reporter,
      TraceReporter traceReporter, IMergeFunctionFactory mergeFactory) {
    super(cg, pointerAnalysis, dfa, options, live, ora, metrics, reporter, traceReporter, mergeFactory);
  }

  protected InstanceBatchIterator makeBatchIterator(Collection<InstanceKey> allInstances) {
    return InstanceBatchIterator.makeSeparation(allInstances);
  }

  protected boolean supportsWitnessGeneration() {
    return false;
  }

  public String toString() {
    return "Solver for " + this.getDFA().toString();
  }
}