/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.ide.ui;

import com.ibm.wala.dataflow.IFDS.ISupergraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.warnings.WalaException;
import com.ibm.wala.viz.EdgeDecorator;

public class IFDSEdgeDecorator extends EdgeDecorator {

  @Override
  public String getLabel(Object src, Object dest) throws WalaException {
    System.err.println("IFDSEdgeDecorator, getLabel");
    if ((src instanceof BasicBlockInContext<?>) 
      && (dest instanceof BasicBlockInContext<?>)) {
      byte kind = classifyEdge((BasicBlockInContext)src, (BasicBlockInContext)dest);
      if (kind == ISupergraph.CALL_EDGE) {
        return "label=\"C\"";
      } else if (kind == ISupergraph.CALL_TO_RETURN_EDGE) {
        return "label=\"CTR\" color=\"blue\"";
      } else if (kind == ISupergraph.RETURN_EDGE) {
        return "label=\"RET\"";
      }
    }
    return "";
  }

  
  public byte classifyEdge(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest) {
    if (isCall(src)) {
      if (isEntry(dest)) {
        return ISupergraph.CALL_EDGE;
      } else {
        return ISupergraph.CALL_TO_RETURN_EDGE;
      }
    } else if (isExit(src)) {
      return ISupergraph.RETURN_EDGE;
    } else {
      return ISupergraph.OTHER;
    }
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
}
