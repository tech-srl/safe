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

package com.ibm.safe.typestate.local;

import java.util.Collection;
import java.util.Collections;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.accesspath.AccessPathSetTransformers;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.typestate.ap.must.AbstractMustAPSolver;
import com.ibm.safe.typestate.ap.must.MustAPSetTransformers;
import com.ibm.safe.typestate.ap.must.mustnot.MustMustNotAPFunctionProvider;
import com.ibm.safe.typestate.core.BenignOracle;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.core.TypeStateFunctionProvider;
import com.ibm.safe.typestate.core.TypeStateProblem;
import com.ibm.safe.typestate.core.TypeStateProperty;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.metrics.TypeStateMetrics;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.quad.QuadTypeStateDomain;
import com.ibm.safe.typestate.rules.InstanceBatchIterator;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.Iterator2Collection;
import com.ibm.wala.util.graph.GraphReachability;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * An intra-procedural typestate solver that uses AP MustMustNot logic
 * 
 * @author sfink
 * @author yahave
 */
public class LocalMustMustNotSolver extends AbstractLocalSolver {

  /**
   * graph view of pointer analysis
   */
  private final HeapGraph heapGraph;

  /**
   * call graph reachability analysis
   */
  private final GraphReachability<CGNode,CGNode> reach;

  /**
   * @param cg
   * @param pointerAnalysis
   * @param property
   * @param options
   * @param live
   * @param warnings
   */
  public LocalMustMustNotSolver(CallGraph cg, PointerAnalysis pointerAnalysis, HeapGraph heapGraph, TypeStateProperty property,
      TypeStateOptions options, ILiveObjectAnalysis live, GraphReachability<CGNode,CGNode> reach, BenignOracle ora,
      TypeStateMetrics metrics, IReporter reporter, IMergeFunctionFactory mergeFactory) {
    super(cg, pointerAnalysis, property, options, live, ora, metrics, reporter, mergeFactory);
    this.heapGraph = heapGraph;
    this.reach = reach;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.AbstractSolver#initializeDomain(java.util.Collection)
   */
  protected void initializeDomain(Collection<InstanceKey> instances) {
    TypeStateDomain d = new QuadTypeStateDomain(getDFA(), getOptions(), getPointerAnalysis());
    setDomain(d);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.local.AbstractLocalSolver#makeFlowFunctions()
   */
  protected TypeStateFunctionProvider makeFlowFunctions(ICFGSupergraph supergraph, Collection<InstanceKey> instances)
      throws PropertiesException {
    AccessPathSetTransformers apst = new MustAPSetTransformers(getPointerAnalysis(), reach);
    return new MustMustNotAPFunctionProvider(getCallGraph(), getPointerAnalysis(), supergraph, (QuadTypeStateDomain) getDomain(),
        getDFA(), instances, apst, reach, getOptions(), getLiveObjectAnalysis(), null);
  }

  /**
   * @return Returns the heapGraph.
   */
  protected HeapGraph getHeapGraph() {
    return heapGraph;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.local.AbstractLocalSolver#computeNodesThatMatter(com.ibm.wala.ipa.callgraph.propagation.InstanceKey)
   */
  protected Collection<CGNode> computeNodesThatMatter(InstanceKey ik) throws PropertiesException {
    OrdinalSet<InstanceKey> s = toOrdinalInstanceSet(Collections.singleton(ik));
    try {
      return AbstractMustAPSolver.computeNodesThatDirectlyMatter(s, getLiveObjectAnalysis(), getCallGraph(), getHeapGraph(),
          getDFA(), getOptions().getAccessPathKLimit());
    } catch (WalaException e) {
      // uh oh. give up. shouldn't happen.
      e.printStackTrace();
      return Iterator2Collection.toList(getCallGraph().iterator());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.AbstractSolver#createTypeStateProblem(com.ibm.wala.dataflow.IFDS.ISupergraph,
   *      com.ibm.wala.ipa.cfg.InterproceduralCFG, java.util.Collection)
   */
  protected TypeStateProblem createTypeStateProblem(CGNode node, ICFGSupergraph supergraph, Collection<InstanceKey> instances)
      throws WalaException, PropertiesException {
    TypeStateFunctionProvider origFunctions = makeFlowFunctions(supergraph, instances);
    // TODO: support merge function?
    return new TypeStateProblem(supergraph, getDomain(), new LocalMustMustNotFlowFunctions(node, supergraph.getICFG().getCFG(node),
        origFunctions, getNodesThatMatter(instances), instances, getCallGraph(), getPointerAnalysis(), getLiveObjectAnalysis()),
        getMergeFactory().create(getDomain()));
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.AbstractSolver#makeBatchIterator(java.util.Collection)
   */
  @Override
  protected InstanceBatchIterator makeBatchIterator(Collection<InstanceKey> allInstances) {
    return InstanceBatchIterator.makeSeparation(allInstances);
  }

  @Override
  protected TypeStateProblem createTypeStateProblem(ICFGSupergraph supergraph, Collection<InstanceKey> instances)
      throws WalaException, PropertiesException {
    throw new UnsupportedOperationException();
  }
}
