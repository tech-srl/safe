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
public class SCCPTopValue extends SCCPValue {
  private static final SCCPTopValue theInstance = new SCCPTopValue();

  public static SCCPTopValue instance() {
    return theInstance;
  }

  private SCCPTopValue() {
    super("_internal_TOP");
  }

  public SCCPValue copy() {
    return theInstance;
  }

  public boolean equals(Object other) {
    return other == theInstance;
  }

  public int hashCode() {
    return 0;
  }

  public SCCPValue join(SCCPValue other) {
    assert other != null : "cannot join with null";

    return theInstance;
  }

}