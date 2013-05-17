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
package com.ibm.safe.typestate.metrics;

class TypeStateMetricsByLoader {

  TypeStateMetricsByLoader(final long theByteCodeStatements, final long theClassesNumber, final long theMethodsNumber,
      final String theClassLoaderName) {
    this.byteCodeStatements = theByteCodeStatements;
    this.classesNumber = theClassesNumber;
    this.methodsNumber = theMethodsNumber;
    this.classLoaderName = theClassLoaderName;
  }

  // --- Interface methods implementation

  public final long getByteCodeStatements() {
    return this.byteCodeStatements;
  }

  public final long getClassesNumber() {
    return this.classesNumber;
  }

  public final String getClassLoaderName() {
    return this.classLoaderName;
  }

  public final long getMethodsNumber() {
    return this.methodsNumber;
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("ClassLoader: ");
    result.append(getClassLoaderName());
    result.append("\n");
    result.append("Classes: ");
    result.append(getClassesNumber());
    result.append("\n");
    result.append("Methods: ");
    result.append(getMethodsNumber());
    result.append("\n");
    result.append("Bytecodes: ");
    result.append(getByteCodeStatements());
    result.append("\n");
    return result.toString();
  }

  // --- Private code

  private final long byteCodeStatements;

  private final long classesNumber;

  private final long methodsNumber;

  private final String classLoaderName;

}
