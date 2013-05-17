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
package com.ibm.safe.typestate.unique;

import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

/**
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 * 
 *         A unique factoid maintains a bit representing whether an instance is
 *         "unique" (meaning we've only encountered 1 possible dynamic
 *         allocation).
 */
public class UniqueFactoid extends BaseFactoid {

  private final boolean isUnique;

  public UniqueFactoid(InstanceKey instance, IDFAState state, boolean isUnique) {
    super(instance, state);
    this.isUnique = isUnique;
  }

  public String toString() {
    return "(" + (instance != null ? instance.toString() : "null-instance-key") + "," + state + ","
        + (isUnique ? "unique" : "many") + ")";
  }

  public boolean equals(Object other) {
    if (other == null)
      return false;
    if (!getClass().equals(other.getClass())) {
      return false;
    }
    UniqueFactoid otherUniqueFactoid = (UniqueFactoid) other;

    return (state.equals(otherUniqueFactoid.state) && instance.equals(otherUniqueFactoid.instance) && isUnique == otherUniqueFactoid.isUnique);
  }

  public int hashCode() {
    int result = 31;
    result = result + 9091 * state.hashCode();
    result = result + (instance != null ? 8273 * instance.hashCode() : 0);
    result = result + (isUnique ? 0 : 1);
    return result;
  }

  /**
   * @return Returns the isUnique.
   */
  public boolean isUnique() {
    return isUnique;
  }
}
