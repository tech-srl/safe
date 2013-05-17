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
 * Created on Jan 26, 2005
 */
package com.ibm.safe.intraproc.sccp;

import com.ibm.wala.fixpoint.AbstractVariable;
import com.ibm.wala.fixpoint.IVariable;

/**
 * A SCCP variable in the dataflow system.
 */
public class SCCPVariable extends AbstractVariable {

  private int valueNumber;

  private SCCPValue value;

  private final int hash;

  SCCPVariable(int valueNumber, SCCPValue value, int hashCode) {
    assert value != null : "attempted creating SCCPVariable with null value";
    this.valueNumber = valueNumber;
    this.value = value;
    this.hash = hashCode;
  }

  public void copyState(IVariable v) {
    SCCPVariable other = (SCCPVariable) v;
    this.value = other.value;
  }

  /**
   * Returns the value.
   * 
   * @return Object
   */
  public SCCPValue getValue() {
    return value;
  }

  /**
   * Sets the value.
   * 
   * @param value
   *            The value to set
   */
  public void setValue(SCCPValue value) {
    assert value != null : "SCCPVariable value set to null";
    this.value = value;
  }

  public void setValue(Object value) {
    SCCPValue sccpvalue = SCCPValue.createValue(value);
    assert sccpvalue != null : "SCCPVariable value set to null";
    this.value = sccpvalue;
  }

  /**
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return valueNumber + " = " + ((value != null) ? value.toString() : "null");
  }
}