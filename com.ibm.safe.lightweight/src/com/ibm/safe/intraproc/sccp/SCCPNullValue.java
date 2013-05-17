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
public class SCCPNullValue extends SCCPValue {
  private static final SCCPNullValue theInstance = new SCCPNullValue();

  public static SCCPNullValue instance() {
    return theInstance;
  }

  private SCCPNullValue() {
    super(null);
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

}