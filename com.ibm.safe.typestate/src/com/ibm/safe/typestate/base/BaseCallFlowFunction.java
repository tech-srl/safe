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

import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.typestate.core.AbstractWholeProgramSolver;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.core.TypeStateFunctionProvider;
import com.ibm.safe.typestate.core.TypeStateMessage;
import com.ibm.safe.typestate.core.TypeStateProperty;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.dataflow.IFDS.IReversibleFlowFunction;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * @author Eran Yahav
 * @author sfink
 * 
 *         A Base flow function represents a particular call in the call graph,
 *         which causes a typestate transition according to the typestate
 *         property DFA
 */
public class BaseCallFlowFunction implements IReversibleFlowFunction {

  /**
   * domain of dataflow facts
   */
  private TypeStateDomain domain;

  /**
   * relevant instances being solved for
   */
  private OrdinalSet<InstanceKey> instances;

  /**
   * the DFA edge label corresponding to this callh
   */
  private IEvent automatonLabel;

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
  private BasicBlockInContext<IExplodedBasicBlock> invokeBlock;

  /**
   * the caller node
   */
  private CGNode caller;

  public BaseCallFlowFunction(TypeStateDomain domain, ITypeStateDFA dfa, OrdinalSet<InstanceKey> instances, IEvent event,
      BasicBlockInContext<IExplodedBasicBlock> block, SSAInvokeInstruction invokeInstr, CGNode caller) {
    this.domain = domain;
    this.dfa = dfa;
    this.instances = instances;
    this.automatonLabel = event;
    this.invokeInstr = invokeInstr;
    this.invokeBlock = block;
    this.caller = caller;
  }

  /**
   * NB: This function implementation assumes that a call will change only the
   * state in a factoid, but not auxiliary information.
   * 
   * @param d1
   *          = integer corresponding to an (instance, state) pair
   * @return set of d2 such that (d1,d2) is an edge in this distributive
   *         function's graph representation, or null if there are none
   */
  public SparseIntSet getTargets(int d1) {

    if (BaseFunctionProvider.DEBUG_LEVEL > 1) {
      Trace.println("Flow of " + automatonLabel.getName() + " : " + instances);
      Trace.println("Getting targets for: " + d1 + " with " + automatonLabel.getName());
    }

    // initialize the result flow to be identity
    // SparseIntSet result = SparseIntSet.singleton(d1);
    if (d1 == 0) {
      // universal fact 0 does not gen any new facts
      return SparseIntSet.singleton(0);
    } else {
      BaseFactoid inputFact = (BaseFactoid) domain.getMappedObject(d1);
      // default result is identity flow.
      SparseIntSet result = SparseIntSet.singleton(d1);

      if (BaseFunctionProvider.DEBUG_LEVEL > 1) {
        Trace.println("Got Input Fact: " + inputFact);
      }

      if (getInstances().contains(inputFact.instance)) {
        IDFAState succState = dfa.successor(inputFact.state, automatonLabel);
        if ((!inputFact.state.isAccepting()) && succState.isAccepting()) {
          if (AbstractWholeProgramSolver.NO_LIBRARY_ERRORS && !TypeStateFunctionProvider.nodeInApplication(caller)) {
            // don't report the error.
          } else {
            domain.addMessage(makeMessage(inputFact));
          }
          if (BaseSolver.NO_PROPAGATE_ACCEPT) {
            // don't propagate accepting state ... use old state instead
            succState = inputFact.state;
          }
        }
        int newStateIndex = domain.getIndexForStateDelta(inputFact, succState);
        
        if (BaseFunctionProvider.DEBUG_LEVEL > 1) {
          Trace.println("Got Output Fact: " + succState + " with index " + newStateIndex);
        }
        
        if (strongUpdate(inputFact)) {
          result = SparseIntSet.singleton(newStateIndex);
        } else {
          if (newStateIndex == d1) {
            result = SparseIntSet.singleton(newStateIndex);
          } else {
            result = SparseIntSet.pair(newStateIndex, d1);
          }
        }
      }
      return result;
    }
  }

  private TypeStateMessage makeMessage(BaseFactoid inputFact) {
    return new TypeStateMessage(getDFAAsProperty(), inputFact, caller, invokeBlock, invokeInstr, caller);
  }

  /**
   * Should this function use strong update to kill the inputFact?
   * 
   * Subclasses should override as desired.
   */
  protected boolean strongUpdate(BaseFactoid inputFact) {
    return false;
  }

  /**
   * @param d2
   *          = integer corresponding to an (instance, state) pair
   * @return set of d1 such that (d1,d2) is an edge in this distributive
   *         function's graph representation, or null if there are none
   */
  public SparseIntSet getSources(int d2) {

    if (d2 == 0) {
      return SparseIntSet.singleton(0);
    } else {

      BaseFactoid fact = (BaseFactoid) domain.getMappedObject(d2);

      if (instances.contains(fact.instance)) {
        MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
        if (!strongUpdate(fact)) {
          result.add(d2);
        }
        Set<IDFAState> predStates = dfa.predecessors(fact.state, automatonLabel);
        for (Iterator<IDFAState> it = predStates.iterator(); it.hasNext();) {
          IDFAState state = it.next();
          int prevStateIndex = domain.getIndexForStateDelta(fact, state);
          result.add(prevStateIndex);
        }

        // check for ignored library errors
        if (predStates.isEmpty()) {
          // since no predecessor state, we must have ignored a transition.
          // when we ignore transitions, we use identity. So ...
          result.add(d2);
        }

        return result;
      } else {
        // not a tracked instance. use identity flow
        return SparseIntSet.singleton(d2);
      }

    }
  }

  /**
   * @return Returns the automatonLabel.
   */
  public IEvent getAutomatonLabel() {
    return automatonLabel;
  }

  /**
   * @return Returns the property.
   */
  public TypeStateProperty getDFAAsProperty() {

    assert (getDFA() instanceof TypeStateProperty);

    return (TypeStateProperty) getDFA();
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

  /**
   * @return Returns the instances.
   */
  public OrdinalSet<InstanceKey> getInstances() {
    return instances;
  }

  /**
   * @return Returns the caller.
   */
  public CGNode getCaller() {
    return caller;
  }

  /**
   * @return Returns the invokeBlock.
   */
  public IBasicBlock getInvokeBlock() {
    return invokeBlock;
  }

  /**
   * @return Returns the invokeInstr.
   */
  public SSAInvokeInstruction getInvokeInstr() {
    return invokeInstr;
  }

}