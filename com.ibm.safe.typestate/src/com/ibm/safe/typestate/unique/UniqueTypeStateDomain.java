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
 * Created on Dec 7, 2004
 */
package com.ibm.safe.typestate.unique;

import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

/**
 * @author Eran Yahav (yahave)
 * @author sfink
 * 
 * for each <instance,state> pair, we have a "count" of either 1 or 2. 1 means
 * we've allocated exactly one such instance 2 means we've allocated more than
 * one the universal factoid "0" means we've allocated none.
 */
public class UniqueTypeStateDomain extends TypeStateDomain {

  /**
   * @param dfa
   *            governing dfa
   */
  public UniqueTypeStateDomain(ITypeStateDFA dfa, TypeStateOptions options) {
    super(dfa, options);
  }

  /**
   * return the domain index of a give (instance,state)
   * 
   * @param state -
   *            the automaton state
   * @return the domain index of this object-state tuple
   */
  public int getIndexForStateDelta(BaseFactoid fact, IDFAState state) {
    UniqueFactoid uniqueFact = (UniqueFactoid) fact;
    UniqueFactoid f = new UniqueFactoid(fact.instance, state, uniqueFact.isUnique());
    int index = add(f);
    return index;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.TypeStateDomain#getIndexForInitialState(com.ibm.wala.ipa.callgraph.propagation.InstanceKey)
   */
  public int getIndexForInitialState(InstanceKey ik) {
    UniqueFactoid f = new UniqueFactoid(ik, getDFA().initial(), true);
    return add(f);
  }

  /**
   * no order implemented as yet
   * 
   * @see com.ibm.wala.dataflow.IFDS.TabulationDomain#isWeakerThan(int, int)
   */
  public boolean isWeakerThan(int d1, int d2) {
    return false;
  }
}