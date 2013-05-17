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

import com.ibm.safe.Factoid;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

/**
 * A pair of (abstract-object-id,abstract-object-state).
 * 
 * @author Eran Yahav (yahave)
 * Created on Dec 6, 2004
 */
public class BaseFactoid implements Factoid {
  /**
   * instance-key in the object-state tuple
   */
  public final InstanceKey instance;

  /**
   * automaton state
   */
  public final IDFAState state;

  /**
   * create a new object-state tuple
   * 
   * @param instance
   * @param state
   */
  public BaseFactoid(InstanceKey instance, IDFAState state) {
    assert (state != null && instance != null);
    this.instance = instance;
    this.state = state;
  }

  /**
   * return a string representation of the tuple
   * 
   * @return human readable string
   */
  public String toString() {
    return "(" + instance.toString() + "," + state.toString() + ")";
  }

  /**
   * is this tuple equal to another object?
   * 
   * @return true if equals, false otherwise
   */
  public boolean equals(Object other) {
    if (other == null)
      return false;
    if (!getClass().equals(other.getClass())) {
      return false;
    }
    BaseFactoid otherObjectStateTuple = (BaseFactoid) other;

    return (state.equals(otherObjectStateTuple.state) && instance.equals(otherObjectStateTuple.instance));
  }

  /**
   * @return tuple hashcode
   */
  public int hashCode() {
    return 9091 * state.hashCode() + instance.hashCode();
  }

}