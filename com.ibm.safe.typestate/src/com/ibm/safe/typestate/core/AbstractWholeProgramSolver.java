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
package com.ibm.safe.typestate.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.ibm.safe.Factoid;
import com.ibm.safe.controller.ISafeSolver;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.options.WholeProgramOptions;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.typestate.merge.EmptyMergeFactory;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.wala.dataflow.IFDS.ISupergraph;
import com.ibm.wala.dataflow.IFDS.TabulationResult;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ide.ui.IFDSExplorer;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.CollectionFilter;
import com.ibm.wala.util.collections.Iterator2Collection;
import com.ibm.wala.util.collections.NonNullSingletonIterator;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.GraphIntegrity;
import com.ibm.wala.util.graph.GraphIntegrity.UnsoundGraphException;
import com.ibm.wala.util.graph.impl.GraphInverter;
import com.ibm.wala.util.graph.traverse.BFSPathFinder;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * Root of the whole-program solver hierarchy.
 * 
 * Also defines debugging and settings flags that are used throughout the solver
 * hierarchy.
 * 
 * @author yahave
 * @author sjfink
 */
public abstract class AbstractWholeProgramSolver implements ISafeSolver {

  protected static final int DEBUG_LEVEL = 0;

  /**
   * fire off an interactive viewer to explore the IFDS result?
   */
  protected static final boolean GUI_DEBUG = false;

  /**
   * When an exception occurs, fire off an interactive viewer to explore the
   * IFDS result?
   */
  protected static final boolean GUI_DEBUG_ON_EXCEPTION = false;

  /**
   * avoid direct transitions to accepting states from library code?
   */
  public static final boolean NO_LIBRARY_ERRORS = true;

  /**
   * should solver attempt to ignore benign statements by using benign-oracle?
   */
  protected static final boolean IGNORE_BENIGN_STATEMENTS = true;

  /**
   * should solver ignore instances created in synthetic reflective methods?
   */
  protected static final boolean IGNORE_REFLECTIVE_SPAWN = true;

  /**
   * A dummy object which represents the "no dataflow facts reach here"
   */
  public final static Factoid DUMMY_ZERO = new Factoid() {
    @Override
    public int hashCode() {
      return 211;
    }

    @Override
    public boolean equals(Object o) {
      return this == o;
    }

    @Override
    public String toString() {
      return "dummy universal dataflow fact";
    }
  };

  /**
   * Governing call graph
   */
  protected final CallGraph callGraph;

  /**
   * Result of pointer analysis
   */
  protected final PointerAnalysis pointerAnalysis;

  /**
   * options relating to the typestate solver
   */
  protected final WholeProgramOptions options;

  /**
   * if non-null, an analysis which helps kill factoids during return flow based
   * on liveness information
   */
  protected final ILiveObjectAnalysis liveObjectAnalysis;

  /**
   * reporting manager
   */
  protected final IReporter reporter;

  /**
   * a factory for merge functions from a given domain
   */
  protected final IMergeFunctionFactory mergeFactory;

  /**
   * Set of nodes that cannot be collapsed
   */
  protected Collection<CGNode> noCollapse;

  public AbstractWholeProgramSolver(CallGraph cg, PointerAnalysis pointerAnalysis, TypeStateOptions options,
      ILiveObjectAnalysis live, IReporter reporter, IMergeFunctionFactory mergeFactory) {
    this.callGraph = cg;
    this.pointerAnalysis = pointerAnalysis;
    this.options = options;
    this.liveObjectAnalysis = live;
    this.reporter = reporter;
    this.mergeFactory = mergeFactory == null ? EmptyMergeFactory.instance() : mergeFactory;
    noCollapse = new HashSet<CGNode>();
  }

  /*
   * does this solver support witness generation?
   */
  abstract protected boolean supportsWitnessGeneration();

  /**
   * @return Returns the callGraph.
   */
  public CallGraph getCallGraph() {
    return callGraph;
  }

  /**
   * @return Returns the pointerAnalysis.
   */
  public PointerAnalysis getPointerAnalysis() {
    return pointerAnalysis;
  }

  /**
   * @return Returns the options.
   */
  protected WholeProgramOptions getOptions() {
    return options;
  }

  /**
   * @return Returns the liveObjectAnalysis.
   */
  public ILiveObjectAnalysis getLiveObjectAnalysis() {
    return liveObjectAnalysis;
  }

  /**
   * @return Returns the reporter.
   */
  public IReporter getReporter() {
    return reporter;
  }

  /**
   * @return Returns the mergeFactory.
   */
  protected IMergeFunctionFactory getMergeFactory() {
    return mergeFactory;
  }

  /**
   * Spawn a GUI helper to explore the IFDS solution
   * 
   * @throws WalaException
   * @throws SetUpException
   * @throws PropertiesException
   */
  protected void launchGuiExplorer(TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Factoid> r)
      throws WalaException, SetUpException, PropertiesException {
    PropertiesManager p = PropertiesManager.initFromMap(new HashMap<String, String>());
    IFDSExplorer.setDotExe(p.getPathValue(CommonProperties.Props.DOT_EXE));
    IFDSExplorer.setGvExe(p.getPathValue(CommonProperties.Props.GHOSTVIEW_EXE));
    IFDSExplorer.viewIFDS(makeStandardIFDSResult(r));
  }

  /**
   * This is needed for solvers that use a custom CFG in the IFDS solution. Such
   * solvers should override this method.
   * 
   * @param r
   * @return a view of r that is based on a "normal" control-flow graph.
   * 
   */
  protected TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode,Factoid> makeStandardIFDSResult(TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Factoid> r) {
    return r;
  }

  /**
   * Build a supergraph.
   * 
   * Subclasses can override as desired.
   * 
   * @param relevantNodes
   *          nodes which should be included in the supergraph
   */
  protected WholeProgramSupergraph buildSupergraph(AnalysisCache ac,Collection<CGNode> relevantNodes) {
    WholeProgramSupergraph supergraph = new WholeProgramSupergraph(getCallGraph(), ac, new CollectionFilter<CGNode>(
        relevantNodes));
    return supergraph;
  }

  /**
   * @return Returns the noCollapse.
   */
  public Collection<CGNode> getNoCollapse() {
    return noCollapse;
  }

  /**
   * TODO: move this somewhere general 
   * TODO: find a good algorithm for this.
   * This algorithm is a quick-and-dirty. Note that this problem is not exactly
   * the same as the classic minimum spanning tree problem, which would have
   * targets = g.nodes. Also note that for now we only prune the supergraph by
   * nodes ... would be better to prune by edges as well.
   * 
   * With a good algorithm, we wouldn't need the "root" parameter; but we use it
   * to drive the greedy heuristic
   * 
   * @param g
   * @param root
   * @param targets
   * @return set of nodes \in g that represent nodes in a minimum spanning tree
   *         that includes all nodes \in targets
   */
  protected Collection<CGNode> getMinimumSpanningTree(Graph<CGNode> g, Object root, Collection<CGNode> targets) {
    HashSet<CGNode> result = new HashSet<CGNode>();
    Graph<CGNode> invG = GraphInverter.invert(g);
    CGNode cgroot = (CGNode) root;
    result.add(cgroot);
    for (Iterator<CGNode> it = targets.iterator(); it.hasNext();) {
      CGNode n = it.next();
      if (!result.contains(n)) {
        // note that we search bfs in the inverted graph, since we'd prefer to
        // search
        // from a single source, in hopes that the width of the bfs tree will be
        // smaller.
        BFSPathFinder<CGNode> bfs = new BFSPathFinder<CGNode>(invG, new NonNullSingletonIterator<CGNode>(n), 
            new CollectionFilter<CGNode>(result));
        List<CGNode> path = bfs.find();
        assert (path != null);
        result.addAll(path);
      }
    }
    return result;
  }

  public boolean isApplicationNode(CGNode n) {
    return n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application);
  }

  /**
   * initialize the no-collapse set to contain all call-graph nodes. At this
   * point, we do not wish to optimize the supergraph by partially collapsing
   * it. Hence, we initialize the no-collapse set to contain all callGraph
   * nodes.
   * 
   * Update: SJF: with one exception ... we should optimize the FakeRootMethod
   * ... no point in tracking stuff through there.
   */
  protected void initializeNoCollapseSet() {
    // currently, avoid collapsing [Eran]
    for (Iterator<CGNode> it = getCallGraph().iterator(); it.hasNext();) {
      CGNode n = it.next();
      // SJF: should enable the following eventually
      // if
      // (!FakeRootMethod.isFakeRootMethod(n.getMethod().getReference()))
      // {
      noCollapse.add(n);
      // }
    }
  }

  /**
   * Perfom some expensive assertion checking on a graph.
   * 
   * @param G
   */
  @SuppressWarnings("unused")
  protected void checkGraph(ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> G) {
    if (DEBUG_LEVEL > 1) {
      try {
        GraphIntegrity.check(G);
      } catch (UnsoundGraphException e) {
        e.printStackTrace();
        Assertions.UNREACHABLE();
      }
    }
  }

  /**
   * compute the set of nodes in the call graph which the IFDS solver cannot
   * ignore.
   * 
   * Note that this set does not have to include any nodes in which no
   * interesting instance is dead. The actual supergraph will be built as a
   * minimal spanning tree in order to reach all nodes that matter.
   * 
   * subclasses should override this as desired
   * 
   * @param instances
   *          Set <InstanceKey>, interesting instances
   * @return set of CGNodes which lie on some path from the root method to an
   *         event which changes state relevant to the IFDS solver.
   * @throws WalaException
   * @throws PropertiesException
   */
  protected Collection<CGNode> computeNodesThatMatter(OrdinalSet<InstanceKey> instances) throws WalaException, PropertiesException {
    // just return all nodes
    return Iterator2Collection.toSet(getCallGraph().iterator());
  }

}