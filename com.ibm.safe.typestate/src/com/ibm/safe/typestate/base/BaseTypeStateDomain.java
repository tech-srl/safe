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
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

/**
 * @author Eran Yahav (yahave)
 * Created on Dec 7, 2004
 */
public class BaseTypeStateDomain extends TypeStateDomain {

  /**
   * @param dfa
   *            governing type state property
   */
  public BaseTypeStateDomain(ITypeStateDFA dfa, TypeStateOptions options) {
    super(dfa, options);
  }

  public int getIndexForInitialState(InstanceKey ik) {
    BaseFactoid f = new BaseFactoid(ik, getDFA().initial());
    return add(f);
  }

  /**
   * return the domain index of a give (instance,state)
   * 
   * @param state -
   *            the automaton state
   * @return the domain index of this object-state tuple
   */
  public int getIndexForStateDelta(BaseFactoid fact, IDFAState state) {
    BaseFactoid f = new BaseFactoid(fact.instance, state);

    return add(f);
  }

  /**
   * no ordering implemented
   * 
   * @see com.ibm.wala.dataflow.IFDS.TabulationDomain#isWeakerThan(int, int)
   */
  public boolean isWeakerThan(int d1, int d2) {
    return false;
  }

}