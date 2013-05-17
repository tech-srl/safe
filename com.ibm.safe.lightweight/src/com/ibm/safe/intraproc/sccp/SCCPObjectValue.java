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
package com.ibm.safe.intraproc.sccp;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class SCCPObjectValue extends SCCPValue {
  protected SCCPObjectValue(Object val) {
    super(val);
  }

  public String getStringValue() {
    return (String) value;
  }

  public SCCPValue copy() {
    return new SCCPObjectValue(value);
  }

  public boolean equals(Object other) {
    if (!(other instanceof SCCPObjectValue)) {
      return false;
    }
    SCCPObjectValue otherValue = (SCCPObjectValue) other;
    return value.equals(otherValue.value);
  }

  public int hashCode() {
    return value.hashCode();
  }

}
