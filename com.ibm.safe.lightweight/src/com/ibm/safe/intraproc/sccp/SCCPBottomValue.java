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
public class SCCPBottomValue extends SCCPValue {
  private static final SCCPBottomValue theInstance = new SCCPBottomValue();

  public static SCCPBottomValue instance() {
    return theInstance;
  }

  private SCCPBottomValue() {
    super("__internal_BOTTOM__", true);
  }

  public SCCPValue copy() {
    return theInstance;
  }

  public String toString() {
    return "Bottom";
  }

  public boolean equals(Object other) {
    return other == theInstance;
  }

  public int hashCode() {
    return 0;
  }

  public SCCPValue join(SCCPValue other) {
    assert other != null : "cannot join with null";
    return other;
  }

}