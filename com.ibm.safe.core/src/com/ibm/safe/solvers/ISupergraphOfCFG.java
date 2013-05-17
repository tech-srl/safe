/*******************************************************************************
 * Copyright (c) 2002-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.solvers;

import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.dataflow.IFDS.ISupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;

/**
 * A supergraph where every node belongs to a CFG
 * 
 * This would be a mix-in of ISupergraph and CFGProvider, except for the ugly
 * warnings object in CFGProvider API.
 * 
 * TODO: clean up global treatment of warnings. maybe cache them in a
 * thread-local.
 * 
 * @author sfink
 * @author yahave
 */
public interface ISupergraphOfCFG extends ISupergraph<ISSABasicBlock,ControlFlowGraph<SSAInstruction,ISSABasicBlock>> {

  /**
   * @return the unique entry node s_main for the main procedure
   */
  ISSABasicBlock getMainEntry();


  /**
   * @return the unique exit node e_main for the main procedure
   */
  ISSABasicBlock getMainExit();

  public ControlFlowGraph<SSAInstruction,ISSABasicBlock> getCFG(CGNode node);
}
