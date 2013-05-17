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
 * @author Marco Pistoia
 */
public class FieldLocation extends Location {
  private final String className;

  private final String fieldName;

  protected FieldLocation(final String className, final String fieldName) {
    this.className = className;
    this.fieldName = fieldName;
  }

  public boolean equals(Object other) {
    if (!(other instanceof FieldLocation)) {
      return false;
    }
    FieldLocation otherFieldLocation = (FieldLocation) other;
    return (className.equals(otherFieldLocation.className) && fieldName.equals(otherFieldLocation.fieldName));
  }

  public int hashCode() {
    return className.hashCode() ^ fieldName.hashCode();
  }

  public String getLocationClass() {
    return className;
  }

  public String getByteCodeLocation() {
    return this.fieldName;
  }

  public String getSourceLocation() {
    return this.fieldName;
  }

  public Object getAdditionalInformation() {
    return null;
  }

  public int getLocationLineNumber() {
    return Location.UNKNOWN_LINE_NUMBER;
  }

  public int getByteCodeIndex() {
    return Location.UNKNOWN_BYTECODE_INDEX;
  }

  public boolean isFieldMember() {
    return true;
  }

  public boolean isMethodMember() {
    return false;
  }

  public String toString() {
    return "FieldLocation: " + className + "." + fieldName;
  }
}