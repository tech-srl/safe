/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.safe.intraproc.sccp;

import java.io.FileWriter;
import java.util.Iterator;

import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class ExplodedCFGDotWriter {

  public static void write(String fileName, ExplodedControlFlowGraph cfg) throws IllegalArgumentException {
    if (cfg == null) {
      throw new IllegalArgumentException("cfg cannot be null");
    }
    StringBuffer result = new StringBuffer();
    result.append(dotOutput(cfg));
    try {
      FileWriter fw = new FileWriter(fileName, false);
      fw.write(result.toString());
      fw.close();
    } catch (Exception e) {
      throw new RuntimeException("Error writing dot file");
    }
  }

  public static void write(String fileName, SSACFG cfg) {
    if (cfg == null) {
      throw new IllegalArgumentException("cfg cannot be null");
    }
    StringBuffer result = new StringBuffer();
    result.append(dotOutput(cfg));
    try {
      FileWriter fw = new FileWriter(fileName, false);
      fw.write(result.toString());
      fw.close();
    } catch (Exception e) {
      throw new RuntimeException("Error writing dot file");
    }
  }

  private static StringBuffer dotOutput(ExplodedControlFlowGraph cfg) {
    StringBuffer result = new StringBuffer("digraph \"ExpandedControlFlowGraph:\" {\n");
    result.append("center=true;fontsize=12;node [fontsize=12];edge [fontsize=12]; \n");

    // create nodes for basic-blocks
    for (Iterator it = cfg.iterator(); it.hasNext();) {

      IExplodedBasicBlock bb = (IExplodedBasicBlock) it.next();
      result.append(dotOutput(bb));
      if (bb.isEntryBlock()) {
        result.append(" [color=green]\n");
      } else if (bb.isExitBlock()) {
        result.append(" [color=red]\n");
      } else {
        result.append("\n");
      }

    }
    // create edges
    for (Iterator it = cfg.iterator(); it.hasNext();) {
      IExplodedBasicBlock bb = (IExplodedBasicBlock) it.next();
      for (Iterator succIt = cfg.getSuccNodes(bb); succIt.hasNext();) {
        IExplodedBasicBlock succ = (IExplodedBasicBlock) succIt.next();
        result.append(dotOutput(bb));
        result.append(" -> ");
        result.append(dotOutput(succ));

        result.append("\n");
      }
    }
    // close digraph
    result.append("}");
    return result;
  }

  private static StringBuffer dotOutput(SSACFG cfg) {
    StringBuffer result = new StringBuffer("digraph \"ControlFlowGraph:\" {\n");
    result.append("center=true;fontsize=12;node [fontsize=12];edge [fontsize=12]; \n");

    // create nodes for basic-blocks
    for (Iterator it = cfg.iterator(); it.hasNext();) {

      BasicBlock bb = (BasicBlock) it.next();
      result.append(dotOutput(bb));
      if (bb.isEntryBlock()) {
        result.append(" [color=green]\n");
      } else if (bb.isExitBlock()) {
        result.append(" [color=red]\n");
      } else {
        result.append("\n");
      }

    }
    // create edges
    for (Iterator it = cfg.iterator(); it.hasNext();) {
      BasicBlock bb = (BasicBlock) it.next();
      for (Iterator succIt = cfg.getSuccNodes(bb); succIt.hasNext();) {
        BasicBlock succ = (BasicBlock) succIt.next();
        result.append(dotOutput(bb));
        result.append(" -> ");
        result.append(dotOutput(succ));
        result.append("\n");
      }
    }
    // close digraph
    result.append("}");
    return result;
  }

  private static StringBuffer dotOutput(BasicBlock bb) {
    StringBuffer result = new StringBuffer();
    result.append("\"");
    result.append(bb.getNumber());
    result.append("\"");
    return result;
  }

  private static StringBuffer dotOutput(IExplodedBasicBlock bb) {
    StringBuffer result = new StringBuffer();
    SSAInstruction inst = bb.getInstruction();
    result.append("\"");
    result.append(bb.getNumber());
    result.append("-");

    for (Iterator<SSAPhiInstruction> phiIt = bb.iteratePhis(); phiIt.hasNext();) {
      SSAPhiInstruction currPhi = phiIt.next();
      result.append(currPhi);
      result.append("\\n");
    }

    result.append(inst);
    result.append("\\n");

    for (Iterator<SSAPiInstruction> piIt = bb.iteratePis(); piIt.hasNext();) {
      SSAPiInstruction currPi = piIt.next();
      result.append(currPi);
      result.append("\\n");
    }

    result.append("\"");
    return result;
  }

}