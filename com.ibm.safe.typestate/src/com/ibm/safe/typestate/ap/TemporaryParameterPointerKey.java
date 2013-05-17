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

package com.ibm.safe.typestate.ap;

import com.ibm.wala.ipa.callgraph.propagation.AbstractPointerKey;

/**
 * @author sfink
 * 
 * This is a temporary local, used for transient copies of formal to actual
 * parameters
 * 
 * By convention, the return value from a call is given parameter number -1
 * 
 */
public class TemporaryParameterPointerKey extends AbstractPointerKey {

  private final int parameterNumber;

  public TemporaryParameterPointerKey(int p) {
    this.parameterNumber = p;

  }

  public static TemporaryParameterPointerKey make(int p) {
    return new TemporaryParameterPointerKey(p);
  }

  /**
   * @return a pointer key which represent the result of a function call
   */
  public static TemporaryParameterPointerKey makeReturnValue() {
    return new TemporaryParameterPointerKey(-1);
  }

  public final boolean equals(Object obj) {
    if (obj instanceof TemporaryParameterPointerKey) {
      TemporaryParameterPointerKey other = (TemporaryParameterPointerKey) obj;
      return parameterNumber == other.parameterNumber;
    } else {
      return false;
    }
  }

  public final int hashCode() {
    return parameterNumber * 4621;
  }

  public String toString() {
    if (parameterNumber == -1) {
      return "[return value]";
    } else {
      return "[parameter " + parameterNumber + "]";
    }
  }
}
