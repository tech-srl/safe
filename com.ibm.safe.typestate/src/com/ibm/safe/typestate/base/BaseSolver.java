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
package com.ibm.safe.typestate.base;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.typestate.core.AbstractTypestateSolver;
import com.ibm.safe.typestate.core.BenignOracle;
import com.ibm.safe.typestate.core.TypeStateProblem;
import com.ibm.safe.typestate.core.TypeStateProperty;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.metrics.TypeStateMetrics;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.typestate.rules.InstanceBatchIterator;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import java.util.function.Predicate;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.CollectionFilter;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Iterator2Collection;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * This is a base implementation of the SAFE solver. This initial version
 * implements the naive interprocedural typestate solver. The domain of this
 * solver consists of (instance-id,object-state) pairs, where instance-ids are
 * obtained from the results of a preceding points-to analysis. The
 * object-states are the possible states of an object as described by a
 * finite-state automaton of the typestate property. This naive implementation
 * does not provide any path sensitivity, and is therefore very likely to
 * produce a large number of false alarms. In addition, this naive
 * implementation does not apply separation and attempts to verify the property
 * for all instances simultaneously [EY]
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public class BaseSolver extends AbstractTypestateSolver {

  /**
   * avoid propagating accepting states?
   */
  static final boolean NO_PROPAGATE_ACCEPT = true;

  /**
   * Instantiate a new base-safe-solver.
   * 
   * @param cg
   *          - underlying callgraph
   * @param pointerAnalysis
   *          - results of pointer-analysis
   * @param dfa
   *          - automaton of typestate property to be verified
   */
  public BaseSolver(CallGraph cg, PointerAnalysis pointerAnalysis, ITypeStateDFA dfa, TypeStateOptions options,
      ILiveObjectAnalysis live, BenignOracle ora, TypeStateMetrics metrics, IReporter reporter, TraceReporter traceReporter,
      IMergeFunctionFactory mergeFactory) {
    super(cg, pointerAnalysis, dfa, options, live, ora, metrics, reporter, traceReporter, mergeFactory);

    assert dfa != null;
    if (!(dfa instanceof TypeStateProperty)) {
      assert traceReporter != null;
    }

  }

  protected TypeStateProblem createTypeStateProblem(ICFGSupergraph supergraph, Collection<InstanceKey> instances)
      throws WalaException {
    return new BaseProblem(getCallGraph(), getPointerAnalysis(), supergraph, getDomain(), getDFA(), instances,
        getLiveObjectAnalysis(), getTraceReporter(), getMergeFactory().create(getDomain()));
  }

  public String toString() {
    assert getDFA() != null;
    return "Solver for " + this.getDFA().toString();
  }

  /**
   * compute the set of nodes in the supergraph which the IFDS solver cannot
   * ignore
   * 
   * @param instances
   *          Set <InstanceKey>, interesting instances
   * @return set of nodes which lie on some path from the root method to an
   *         event which changes state relevant to the IFDS solver, in which
   *         some interesting instance is live.
   * @throws WalaException
   * @throws PropertiesException
   */
  protected Collection<CGNode> computeNodesThatMatter(final OrdinalSet<InstanceKey> instances) throws WalaException,
      PropertiesException {

    if (getOptions().shouldSliceSupergraph()) {

      Assertions.productionAssertion(getLiveObjectAnalysis() != null,
          "expected non-null live object analysis in order to slice supergraph");

      if (DEBUG_LEVEL > 0) {
        Trace.println("Instances: ");
        Trace.println(instances.toString());
        Trace.println("Before slice: " + getCallGraph().getNumberOfNodes());
      }

      Collection<CGNode> eventNodes = scanForEventNodes(instances);
      if (DEBUG_LEVEL > 0) {
        Trace.printCollection("Event nodes", eventNodes);
      }

      Collection<CGNode> relevantCallers = computeRelevantCallers(eventNodes, instances);
      // important: discard the eventNodes ... don't want to consider one
      // event
      // node calling another
      // to be "relevant".
      relevantCallers.removeAll(eventNodes);
      relevantCallers.addAll(computeAllocators(instances));

      if (DEBUG_LEVEL > 0) {
        Trace.printCollection("relevant callers " + relevantCallers.size(), relevantCallers);
      }

      // prune the call graph to include only nodes in which some interesting
      // instance is live.
      Predicate<CGNode> liveFilter = makeLiveNodeFilter(instances, getLiveObjectAnalysis());
      Graph<CGNode> pruned = GraphSlicer.prune(getCallGraph(), liveFilter);

      if (DEBUG_LEVEL > 0) {
        Trace.printCollection("pruned by liveness " + pruned.getNumberOfNodes(), Iterator2Collection.toList(pruned.iterator()));
      }

      // compute the set of nodes in the pruned graph from which some relevant
      // caller is reachable
      Collection<CGNode> result = GraphSlicer.slice(pruned, new CollectionFilter<CGNode>(relevantCallers));
      if (DEBUG_LEVEL > 0) {
        Trace.printCollection("slice " + result.size(), result);
      }

      result = getMinimumSpanningTree(getCallGraph(), getCallGraph().getFakeRootNode(), result);
      if (DEBUG_LEVEL > 0) {
        Trace.printCollection("tree " + result.size(), result);
      }
      result.addAll(eventNodes);

      addAllCalleesOfEventSites(result, eventNodes);

      return result;
    } else {
      return Iterator2Collection.toList(getCallGraph().iterator());
    }
  }

  /**
   * @param instances
   * @return Collection<CGNode>
   */
  private Collection<CGNode> computeAllocators(OrdinalSet<InstanceKey> instances) {
    HashSet<CGNode> result = HashSetFactory.make();
    for (Iterator<InstanceKey> it = instances.iterator(); it.hasNext();) {
      InstanceKey ik = it.next();
      if (ik instanceof AllocationSiteInNode) {
        result.add(((AllocationSiteInNode) ik).getNode());
      } else if (ik instanceof ConcreteTypeKey) {
        IClass type = ((ConcreteTypeKey) ik).getConcreteType();
        result.addAll(scanForAllocators(type));
      } else {
        Assertions.UNREACHABLE("unexpected instance key type: " + ik.getClass());
      }
    }
    return result;
  }

  /**
   * @param type
   * @return Collection<CGNode>
   */
  private Collection<CGNode> scanForAllocators(IClass type) {
    HashSet<CGNode> result = HashSetFactory.make();
    nodes: for (Iterator<CGNode> it = getCallGraph().iterator(); it.hasNext();) {
      CGNode n = it.next();
      for (Iterator<NewSiteReference> it2 = n.iterateNewSites(); it2.hasNext();) {
        NewSiteReference site = it2.next();
        IClass klass = getCallGraph().getClassHierarchy().lookupClass(site.getDeclaredType());
        if (klass != null && type.equals(klass)) {
          result.add(n);
          continue nodes;
        }
      }
    }
    return result;
  }

  /**
   * Create the analysis domain. A domain-element consists of an
   * object-identifier and an object-state. side effect: populate objectStateMap
   * map
   * 
   * @param instances
   *          - set of tracked instances
   */
  protected void initializeDomain(Collection<InstanceKey> instances) {
    setDomain(new BaseTypeStateDomain(getDFA(), getOptions()));
  }

  protected InstanceBatchIterator makeBatchIterator(Collection<InstanceKey> allInstances) {
    return InstanceBatchIterator.makeNoSeparation(allInstances);
  }

  protected boolean supportsWitnessGeneration() {
    return false;
  }

}
