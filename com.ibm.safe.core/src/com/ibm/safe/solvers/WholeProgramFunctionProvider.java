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
package com.ibm.safe.solvers;

import com.ibm.safe.Factoid;
import com.ibm.safe.ICFGSupergraph;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.dataflow.IFDS.IFlowFunction;
import com.ibm.wala.dataflow.IFDS.IFlowFunctionMap;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.dataflow.IFDS.TabulationDomain;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;

public abstract class WholeProgramFunctionProvider implements IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> {

  protected final CallGraph callGraph;

  protected final PointerAnalysis pointerAnalysis;

  protected final ICFGSupergraph supergraph;

  protected final TabulationDomain<Factoid, BasicBlockInContext<IExplodedBasicBlock>> domain;

  protected WholeProgramFunctionProvider(CallGraph cg, PointerAnalysis pointerAnalysis, ICFGSupergraph supergraph,
      TabulationDomain<Factoid, BasicBlockInContext<IExplodedBasicBlock>> domain) {
    this.callGraph = cg;
    this.pointerAnalysis = pointerAnalysis;
    this.supergraph = supergraph;
    this.domain = domain;
  }

  public abstract IUnaryFlowFunction getCallFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest, BasicBlockInContext<IExplodedBasicBlock> ret);

  public abstract IUnaryFlowFunction getCallNoneToReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest);

  public abstract IUnaryFlowFunction getCallToReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest);

  public abstract IUnaryFlowFunction getNormalFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest);

  public abstract IFlowFunction getReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> call, BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest);

  /**
   * @return Domain of dataflow facts
   */
  public TabulationDomain<Factoid,BasicBlockInContext<IExplodedBasicBlock>> getDomain() {
    return domain;
  }

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
   * @param n
   * @return the CFG for node n
   */
  protected ControlFlowGraph<SSAInstruction,IExplodedBasicBlock> getCFG(CGNode n) {
    return supergraph.getICFG().getCFG(n);
  }

  /**
   * @param b
   * @return the CFG to which basic block b belongs
   */
  protected ControlFlowGraph<SSAInstruction,IExplodedBasicBlock> getCFG(BasicBlockInContext<IExplodedBasicBlock> b) {
    CGNode node = (CGNode) getSupergraph().getProcOf(b);
    return supergraph.getICFG().getCFG(node);
  }

  protected ICFGSupergraph getSupergraph() {
    return supergraph;
  }

}
