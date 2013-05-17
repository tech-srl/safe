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

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.collections.ObjectArrayMapping;

public class ProcessorUtils {

  public static int getLineNumber(IMethod method, IR ir, SSAInstruction inst) {
    ObjectArrayMapping<SSAInstruction> instMapping = new ObjectArrayMapping<SSAInstruction>(ir.getInstructions());
    int index = instMapping.getMappedIndex(inst);
    int pc = ir.getControlFlowGraph().getProgramCounter(index);
    int lineNum = -1;
    lineNum = method.getLineNumber(pc);
    return lineNum;
  }

}
