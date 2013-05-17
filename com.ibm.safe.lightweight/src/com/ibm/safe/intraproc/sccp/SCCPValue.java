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

import com.ibm.wala.util.debug.Assertions;

/**
 * SCCP Value could be TOP, BOTTOM, or any constant object value. Constant
 * values may have any Reference type. Primitive constants are realized as their
 * corresponding reference object constants (e.g., boolean by Boolean objects)
 * 
 * @author Eran Yahav (yahave)
 * 
 */
public abstract class SCCPValue {
  public static final SCCPBottomValue BOTTOM = SCCPBottomValue.instance();

  public static final SCCPTopValue TOP = SCCPTopValue.instance();

  public static final SCCPNullValue NULL = SCCPNullValue.instance();

  protected final Object value;

  /**
   * uses static creation method to allow avoiding creation of multiple
   * null-value objects.
   */
  public static SCCPValue createValue(Object val) {
    if (val == null) {
      return NULL;
    } else if (val instanceof String) {
      return new SCCPStringValue(val);
    } else if (val instanceof Integer) {
      return new SCCPIntegerValue(val);
    } else if (val instanceof Boolean) {
      return new SCCPBooleanValue(val);
    }
    return new SCCPObjectValue(val);
  }

  public static SCCPValue createValue(int val) {
    return new SCCPIntegerValue(new Integer(val));
  }

  public static SCCPValue createValue(boolean val) {
    return new SCCPBooleanValue(Boolean.valueOf(val));
  }

  public abstract SCCPValue copy();

  protected SCCPValue(Object val) {
    value = val;
  }

  protected SCCPValue(Object val, boolean b) {
    value = val;
  }

  public Object getValue() {
    return value;
  }

  public String toString() {
    return (value != null) ? value.toString() : "#null";
  }

  // public abstract SCCPValue join(SCCPValue other);

  public SCCPValue join(SCCPValue other) {
    assert other != null : "cannot join with null";
    if (this.equals(other)) {
      return this.copy();
    } else if (this.equals(BOTTOM)) {
      return other.copy();
    } else if (other.equals(BOTTOM)) {
      return this.copy();
    } else {
      return TOP;
    }
  }

  public boolean equals(Object other) {
    if (!(other instanceof SCCPValue)) {
      return false;
    }
    SCCPValue otherValue = (SCCPValue) other;
    if (value == null) {
      return (otherValue.value == null);
    }
    return value.equals(otherValue.value);
  }

  public SCCPValue evaluateBinaryOp(SCCPValue rhs2) {
    return SCCPValue.TOP;
  }

  public boolean isNullConstant() {
    return (this == NULL);
  }

  public boolean isStringConstant() {
    return (value != null && value instanceof String);
  }

  public boolean isIntConstant() {
    return (value != null && value instanceof Integer);
  }

  public boolean isBooleanConstant() {
    return (value != null && value instanceof Boolean);
  }

  public int hashCode() {
    return (value != null) ? value.hashCode() : 0;
  }
}