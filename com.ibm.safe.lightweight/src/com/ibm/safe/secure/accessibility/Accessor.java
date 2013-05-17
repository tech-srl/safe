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
package com.ibm.safe.secure.accessibility;

import com.ibm.wala.classLoader.IMember;

public class Accessor extends AccessibilityMember {

  private String accessingClassName;

  private String accessingPackageName;

  private String accessingClassLoaderName;

  Accessor(IMember iMember, String accessingClassName, String accessingClassLoaderName) {
    super(iMember);
    this.accessingClassName = accessingClassName;
    this.accessingPackageName = computePackageName(accessingClassName);
    this.accessingClassLoaderName = accessingClassLoaderName;
  }

  String getAccessingClassName() {
    return accessingClassName;
  }

  String getAccessingPackageName() {
    return accessingPackageName;
  }

  String getAccessingClassLoaderName() {
    return accessingClassLoaderName;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(super.toString());
    buf.append("Accessing Class: " + accessingClassName + "\n");
    buf.append("Accessing Package: " + accessingPackageName + "\n");
    buf.append("Accessing ClassLoader: " + accessingClassLoaderName + "\n");
    return buf.toString();
  }
}
