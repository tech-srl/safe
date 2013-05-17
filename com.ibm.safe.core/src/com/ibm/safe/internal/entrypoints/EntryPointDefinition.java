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
package com.ibm.safe.internal.entrypoints;

public final class EntryPointDefinition {

  public EntryPointDefinition() {
  }

  public EntryPointDefinition(final String aClassName, final String aMethodName, final String aMethodDescriptor) {
    this.className = 'L' + aClassName.replace('.', '/');
    this.methodName = aMethodName;
    this.methodDescriptor = aMethodDescriptor.replace('.', '/');
  }

  // --- Interface methods implementation

  public String getClassName() {
    return this.className;
  }

  public String getMethodDescriptor() {
    return this.methodDescriptor;
  }

  public String getMethodName() {
    return this.methodName;
  }

  // --- Public services

  public void setClassName(final String aClassName) {
    this.className = 'L' + aClassName.replace('.', '/');
  }

  public void setMethodDescriptor(final String aMethodDescriptor) {
    this.methodDescriptor = aMethodDescriptor.replace('.', '/');
  }

  public void setMethodName(final String aMethodName) {
    this.methodName = aMethodName;
  }

  // --- Overridden methods

  public String toString() {
    final StringBuffer buf = new StringBuffer(this.className);
    buf.append('/').append(this.methodName).append(this.methodDescriptor);
    return buf.toString();
  }

  // --- Private code

  private String className;

  private String methodDescriptor;

  private String methodName;

}
