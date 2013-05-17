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

import com.ibm.safe.ICFGSupergraph;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cfg.ExplodedInterproceduralCFG;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.collections.CollectionFilter;
import com.ibm.wala.util.collections.Iterator2Collection;

/**
 * Note that this class does not really support collapsing nodes for now.
 * 
 * @author sfink
 * @author yahave
 */
public class WholeProgramSupergraph extends ICFGSupergraph {

  public WholeProgramSupergraph(CallGraph cg, AnalysisCache ac) {
    super(ExplodedInterproceduralCFG.make(cg), ac);
  }

  public WholeProgramSupergraph(CallGraph cg, AnalysisCache ac, CollectionFilter<CGNode> collectionFilter) {
    // TODO implmenet filtering
    super(ExplodedInterproceduralCFG.make(cg), ac);
  }

  /**
   * @param src
   *          a call node
   * @return true iff we have sliced away some callee of the node. TODO: looks
   *         like this is expensive, can we improve? [EY]
   */
  public boolean slicedAnyCallee(BasicBlockInContext<IExplodedBasicBlock> src) {
    SSAInvokeInstruction srcInvokeInstr = TypeStateFunctionProvider.getLastCallInstruction(getCFG(src), src);
    CGNode callNode = (CGNode) getProcOf(src);
    int originalCalleeCount = getICFG().getCallGraph().getNumberOfTargets(callNode, srcInvokeInstr.getCallSite());
    int actualCalleeCount = Iterator2Collection.toSet(getCalledNodes(src)).size();
    return originalCalleeCount > actualCalleeCount;
  }

  /**
   * @param b
   * @return the CFG to which basic block b belongs
   */
  public ControlFlowGraph getCFG(BasicBlockInContext<IExplodedBasicBlock> b) {
    CGNode node = (CGNode) getProcOf(b);
    return getICFG().getCFG(node);
  }

}
