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
package com.ibm.safe.typestate.rules;

import java.util.Collection;
import java.util.Set;

import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;

/**
 * Generalization of typestate DFA interface; 
 * general enough to support both mining and verification.
 * 
 * @author sfink
 * @author yahave
 */
public interface ITypeStateDFA {

  boolean receives(IMethod method);

  /**
   * for all callers, matchDispatchEvent(caller,sig) must either equal
   * matchDispatchEvent(sig), or be null.
   * 
   * This allows the automaton to filter events; observe only some events but
   * not all.
   * 
   * @return the event matching a dispatch from caller on the signature
   */
  IEvent matchDispatchEvent(CGNode caller, String signature);

  /**
   * @return the event matching a dispatch to signature.
   */
  IEvent matchDispatchEvent(String signature);

  IDFAState successor(IDFAState state, IEvent e);

  Set<IDFAState> predecessors(IDFAState state, IEvent automatonLabel);

  IDFAState initial();

  String getName();

  /**
   * @return Collection<IClass>, the classes this DFA mines for.
   */
  Collection<IClass> getTypes();

  /**
   * Does this DFA observe the program exit event?
   */
  public boolean observesProgramExit();

  /**
   * Does this DFA observe the object death event?
   */
  boolean observesObjectDeath();
}