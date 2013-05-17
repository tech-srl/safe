/*******************************************************************************
 * Copyright (c) 2002-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.reporting.message;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class UnknownLocation extends Location {
  private final static UnknownLocation theInstance = new UnknownLocation();

  public static UnknownLocation location() {
    return theInstance;
  }

  public boolean equals(Object other) {
    return (other == theInstance);
  }

  public int hashCode() {
    return 1;
  }

  public String getByteCodeLocation() {
    return toString();
  }

  public String getSourceLocation() {
    return toString();
  }

  public String getLocationClass() {
    return Location.UNKNOWN_CLASS;
  }

  public String getSourceFileName() {
    return "";
  }

  public int getLocationLineNumber() {
    return Location.UNKNOWN_LINE_NUMBER;
  }

  public int getByteCodeIndex() {
    return Location.UNKNOWN_BYTECODE_INDEX;
  }

  public Object getAdditionalInformation() {
    return null;
  }

  public boolean isFieldMember() {
    return false;
  }

  public boolean isMethodMember() {
    return false;
  }

  public String toString() {
    return "UnknownLocation";
  }
}
