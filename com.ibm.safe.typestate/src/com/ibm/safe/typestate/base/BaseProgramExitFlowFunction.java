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
package com.ibm.safe.typestate.base;

import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.events.IProgramExitEventImpl;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.core.TypeStateMessage;
import com.ibm.safe.typestate.core.TypeStateProperty;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * @author Eran Yahav
 * @author sfink
 */
public class BaseProgramExitFlowFunction implements IUnaryFlowFunction {

  /**
   * domain of dataflow facts
   */
  private TypeStateDomain domain;

  /**
   * information about the type state property
   */
  private ITypeStateDFA dfa;

  /**
   * The call instruction which generates this flow
   */
  private SSAInvokeInstruction invokeInstr;

  /**
   * the basic block of the call instruction
   */
  private IBasicBlock invokeBlock;

  /**
   * the caller node
   */
  private CGNode caller;

  /**
   * If non-null, an object to record tracing information
   */
  private final TraceReporter traceReporter;

  public BaseProgramExitFlowFunction(TypeStateDomain domain, ITypeStateDFA dfa, BasicBlockInContext<IExplodedBasicBlock> block,
      SSAInvokeInstruction invokeInstr, CGNode caller, TraceReporter traceReporter) {
    this.domain = domain;
    this.dfa = dfa;
    this.invokeInstr = invokeInstr;
    this.invokeBlock = block;
    this.caller = caller;
    this.traceReporter = traceReporter;
  }

  /**
   * NB: This function implementation assumes that a call will change only the
   * state in a factoid, but not auxiliary information.
   * 
   * @param d1 =
   *            integer corresponding to an (instance, state) pair
   * @return set of d2 such that (d1,d2) is an edge in this distributive
   *         function's graph representation, or null if there are none
   */
  public SparseIntSet getTargets(int d1) {    
    if (BaseFunctionProvider.DEBUG_LEVEL > 1) {
      Trace.println("Flow of " + IProgramExitEventImpl.singleton().getName());
      Trace.println("Getting targets for: " + d1 + " with " + IProgramExitEventImpl.singleton().getName());
    }

    if (d1 == 0) {
      return SparseIntSet.singleton(0);
    } else {
      BaseFactoid inputFact = (BaseFactoid) domain.getMappedObject(d1);
      if (BaseFunctionProvider.DEBUG_LEVEL > 1) {
        Trace.println("Got Input Fact: " + inputFact);
      }
      IDFAState succState = dfa.successor(inputFact.state, IProgramExitEventImpl.singleton());

      if (getDFA() instanceof TypeStateProperty) {
        // if moved from non-accepting to accepting state, record a message
        // indicating a finding
        boolean transitionToAccept = (!inputFact.state.isAccepting()) && succState.isAccepting();
        if (transitionToAccept) {
          domain
              .addMessage(new TypeStateMessage((TypeStateProperty) getDFA(), inputFact, caller, invokeBlock, invokeInstr, caller));
        }
      } else {
        traceReporter.record(inputFact);
      }

      return SparseIntSet.singleton(0);
    }
  }

  /**
   * @return Returns the property.
   */
  public ITypeStateDFA getDFA() {
    return dfa;
  }

  /**
   * @return Returns the domain.
   */
  public TypeStateDomain getDomain() {
    return domain;
  }
}