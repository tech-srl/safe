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
package com.ibm.safe.utils;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.collections.ObjectArrayMapping;

public class Utils {

  /**
   * inefficient but effective
   * 
   * @param node
   * @param inst
   * @return
   */
  public static int getInstructionIndex(CGNode node, SSAInstruction inst) {
    IR ir = node.getIR();
    ObjectArrayMapping<SSAInstruction> instMapping = new ObjectArrayMapping<SSAInstruction>(ir.getInstructions());
    int instrIndex = instMapping.getMappedIndex(inst);
    return instrIndex;
  }

}
