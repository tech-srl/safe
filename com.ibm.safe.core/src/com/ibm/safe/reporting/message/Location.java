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

import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;

/**
 * Location of a warning or an error message
 * 
 * @author Eran Yahav (yahave)
 * 
 * TODO: to be consolidated with InstructionLocation.
 */
public abstract class Location {

  public static final int UNKNOWN_LINE_NUMBER = -1, UNKNOWN_BYTECODE_INDEX = -1;

  public static final String UNKNOWN_METHOD = "unknown method";

  public static final String UNKNOWN_CLASS = "unknown class";

  public static final String UNKNOWN_FIELD = "unknown field";

  public static ClassLocation createClassLocation(final String className, final int line) {
    return new ClassLocation(className, line);
  }

  public static MethodLocation createMethodLocation(final String signature, final int lineNumber) {
    final int dotIndex = signature.indexOf('.');
    if (dotIndex == -1) {
      throw new AssertionError("We could not extract class name from signature: " + signature);
    }
    return new MethodLocation(signature.substring(0, dotIndex), signature.substring(dotIndex + 1), lineNumber);
  }

  public static MethodLocation createMethodLocation(final TypeName typeName, final String selector) {
    return new MethodLocation(typeName.toString(), selector);
  }

  public static MethodLocation createMethodLocation(final TypeName typeName, final Selector selector,
      final int lineNumber, final int bcIndex) {
    return new MethodLocation(typeName.toString(), selector.toString(), lineNumber, bcIndex);
  }

  public static MethodLocation createMethodLocation(final TypeName typeName, final Selector selector, final int lineNumber) {
    return new MethodLocation(typeName.toString(), selector.toString(), lineNumber);
  }

  public static FieldLocation createFieldLocation(final TypeName typeName, final String fieldName) {
    return new FieldLocation(typeName.toString(), fieldName);
  }

  public static UnknownLocation createUnknownLocation() {
    return UnknownLocation.location();
  }

  public abstract String getLocationClass();

  public String getSourceFileName() {
    final int dollarIndex = getLocationClass().indexOf('$');
    final String sourceFileName;
    if (dollarIndex != -1) {
      sourceFileName = getLocationClass().substring(0, dollarIndex);
    } else {
      sourceFileName = getLocationClass();
    }
    return sourceFileName.replace('.', '/').concat(".java"); //$NON-NLS-1$
  }

  public abstract String getByteCodeLocation();

  public abstract String getSourceLocation();

  public abstract int getLocationLineNumber();

  public abstract int getByteCodeIndex();

  public abstract Object getAdditionalInformation();

  /**
   * force subclasses to define equals as Locations are being stored in
   * collections
   */
  public abstract boolean equals(Object other);

  /**
   * force subclasses to define hashCode as Locations are being stored in
   * collections
   */
  public abstract int hashCode();

  public abstract boolean isFieldMember();

  public abstract boolean isMethodMember();
}
