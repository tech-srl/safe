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
/*
 * Created on Dec 23, 2004
 */
package com.ibm.safe.typestate.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.Factoid;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.wala.dataflow.IFDS.PathEdge;
import com.ibm.wala.dataflow.IFDS.TabulationDomain;
import com.ibm.wala.dataflow.IFDS.TabulationSolver;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.intset.MutableMapping;

/**
 * The domain of factoids which the IFDS solver operates on.
 * 
 * Currently, a typestate domain assumes a single underlying typestate property.
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public abstract class TypeStateDomain extends MutableMapping<Factoid> implements TabulationDomain<Factoid, BasicBlockInContext<IExplodedBasicBlock>> {

  /**
   * underlying typestate automaton
   */
  private final ITypeStateDFA dfa;

  /**
   * Set<TypeStateMessage>: messages created for the domain TODO: need to
   * refactor this out of here, this should be part of the result [EY]
   */
  private final Set<TypeStateMessage> messages = HashSetFactory.make();

  /**
   * A set of tracked instance keys (of the relevant type)
   */
  protected Collection<InstanceKey> trackedInstances = HashSetFactory.make();

  /**
   * governing type state options
   */
  private final TypeStateOptions options;

  /**
   * @param dfa
   *          underlying typestate automaton
   */
  public TypeStateDomain(ITypeStateDFA dfa, TypeStateOptions options) {
    this.dfa = dfa;
    this.options = options;
    add(AbstractWholeProgramSolver.DUMMY_ZERO);
  }

  /**
   * Given the input factoid, return the index of the factoid if the input
   * factoid undergoes a typestate transition to the successor state, but all
   * other information in the factoid remains constant.
   */
  public abstract int getIndexForStateDelta(BaseFactoid inputFact, IDFAState succState);

  /**
   * @param ik
   * @return the integer index identifying the initial state for an instance
   */
  public abstract int getIndexForInitialState(InstanceKey ik);

  /**
   * TODO: this should be refactored .. a separate type should track messages.
   * 
   * @param message
   * @return true if we've reached the maximum number of messages to report, as
   *         indicated in the governing analysis options. false otherwise.
   * @throws PropertiesException
   */
  public boolean addMessage(TypeStateMessage message) {
    messages.add(message);
    try {
      return (messages.size() >= options.getMaxFindingsPerRule());
    } catch (PropertiesException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public Set<TypeStateMessage> getMessages() {
    return messages;
  }

  public Collection<InstanceKey> getAcceptingInstances() {
    Collection<InstanceKey> result = HashSetFactory.make();
    for (Iterator<TypeStateMessage> it = messages.iterator(); it.hasNext();) {
      TypeStateMessage msg = it.next();
      result.add(msg.getInstance());
    }
    return result;
  }

  /**
   * For each message, add a witness, which is a path in the exploded supergraph
   * which demonstrates the reported problem
   */
  public void populateWitnesses(TabulationSolver solver) {
    for (Iterator<TypeStateMessage> it = messages.iterator(); it.hasNext();) {
      TypeStateMessage m = it.next();
      // m.populateWitness(this, solver);
      // @TODO: NYI
    }
  }

  /**
   * @return Returns the automaton.
   */
  public ITypeStateDFA getDFA() {
    return dfa;
  }

  public TypeStateProperty getDFAAsProperty() {
    assert (getDFA() instanceof TypeStateProperty);
    return (TypeStateProperty) getDFA();
  }

  /**
   * @param caller
   * @param call
   * @return true iff we've already reported a message for a particular call
   */
  public boolean hasMessage(CGNode caller, SSAInvokeInstruction call) {
    for (Iterator<TypeStateMessage> it = messages.iterator(); it.hasNext();) {
      TypeStateMessage m = it.next();
      if (m.getCaller().equals(caller) && m.getInstruction().equals(call)) {
        return true;
      }
    }
    return false;
  }

  public boolean hasPriorityOver(PathEdge<BasicBlockInContext<IExplodedBasicBlock>> p1, PathEdge<BasicBlockInContext<IExplodedBasicBlock>> p2) {
    // TODO Auto-generated method stub
    return false;
  }

}