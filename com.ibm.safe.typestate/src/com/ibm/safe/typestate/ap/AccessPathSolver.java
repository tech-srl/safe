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

package com.ibm.safe.typestate.ap;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.accesspath.AccessPathDictionary;
import com.ibm.safe.accesspath.AccessPathSetTransformers;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.typestate.core.BenignOracle;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.metrics.TypeStateMetrics;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.quad.QuadSolver;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AbstractLocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder.TypedPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstanceofInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.CollectionFilter;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Iterator2Collection;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphReachability;
import com.ibm.wala.util.graph.GraphSlicer;
import com.ibm.wala.util.graph.impl.GraphInverter;
import com.ibm.wala.util.graph.traverse.BFSIterator;
import com.ibm.wala.util.graph.traverse.BoundedBFSIterator;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * AccessPathSolver integrating pointer-information into typestate domain
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public abstract class AccessPathSolver extends QuadSolver {
  /**
   * utility path-set transformer
   */
  private AccessPathSetTransformers apsTransformer;

  /**
   * call graph reachability analysis
   */
  private GraphReachability<CGNode> reach;

  private final AccessPathDictionary APDictionary;

  /**
   * Instantiate a new AccessPathsSafeSolver
   * 
   * @param cg -
   *            underlying callgraph
   * @param pointerAnalysis -
   *            results of pointer-analysis
   * @param dfa -
   *            automaton of typestate property to be verified
   * @param warnings -
   *            collector of produced warnings
   */
  public AccessPathSolver(AnalysisOptions domoOptions, CallGraph cg, GraphReachability<CGNode> reach,
      PointerAnalysis pointerAnalysis, ITypeStateDFA dfa, TypeStateOptions options, AccessPathSetTransformers apst,
      ILiveObjectAnalysis live, BenignOracle ora, TypeStateMetrics metrics, IReporter reporter, TraceReporter traceReporter,
      IMergeFunctionFactory mergeFactory) {
    super(domoOptions, cg, pointerAnalysis, dfa, options, live, ora, metrics, reporter, traceReporter, mergeFactory);
    apsTransformer = apst;
    this.reach = reach;
    this.APDictionary = apst.getAPDictionary();
  }

  /**
   * @return Returns the apsTransformer.
   */
  protected AccessPathSetTransformers getApsTransformer() {
    return apsTransformer;
  }

  /**
   * @return Returns the reach.
   */
  protected GraphReachability<CGNode> getReach() {
    return reach;
  }

  /**
   * compute the set of nodes in the supergraph which the IFDS solver cannot
   * ignore
   * 
   * @param instances
   *            Set <InstanceKey>, interesting instances
   * @return set of nodes which lie on some path from the root method to an
   *         event which changes state relevant to the IFDS solver, in which
   *         some interesting instance is live.
   * @throws WalaException
   * @throws WalaException
   * @throws PropertiesException
   */
  @SuppressWarnings("unused")
  protected Collection<CGNode> computeNodesThatMatter(final OrdinalSet<InstanceKey> instances) throws WalaException,
      PropertiesException {

    if (getOptions().shouldSliceSupergraph()) {

      Collection<CGNode> eventNodes = scanForEventNodes(instances);
      Collection<CGNode> result = computeNodesThatDirectlyMatter(instances, getLiveObjectAnalysis(), getCallGraph(),
          getHeapGraph(), getDFA(), getOptions().getAccessPathKLimit());

      result = getMinimumSpanningTree(getCallGraph(), getCallGraph().getFakeRootNode(), result);
      if (DEBUG_LEVEL > 0) {
        Trace.printCollection("tree " + result.size(), result);
      }
      addAllCalleesOfEventSites(result, eventNodes);

      return result;
    } else {
      return Iterator2Collection.toList(getCallGraph().iterator());
    }
  }

  /**
   * compute the set of nodes in the supergraph which the IFDS solver cannot
   * ignore
   * 
   * @param instances
   *            Set <InstanceKey>, interesting instances
   * @return set of nodes which lie on some path from the root method to an
   *         event which changes state relevant to the IFDS solver, in which
   *         some interesting instance is live.
   * @throws WalaException
   */
  @SuppressWarnings("unused")
  public static Collection<CGNode> computeNodesThatDirectlyMatter(final OrdinalSet<InstanceKey> instances,
      ILiveObjectAnalysis live, CallGraph callGraph, HeapGraph heapGraph, ITypeStateDFA dfa, int kLimit) throws WalaException {
    Assertions.productionAssertion(live != null, "expected non-null live object analysis in order to slice supergraph");

    if (DEBUG_LEVEL > 0) {
      Trace.println("Instances: ");
      Trace.println(instances.toString());
      Trace.println("Before slice: " + callGraph.getNumberOfNodes());
    }

    Collection<CGNode> relevantNodes = computeRelevantNodes(instances, heapGraph, callGraph, dfa, kLimit);
    if (DEBUG_LEVEL > 0) {
      Trace.printCollection("relevant nodes " + relevantNodes.size(), relevantNodes);
    }

    // prune the call graph to include only nodes in which some interesting
    // instance is live.
    Predicate<CGNode> liveFilter = makeLiveNodeFilter(instances, live);
    Graph<CGNode> pruned = GraphSlicer.prune(callGraph, liveFilter);

    if (DEBUG_LEVEL > 0) {
      Trace.printCollection("pruned by liveness " + pruned.getNumberOfNodes(), Iterator2Collection.toList(pruned.iterator()));
    }

    // compute the set of nodes in the pruned graph from which some relevant
    // node is reachable
    Collection<CGNode> result = GraphSlicer.slice(pruned, new CollectionFilter(relevantNodes));
    if (DEBUG_LEVEL > 0) {
      Trace.printCollection("slice " + result.size(), result);
    }

    return result;
  }

  /**
   * A CGNode n is "relevant" if some local pointer in n may (transitively)
   * point to an interesting instance in <= k hops, if we're doing a k-bounded
   * AP solver.
   * 
   * The logic behind this is complex, but I believe correct. I have a short
   * writeup of the justification [SJF].
   * 
   * TODO: this is not aggressive enough.
   * 
   * TODO: can we steal this logic for the base solver?
   */
  private static Collection<CGNode> computeRelevantNodes(OrdinalSet<InstanceKey> instances, HeapGraph heapGraph, CallGraph cg,
      ITypeStateDFA dfa, int kLimit) {

    Iterator<Object> traverse = kLimit > 0 ? new BoundedBFSIterator<Object>(GraphInverter.invert(heapGraph), OrdinalSet
        .toCollection(instances).iterator(), kLimit) : new BFSIterator<Object>(GraphInverter.invert(heapGraph), OrdinalSet
        .toCollection(instances).iterator());
    Collection reached = Iterator2Collection.toSet(traverse);

    Set<CGNode> result = HashSetFactory.make();
    for (Iterator it = reached.iterator(); it.hasNext();) {
      Object node = it.next();
      if (heapGraph.getPredNodeCount(node) == 0) {
        if (node instanceof StaticFieldKey) {
          continue;
        } else {
          if (node instanceof AbstractLocalPointerKey) {
            AbstractLocalPointerKey local = (AbstractLocalPointerKey) node;
            if (localIsRelevant(local, cg, dfa)) {
              result.add(local.getNode());
            }
          } else if (node instanceof TypedPointerKey) {
            TypedPointerKey t = (TypedPointerKey) node;
            PointerKey b = t.getBase();
            if (b instanceof AbstractLocalPointerKey) {
              AbstractLocalPointerKey local = (AbstractLocalPointerKey) b;
              if (localIsRelevant(local, cg, dfa)) {
                result.add(local.getNode());
              }
            } else {
              Assertions.UNREACHABLE("unexpected: " + b.getClass() + " " + b);
            }
          } else {
            Assertions.UNREACHABLE("unexpected: " + node.getClass() + " " + node);
          }
        }
      }
    }
    return result;
  }

  /**
   * TODO: enhance this logic
   * 
   * @param local
   * @return true iff the fact that local transitively points to an interesting
   *         instance means that local.getNode() must be processed by IFDS
   */
  private static boolean localIsRelevant(AbstractLocalPointerKey local, CallGraph cg, ITypeStateDFA dfa) {
    CGNode n = local.getNode();
    if (dfa.receives(n.getMethod())) {
      return true;
    }
    if (local instanceof LocalPointerKey) {
      LocalPointerKey lpk = (LocalPointerKey) local;
      DefUse du = n.getDU();
      if (du == null) {
        // something went wrong. terribly wrong.
        return true;
      } else {
        for (Iterator<SSAInstruction> it = du.getUses(lpk.getValueNumber()); it.hasNext();) {
          SSAInstruction s = it.next();
          if (s instanceof SSAInvokeInstruction) {
            // a use in a call is not enough to make the local relevant. the
            // call is
            // only relevant if something reachable from the call makes it
            // relevant.
            // so ignore it for now. if some callee from here is relevant, the
            // calling
            // logic will pick up this node since the callee is reachable from
            // here.
            continue;
          } else if (s instanceof SSAGetInstruction) {
            SSAGetInstruction g = (SSAGetInstruction) s;
            if (g.getDeclaredFieldType().isPrimitiveType()) {
              // just a load of a primitive field. not interesting
              continue;
            } else {
              // a load of a reference field. could be interesting.
              // TODO!!!: enhance this logic to check if this particular field
              // is relevant,
              // by checking the set of transitive fields
              return true;
            }
          } else if (s instanceof SSAPutInstruction) {
            SSAPutInstruction p = (SSAPutInstruction) s;
            if (p.getDeclaredFieldType().isPrimitiveType()) {
              // just a load of a primitive field. not interesting
              continue;
            } else {
              // a store of a reference field. could be interesting.
              // TODO!!!: enhance this logic to check if this particular field
              // is relevant,
              // by checking the set of transitive fields
              return true;
            }
          } else if (s instanceof SSAReturnInstruction || s instanceof SSAArrayStoreInstruction) {
            // always interesting to access paths
            return true;
          } else if (s instanceof SSAInstanceofInstruction || s instanceof SSACheckCastInstruction
              || s instanceof SSAConditionalBranchInstruction || s instanceof SSAPhiInstruction
              || s instanceof SSAMonitorInstruction) {
            // never interesting to access paths and typestate by themselves
            continue;
          } else {
            // unexpected use
            Assertions.UNREACHABLE("unexpected " + s.toString());
            return true;
          }
        }
        return false;
      }
    } else {
      // something exotic.
      return true;
    }
  }

  /**
   * @return Returns the heapGraph.
   */
  protected HeapGraph getHeapGraph() {
    return getPointerAnalysis().getHeapGraph();
  }

  /**
   * @return Returns the aPDictionary.
   */
  protected AccessPathDictionary getAPDictionary() {
    return APDictionary;
  }
}
