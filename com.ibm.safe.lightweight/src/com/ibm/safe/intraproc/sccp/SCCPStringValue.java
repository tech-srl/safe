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

import com.ibm.wala.shrikeBT.BinaryOpInstruction;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class SCCPStringValue extends SCCPValue {
  protected SCCPStringValue(Object val) {
    super(val);
  }

  public String getStringValue() {
    return (String) value;
  }

  public SCCPValue copy() {
    return new SCCPStringValue(value);
  }

  public SCCPValue evaluateBinaryOp(SCCPStringValue rhs2, BinaryOpInstruction.Operator operator) {
    if (operator == BinaryOpInstruction.Operator.ADD) {
      String result = this.getStringValue() + rhs2.getStringValue();
      return SCCPValue.createValue(result);
    } else {
      return SCCPValue.TOP;
    }
  }

  public boolean equals(Object other) {
    if (!(other instanceof SCCPStringValue)) {
      return false;
    }
    SCCPStringValue otherValue = (SCCPStringValue) other;
    return getStringValue().equals(otherValue.getStringValue());
  }

  public int hashCode() {
    return value.hashCode();
  }

}