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
/*
 * Created on Dec 23, 2004
 */
package com.ibm.safe.typestate.unique;

import java.util.Collection;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.typestate.base.SeparatingSolver;
import com.ibm.safe.typestate.core.BenignOracle;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.core.TypeStateProblem;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.metrics.TypeStateMetrics;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.util.WalaException;

/**
 * Note: UniqueSolver is currently _not sound_ as it does not check that the
 * pointer performing an operation must-be-pointing to the object of interest.
 * The only thing that it currently checks is the fact that the pointer may be
 * pointing to a singleton alloc-site, but one still needs to check that pointer
 * must-be-pointing to that allocated object. [EY]
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 * 
 */
public class UniqueSolver extends SeparatingSolver {
  /**
   * Instantiate a new unique-safe-solver.
   * 
   * @param cg -
   *            underlying callgraph
   * @param pointerAnalysis -
   *            results of pointer-analysis
   * @param warnings -
   *            collector of produced warnings
   */
  public UniqueSolver(CallGraph cg, PointerAnalysis pointerAnalysis, ITypeStateDFA dfa, TypeStateOptions options,
      ILiveObjectAnalysis live, BenignOracle ora, TypeStateMetrics metrics, IReporter reporter, TraceReporter traceReporter,
      IMergeFunctionFactory mergeFactory) {
    super(cg, pointerAnalysis, dfa, options, live, ora, metrics, reporter, traceReporter, mergeFactory);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.base.BaseSafeSolver#initializeDomain(java.util.Collection)
   */
  protected void initializeDomain(Collection<InstanceKey> instances) {
    TypeStateDomain d = new UniqueTypeStateDomain(getDFA(), getOptions());
    setDomain(d);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.AbstractSolver#createTypeStateProblem(com.ibm.wala.dataflow.IFDS.ISupergraph,
   *      com.ibm.wala.ipa.cfg.InterproceduralCFG, java.util.Collection)
   */
  protected TypeStateProblem createTypeStateProblem(ICFGSupergraph supergraph, Collection<InstanceKey> instances)
      throws WalaException {
    return new UniqueProblem(getCallGraph(), getPointerAnalysis(), supergraph, getDomain(), getDFA(), instances,
        getLiveObjectAnalysis(), getTraceReporter(), getMergeFactory().create(getDomain()));
  }
}