/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 /*
 * $Id: DFAState.java,v 1.2 2010/10/17 01:20:31 eyahav Exp $
 */
package com.ibm.safe.dfa;

/**
 * State of a DFA
 * @author yahave
 *
 */
public class DFAState implements IDFAState {
  
  protected String name;

  protected static final boolean ACCEPTING_EDEFAULT = false;

  protected boolean accepting;

  public DFAState() {
    super();
  }

  public String getName() {
    return name;
  }

  public void setName(String newName) {
    name = newName;
  }

  
  public boolean isAccepting() {
    return accepting;
  }

  
  public void setAccepting(boolean newAccepting) {
    accepting = newAccepting;
  }

  public String toString() {

    StringBuffer result = new StringBuffer();
    result.append("[name: ");
    result.append(name);
    result.append(", accepting: ");
    result.append(accepting);
    result.append(']');
    return result.toString();
  }

  public boolean equals(final Object rhsObject) {
    if (rhsObject == null)
      return false;
    if (!getClass().equals(rhsObject.getClass()))
      return false;
    final DFAState obj = (DFAState) rhsObject;
    if (this.name == null) {
      return ((this.name == obj.name) && (this.accepting == obj.accepting));
    } else {
      return (this.name.equals(obj.name) && (this.accepting == obj.accepting));
    }
  }

  public int hashCode() {
    return (this.name == null) ? -1 : this.name.hashCode();
  }
} 
