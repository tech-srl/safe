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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.CompoundPiPolicy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.InstanceOfPiPolicy;
import com.ibm.wala.ssa.NullTestPiPolicy;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPiNodePolicy;
import com.ibm.wala.util.CancelException;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class BaseMethodProcessor implements MethodProcessor {

  protected List<InstructionProcessor> instructionProcessors = new ArrayList<InstructionProcessor>();

  protected final IClassHierarchy cha;

  public BaseMethodProcessor(IClassHierarchy cha) {
    this.cha = cha;
  }

  public void processProlog(IMethod method) {

  }

  public void setup(IClass c, Object context) {

  }

  public void process(IMethod method) throws CancelException {

    if (method.isAbstract() || method.isNative()) {
      return;
    }

    AnalysisOptions options = new AnalysisOptions();
    SSAPiNodePolicy policy = CompoundPiPolicy.createCompoundPiPolicy(InstanceOfPiPolicy.createInstanceOfPiPolicy(), NullTestPiPolicy.createNullTestPiPolicy());
    options.getSSAOptions().setPiNodePolicy(policy);
    IR ir = new AnalysisCache().getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, options.getSSAOptions());
    if (ir == null) {
      return;
    }

    for (Iterator<InstructionProcessor> procIt = instructionProcessors.iterator(); procIt.hasNext();) {
      InstructionProcessor processor = procIt.next();
      processor.setup(method, null, ir);
    }

    for (int i = 0, size = ir.getInstructions().length; i < size; ++i) {
      SSAInstruction curr = ir.getInstructions()[i];
      if (curr == null) {
        continue;
      }

      for (Iterator<InstructionProcessor> procIt = instructionProcessors.iterator(); procIt.hasNext();) {
        InstructionProcessor ip = procIt.next();
        ip.processProlog(curr);
        ip.process(curr, ir.getControlFlowGraph().getProgramCounter(i));
        ip.processEpilog(curr);
      }

    }
  }

  public void processEpilog(IMethod method) {

  }

  public Object getResult() {
    return null;
  }

  public void addInstructionProcessor(InstructionProcessor ip) {
    instructionProcessors.add(ip);
  }
}
