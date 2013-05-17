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

import java.util.Collections;
import java.util.Iterator;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.utils.SafeAssertions;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.collections.EmptyIterator;
import com.ibm.wala.util.collections.MapIterator;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.functions.Function;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.intset.IntSet;

/**
 * a supergraph for a single procedure.
 * 
 * @author sfink
 * @author yahave
 * 
 */
public class SingleProcedureSupergraph extends ICFGSupergraph {

  /**
   * the node which this supergraph represents
   */
  private final CGNode node;

  /**
   * control-flow graph which defines this supergraph
   */
  private ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> cfg;

  private BasicBlockInContext<IExplodedBasicBlock> mapBlock(IExplodedBasicBlock b) {
    return new BasicBlockInContext<IExplodedBasicBlock>(node, b);
  }

  private Function mapFunc = new Function() {
    public Object apply(Object object) {
      return mapBlock((IExplodedBasicBlock) object);
    }
  };

  /**
   * @param callGraph
   * @param node
   * @param cfg
   */
  public SingleProcedureSupergraph(CallGraph cg, CGNode node, ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> cfg) {
    super(ExplodedInterproceduralCFG.make(cg), new AnalysisCache());
    this.node = node;
    this.cfg = cfg;
  }

  public boolean isCall(SSAInstruction n) {

    assert (n instanceof IBasicBlock);

    IBasicBlock b = (IBasicBlock) n;
    SSAInstruction[] statements = cfg.getInstructions();

    int lastIndex = b.getLastInstructionIndex();
    if (lastIndex >= 0) {
      if (SafeAssertions.verifyAssertions) {
        if (statements.length <= lastIndex) {
          System.err.println(statements.length);
          System.err.println(cfg);
          assert lastIndex < statements.length : "bad BB " + b + " and CFG for " + node;
        }
      }
      SSAInstruction last = statements[lastIndex];
      return (last instanceof IInvokeInstruction);
    } else {
      return false;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.wala.dataflow.IFDS.ISupergraph#getCalledNodes(java.lang.Object)
   */
  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> getCalledNodes(SSAInstruction call) {
    return EmptyIterator.instance();
  }

  /**
   * In forward problems, a call node will have no normal successors.
   * 
   * @see com.ibm.wala.dataflow.IFDS.ISupergraph#getNormalSuccessors(java.lang.Object)
   */
  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> getNormalSuccessors(BasicBlockInContext<IExplodedBasicBlock> call) {
    return EmptyIterator.instance();
  }

  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> getReturnSites(BasicBlockInContext<IExplodedBasicBlock> call) {
    return getSuccNodes(call);
  }

  public Iterator<Object> getCallSites(BasicBlockInContext<IExplodedBasicBlock> r) {
    Assertions.UNREACHABLE();
    return null;
  }

  public boolean isExit(BasicBlockInContext<IExplodedBasicBlock> n) {
    IBasicBlock b = (IBasicBlock) n;
    return b.isExitBlock();
  }

  public CGNode getProcOf(BasicBlockInContext<IExplodedBasicBlock> n) {
    return node;
  }

  public BasicBlockInContext<IExplodedBasicBlock>[] getEntriesForProcedure(CGNode procedure) {

    assert (procedure.equals(node));

    return new BasicBlockInContext[] { getMainEntry() };
  }

  public int getNumberOfBlocks(CGNode procedure) {
    Assertions.UNREACHABLE();
    return 0;
  }

  public int getLocalBlockNumber(BasicBlockInContext<IExplodedBasicBlock> n) {
    return n.getNumber();
  }

  public BasicBlockInContext<IExplodedBasicBlock> getLocalBlock(CGNode procedure, int i) {
    Assertions.UNREACHABLE();
    return null;
  }

  public BasicBlockInContext<IExplodedBasicBlock> getMainEntry() {
    return mapBlock((IExplodedBasicBlock) cfg.entry());
  }

  public BasicBlockInContext<IExplodedBasicBlock>[] getExitsForProcedure(CGNode n) {
    Assertions.UNREACHABLE();
    return null;
  }

  public Object getMainExit() {
    Assertions.UNREACHABLE();
    return null;
  }

  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> iterator() {
    return new MapIterator(cfg.iterator(), mapFunc);
  }

  public int getNumberOfNodes() {
    return cfg.getNumberOfNodes();
  }

  public boolean containsNode(BasicBlockInContext<IExplodedBasicBlock> N) {
    return cfg.containsNode(N.getDelegate());
  }

  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> getPredNodes(BasicBlockInContext<IExplodedBasicBlock> N) {
    return new MapIterator(cfg.getPredNodes(N.getDelegate()), mapFunc);
  }

  public int getPredNodeCount(BasicBlockInContext<IExplodedBasicBlock> N) {
    return cfg.getPredNodeCount(N.getDelegate());
  }

  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> getSuccNodes(BasicBlockInContext<IExplodedBasicBlock> N) {
    return new MapIterator(cfg.getSuccNodes(N.getDelegate()), mapFunc);
  }

  public int getSuccNodeCount(BasicBlockInContext<IExplodedBasicBlock> N) {
    return cfg.getSuccNodeCount(N.getDelegate());
  }

  public boolean hasEdge(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dst) {
    Assertions.UNREACHABLE();
    return false;
  }

  public int getNumber(BasicBlockInContext<IExplodedBasicBlock> N) {
    return N.getNumber();
  }

  public BasicBlockInContext<IExplodedBasicBlock> getNode(int number) {
    Assertions.UNREACHABLE();
    return null;
  }

  public int getMaxNumber() {
    return cfg.getMaxNumber();
  }

  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> iterateNodes(IntSet s) {
    Assertions.UNREACHABLE();
    return null;
  }

  public IntSet getSuccNodeNumbers(BasicBlockInContext<IExplodedBasicBlock> node) {
    return cfg.getSuccNodeNumbers(node.getDelegate());
  }

  public IntSet getPredNodeNumbers(BasicBlockInContext<IExplodedBasicBlock> node) {
    Assertions.UNREACHABLE();
    return null;
  }

  public ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> getCFG(CGNode node) {
    assert this.node == node;
    return cfg;
  }

  public Graph<CGNode> getProcedureGraph() {
    Assertions.UNREACHABLE();
    return null;
  }

  public Iterator<BasicBlockInContext<IExplodedBasicBlock>> getCallSites(BasicBlockInContext<IExplodedBasicBlock> ret, CGNode callee) {
    return Collections.<BasicBlockInContext<IExplodedBasicBlock>> emptySet().iterator();
  }

  public Iterator<? extends BasicBlockInContext<IExplodedBasicBlock>> getReturnSites(BasicBlockInContext<IExplodedBasicBlock> call,
      CGNode callee) {
    return Collections.<BasicBlockInContext<IExplodedBasicBlock>> emptySet().iterator();
  }

}
