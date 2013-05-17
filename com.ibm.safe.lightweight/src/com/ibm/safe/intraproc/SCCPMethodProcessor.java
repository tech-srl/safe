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
package com.ibm.safe.intraproc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.safe.intraproc.sccp.NewSCCPSolver;
import com.ibm.safe.intraproc.sccp.SCCPSolver;
import com.ibm.safe.intraproc.sccp.SCCPValue;
import com.ibm.safe.processors.BaseMethodProcessor;
import com.ibm.safe.processors.InstructionProcessor;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.utils.Trace;
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
public class SCCPMethodProcessor extends BaseMethodProcessor {

  private final static boolean DEBUG = false;

  private static final boolean NEW_SOLVER = true;

  private Set<Object> violations = new HashSet<Object>();

  public SCCPMethodProcessor(IClassHierarchy cha) {
    super(cha);
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

    Map<Integer, SCCPValue> methodConstants = null;

    if (NEW_SOLVER) {
      NewSCCPSolver solver = new NewSCCPSolver(ir);
      solver.solve();
      methodConstants = solver.getConstantValues();
    } else {
      SCCPSolver solver = new SCCPSolver(ir);
      solver.solve();
      methodConstants = solver.getConstantValues();
    }

    for (Iterator<InstructionProcessor> procIt = instructionProcessors.iterator(); procIt.hasNext();) {
      InstructionProcessor processor = procIt.next();
      processor.setup(method, methodConstants, ir);
    }

    for (int i = 0, size = ir.getInstructions().length; i < size; ++i) {
      SSAInstruction curr = ir.getInstructions()[i];
      if (curr == null) {
        continue;
      }

      for (Iterator<InstructionProcessor> procIt = instructionProcessors.iterator(); procIt.hasNext();) {
        InstructionProcessor processor = procIt.next();

        processor.processProlog(curr);
        processor.process(curr, ir.getControlFlowGraph().getProgramCounter(i));
        processor.processEpilog(curr);

        Collection<? extends Message> resultMessages = processor.getResult();
        violations.addAll(resultMessages);
      }
    }
  }

  public void processEpilog(IMethod method) {
    if (DEBUG) {
      Trace.println("-------------------------------------");
      Trace.println("Violations for " + method);
      for (Iterator<Object> it = violations.iterator(); it.hasNext();) {
        Trace.println(it.next());
      }
      Trace.println("-------------------------------------");
    }
  }

  public Object getResult() {
    return violations;
  }

}