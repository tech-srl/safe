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
public class SCCPBooleanValue extends SCCPValue {
  protected SCCPBooleanValue(Object val) {
    super(val);
  }

  public Boolean getBooleanValue() {
    return (Boolean) value;
  }

  public SCCPValue copy() {
    return new SCCPBooleanValue(value);
  }

  public SCCPValue evaluateBinaryOp(SCCPBooleanValue rhs2, BinaryOpInstruction.Operator operator) {
    SCCPValue result;
    boolean rhs1Value = this.getBooleanValue().booleanValue();
    boolean rhs2Value = rhs2.getBooleanValue().booleanValue();

    switch (operator) {
    case AND:
      result = SCCPValue.createValue(rhs1Value && rhs2Value);
      break;
    case OR:
      result = SCCPValue.createValue(rhs1Value || rhs2Value);
      break;
    default:
      result = SCCPValue.TOP;
    }
    return result;
  }

  public boolean equals(Object other) {
    if (!(other instanceof SCCPBooleanValue)) {
      return false;
    }
    SCCPBooleanValue otherValue = (SCCPBooleanValue) other;
    return getBooleanValue().equals(otherValue.getBooleanValue());
  }

  public int hashCode() {
    return value.hashCode();
  }

}