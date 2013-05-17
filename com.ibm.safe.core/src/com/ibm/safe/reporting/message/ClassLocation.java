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
public class ClassLocation extends Location {
  private final String className;

  private final int lineNumber;

  protected ClassLocation(final String className, final int lineNumber) {
    this.className = className;
    this.lineNumber = lineNumber;
  }

  public String getLocationClass() {
    return className;
  }

  public boolean equals(Object other) {
    if (!(other instanceof ClassLocation)) {
      return false;
    }
    ClassLocation otherClassLocation = (ClassLocation) other;
    return className.equals(otherClassLocation.className);

  }

  public int hashCode() {
    return className.hashCode();
  }

  public String getByteCodeLocation() {
    return this.className;
  }

  public String getSourceLocation() {
    return SignatureUtils.getClassName(this);
  }

  public int getLocationLineNumber() {
    return this.lineNumber;
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
    return "ClassLocation: " + className + ":" + lineNumber;
  }
}