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

import com.ibm.safe.utils.Trace;
import com.ibm.wala.shrikeBT.BinaryOpInstruction;
import com.ibm.wala.util.debug.Assertions;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class SCCPIntegerValue extends SCCPValue {
  private final static int DEBUG_LEVEL = 0;

  protected SCCPIntegerValue(Object val) {
    super(val);
    Assertions.productionAssertion(val != null);
  }

  public Integer getIntegerValue() {
    return (Integer) value;
  }

  public SCCPValue copy() {
    return new SCCPIntegerValue(value);
  }

  public SCCPValue evaluateBinaryOp(SCCPValue rhs2, BinaryOpInstruction.Operator operator) {

    if (rhs2 == null) {
      return SCCPValue.TOP;
    }

    SCCPIntegerValue rhs2int = (SCCPIntegerValue) rhs2;

    int rhs1Val = this.getIntegerValue().intValue();
    int rhs2Val = rhs2int.getIntegerValue().intValue();
    int result;

    switch (operator) {
    case ADD:
      result = rhs1Val + rhs2Val;
      break;
    case SUB:
      result = rhs1Val - rhs2Val;
      break;
    case MUL:
      result = rhs1Val * rhs2Val;
      break;
    default:
      if (DEBUG_LEVEL > 1) {
        Trace.println("Integer:evaluateBinaryOp --- Unknown Operator --- ResultValue is TOP");
      }
      return SCCPValue.TOP;
    }

    SCCPValue resultValue = SCCPValue.createValue(result);
    if (DEBUG_LEVEL > 1) {
      Trace.println("Integer:evaluateBinaryOp --- ResultValue: " + resultValue);
    }

    return resultValue;
  }

  public boolean equals(Object other) {
    if (!(other instanceof SCCPIntegerValue)) {
      return false;
    }
    SCCPIntegerValue otherValue = (SCCPIntegerValue) other;
    return getIntegerValue().equals(otherValue.getIntegerValue());
  }

  public int hashCode() {
    return value.hashCode();
  }

}