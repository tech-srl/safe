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
public class MethodLocation extends Location {
  private final String className;

  private final String methodName;

  private final int lineNumber;

  private final int bcIndex;

  /**
   * this has some ugly hacking to extract class-name and method-name from a
   * signature string.
   * 
   * @TODO: This is horrendous. fix this. [EY]
   */
  protected MethodLocation(final String className, final String methodSelector, final int line, final int bcIndex) {
    this.className = className;
    this.methodName = methodSelector;
    this.lineNumber = line;
    this.bcIndex = bcIndex;
  }

  protected MethodLocation(final String className, final String methodSelector, final int line) {
    this(className, methodSelector, line, Location.UNKNOWN_BYTECODE_INDEX);
  }

  protected MethodLocation(final String className, final String methodSelector) {
    this(className, methodSelector, Location.UNKNOWN_LINE_NUMBER, Location.UNKNOWN_BYTECODE_INDEX);
  }

  public boolean equals(Object other) {
    if (!(other instanceof MethodLocation)) {
      return false;
    }
    MethodLocation otherMethodLocation = (MethodLocation) other;
    return (className.equals(otherMethodLocation.className) && methodName.equals(otherMethodLocation.methodName) && (lineNumber == otherMethodLocation.lineNumber));
  }

  public int hashCode() {
    return className.hashCode() + methodName.hashCode() + lineNumber;
  }

  public String getByteCodeLocation() {
    return this.methodName;
  }

  public String getSourceLocation() {
    return SignatureUtils.getMethodSignature(this, false /* withReturnType */);
  }

  public String getLocationClass() {
    return className;
  }

  public String getLocationMethodSignature() {
    return methodName;
  }

  public Object getAdditionalInformation() {
    return null;
  }

  public int getLocationLineNumber() {
    return this.lineNumber;
  }

  public int getByteCodeIndex() {
    return this.bcIndex;
  }

  public boolean isMethodMember() {
    return true;
  }

  public boolean isFieldMember() {
    return false;
  }

  public String toString() {
    return "MethodLocation: " + className + "." + methodName + ":" + lineNumber;
  }
}