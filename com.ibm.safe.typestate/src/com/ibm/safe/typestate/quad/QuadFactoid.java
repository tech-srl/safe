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
package com.ibm.safe.typestate.quad;

import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.typestate.unique.UniqueFactoid;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

/**
 * A quad factoid is a flexible factoid that allows us to simply modify the
 * domain by providing different implementations for the Auxiliary component of
 * the tuple.
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public class QuadFactoid extends UniqueFactoid {

  /**
   * General component of the tuple. Providing different implementations for
   * this components allows us to modify the domain without redefining domain
   * functionality.
   */
  public Auxiliary aux;

  /**
   * @param instance
   * @param state
   * @param aux
   */
  public QuadFactoid(InstanceKey instance, IDFAState state, boolean isUnique, Auxiliary aux) {
    super(instance, state, isUnique);
    this.aux = aux;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return (instance != null ? instance.toString() : "null-instance-key") + "\n" + state + "\n"
        + (isUnique() ? "unique" : "notUnique") + "\n" + aux + "\n";
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (!(other instanceof QuadFactoid)) {
      return false;
    }
    QuadFactoid otherFactoid = (QuadFactoid) other;

    return (super.equals(otherFactoid) && aux.equals(otherFactoid.aux));
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return super.hashCode() + 4813 * aux.hashCode();
  }

}