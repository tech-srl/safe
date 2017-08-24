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
package com.ibm.safe;

import java.util.Iterator;
import java.util.Map;

import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.IFDS.ISupergraph;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.collections.EmptyIterator;
import com.ibm.wala.util.collections.FilterIterator;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.SparseVector;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.intset.IntSet;

/**
 * Forward supergraph induced over an {@link ExplodedInterproceduralCFG}
 * 
 * This should lazily build the supergraph as it is explored.
 * 
 * TODO: promote to WALA?
 * 
 * @author sjfink
 * 
 */
public class ICFGSupergraph implements ISupergraph<BasicBlockInContext<IExplodedBasicBlock>, CGNode> {

  private final AnalysisCache analysisCache;

  private final ExplodedInterproceduralCFG icfg;

  // CGNode -> [<outputs of i'th input>]
  private final Map<CGNode, SparseVector<IntSet>> summaries = HashMapFactory.make();
 
  protected ICFGSupergraph(ExplodedInterproceduralCFG icfg, AnalysisCache cache) {
    this.icfg = icfg;
    this.analysisCache = cache;
  }

  public static ICFGSupergraph make(CallGraph cg, AnalysisCache cache) {
    ICFGSupergraph w = new ICFGSupergraph(ExplodedInterproceduralCFG.make(cg), cache);
    return w;
  }

  public Graph<? extends CGNode> getProcedureGraph() {
    return icfg.getCallGraph();
  }

  public IClassHierarchy getClassHierarchy() {
    return icfg.getCallGraph().getClassHierarchy();
  }

  public IR getIR(IMethod m, Context c) {
    AnalysisOptions options = new AnalysisOptions();
    IR ir = analysisCache.getSSACache().findOrCreateIR(m, c, options.getSSAOptions());
    return ir;
  }

  public IR getIR(CGNode n) {
    AnalysisOptions options = new AnalysisOptions();
    IR ir = analysisCache.getSSACache().findOrCreateIR(n.getMethod(), n.getContext(), options.getSSAOptions());
    return ir;
  }
  
  public Map<CGNode, SparseVector<IntSet>> getSummeries(){
    return summaries;
  }
  
  public byte classifyEdge(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest) {
    if (isCall(src)) {
      if (isEntry(dest)) {
        return CALL_EDGE;
      } else {
        return CALL_TO_RETURN_EDGE;
      }
    } else if (isExit(src)) {
      return RETURN_EDGE;
    } else {
      return OTHER;
    }
  }

  /*
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#getCallSites(java.lang.Object,
   * java.lang.Object)
   */
  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> getCallSites(BasicBlockInContext<IExplodedBasicBlock> r, CGNode callee) {
    return icfg.getCallSites(r, callee);
  }

  public Iterator<? extends BasicBlockInContext<IExplodedBasicBlock>> getCalledNodes(BasicBlockInContext<IExplodedBasicBlock> call) {
    final Predicate<BasicBlockInContext<IExplodedBasicBlock>> isEntryFilter = new Predicate<BasicBlockInContext<IExplodedBasicBlock>>() {
      public boolean test(BasicBlockInContext<IExplodedBasicBlock> o) {
        return o.isEntryBlock();
      }
    };
    return new FilterIterator<BasicBlockInContext<IExplodedBasicBlock>>(getSuccNodes(call), isEntryFilter);
  }

  @SuppressWarnings("unchecked")
  public BasicBlockInContext<IExplodedBasicBlock>[] getEntriesForProcedure(CGNode procedure) {
    return new BasicBlockInContext[] { icfg.getEntry(procedure) };
  }

  @SuppressWarnings("unchecked")
  public BasicBlockInContext<IExplodedBasicBlock>[] getExitsForProcedure(CGNode procedure) {
    return new BasicBlockInContext[] { icfg.getExit(procedure) };
  }

  public BasicBlockInContext<IExplodedBasicBlock> getLocalBlock(CGNode procedure, int i) {
    IExplodedBasicBlock b = icfg.getCFG(procedure).getNode(i);
    return new BasicBlockInContext<IExplodedBasicBlock>(procedure, b);
  }

  public int getLocalBlockNumber(BasicBlockInContext<IExplodedBasicBlock> n) {
    return n.getDelegate().getNumber();
  }


  public BasicBlockInContext<IExplodedBasicBlock> getMainEntry() {
    Assertions.UNREACHABLE();
    return null;
  }

  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> getNormalSuccessors(BasicBlockInContext<IExplodedBasicBlock> call) {
    return EmptyIterator.instance();
  }

  public int getNumberOfBlocks(CGNode procedure) {
    Assertions.UNREACHABLE();
    return 0;
  }

  public CGNode getProcOf(BasicBlockInContext<IExplodedBasicBlock> n) {
    return icfg.getCGNode(n);
  }


  public Iterator<? extends BasicBlockInContext<IExplodedBasicBlock>> getReturnSites(BasicBlockInContext<IExplodedBasicBlock> call,
      CGNode callee) {
    return icfg.getReturnSites(call);
  }

  public boolean isCall(BasicBlockInContext<IExplodedBasicBlock> n) {
    return n.getDelegate().getInstruction() instanceof SSAAbstractInvokeInstruction;
  }

  public boolean isEntry(BasicBlockInContext<IExplodedBasicBlock> n) {
    return n.getDelegate().isEntryBlock();
  }

  public boolean isExit(BasicBlockInContext<IExplodedBasicBlock> n) {
    return n.getDelegate().isExitBlock();
  }

  public boolean isReturn(BasicBlockInContext<IExplodedBasicBlock> n) {
    return icfg.isReturn(n);
  }

  public void removeNodeAndEdges(BasicBlockInContext<IExplodedBasicBlock> N) throws UnsupportedOperationException {
    Assertions.UNREACHABLE();

  }

  public void addNode(BasicBlockInContext<IExplodedBasicBlock> n) {
    Assertions.UNREACHABLE();

  }

  public boolean containsNode(BasicBlockInContext<IExplodedBasicBlock> N) {
    return icfg.containsNode(N);
  }

  public int getNumberOfNodes() {
    return icfg.getNumberOfNodes();
  }

  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> iterator() {
    return icfg.iterator();
  }

  public void removeNode(BasicBlockInContext<IExplodedBasicBlock> n) {
    Assertions.UNREACHABLE();

  }

  public void addEdge(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dst) {
    Assertions.UNREACHABLE();

  }

  public int getPredNodeCount(BasicBlockInContext<IExplodedBasicBlock> N) {
    return icfg.getPredNodeCount(N);
  }

  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> getPredNodes(BasicBlockInContext<IExplodedBasicBlock> N) {
    return icfg.getPredNodes(N);
  }

  public int getSuccNodeCount(BasicBlockInContext<IExplodedBasicBlock> N) {
    return icfg.getSuccNodeCount(N);
  }

  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> getSuccNodes(BasicBlockInContext<IExplodedBasicBlock> N) {
    return icfg.getSuccNodes(N);
  }

  public boolean hasEdge(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dst) {
    return icfg.hasEdge(src, dst);
  }

  public void removeAllIncidentEdges(BasicBlockInContext<IExplodedBasicBlock> node) throws UnsupportedOperationException {
    Assertions.UNREACHABLE();

  }

  public void removeEdge(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dst)
      throws UnsupportedOperationException {
    Assertions.UNREACHABLE();

  }

  public void removeIncomingEdges(BasicBlockInContext<IExplodedBasicBlock> node) throws UnsupportedOperationException {
    Assertions.UNREACHABLE();

  }

  public void removeOutgoingEdges(BasicBlockInContext<IExplodedBasicBlock> node) throws UnsupportedOperationException {
    Assertions.UNREACHABLE();

  }

  public int getMaxNumber() {
    return icfg.getMaxNumber();
  }

  public BasicBlockInContext<IExplodedBasicBlock> getNode(int number) {
    return icfg.getNode(number);
  }

  public int getNumber(BasicBlockInContext<IExplodedBasicBlock> N) {
    return icfg.getNumber(N);
  }

  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> iterateNodes(IntSet s) {
    Assertions.UNREACHABLE();
    return null;
  }

  public IntSet getPredNodeNumbers(BasicBlockInContext<IExplodedBasicBlock> node) {
    return icfg.getPredNodeNumbers(node);
  }

  public IntSet getSuccNodeNumbers(BasicBlockInContext<IExplodedBasicBlock> node) {
    return icfg.getSuccNodeNumbers(node);
  }

  public ControlFlowGraph<SSAInstruction,IExplodedBasicBlock> getCFG(BasicBlockInContext<IExplodedBasicBlock> node) {
    return icfg.getCFG(node);
  }

  public ExplodedInterproceduralCFG getICFG() {
    return icfg;
  }

  @Override
  public String toString() {
    return icfg.toString();
  }

}
