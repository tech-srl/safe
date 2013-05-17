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
package com.ibm.safe.processors;

import java.util.Collection;
import java.util.Map;

import com.ibm.safe.intraproc.sccp.SCCPValue;
import com.ibm.safe.reporting.message.Message;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.ObjectArrayMapping;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public abstract class BaseInstructionProcessor implements InstructionProcessor {
  protected IMethod method;

  protected IR ir;

  protected ObjectArrayMapping<SSAInstruction> instMapping;

  public BaseInstructionProcessor() {
  }

  public void setup(IMethod method, Map<Integer, SCCPValue> context, IR ir) throws CancelException {
    this.method = method;
    this.ir = ir;
    instMapping = new ObjectArrayMapping<SSAInstruction>(ir.getInstructions());
  }

  public abstract void processProlog(SSAInstruction inst);

  public abstract void process(SSAInstruction inst, int bcIndex);

  public abstract void processEpilog(SSAInstruction inst);

  public abstract Collection<? extends Message> getResult();

  protected int getPC(SSAInstruction inst) {
    int index = instMapping.getMappedIndex(inst);
    int pc = ir.getControlFlowGraph().getProgramCounter(index);
    return pc;
  }

  protected int getInstructionIndex(SSAInstruction inst) {
    return instMapping.getMappedIndex(inst);
  }

  public int getLineNumber(SSAInstruction inst) {
    int index = instMapping.getMappedIndex(inst);
    int pc = ir.getControlFlowGraph().getProgramCounter(index);
    int lineNum = -1;
    lineNum = method.getLineNumber(pc);
    return lineNum;
  }

  public boolean instructionInCatchBlock(SSAInstruction inst) {
    IBasicBlock bb = ir.getBasicBlockForInstruction(inst);
    return bb.isCatchBlock();
  }

}