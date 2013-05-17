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

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMember;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.TypeName;

class AccessibilityMember {
  private String memberName;

  private TypeName className;

  private String packageName;

  private String classLoaderName;

  private boolean isField = false;

  AccessibilityMember(IMember iMember) {
    if (iMember instanceof IField) {
      memberName = iMember.getName().toString();
      isField = true;
    } else {
      IMethod method = (IMethod) iMember;
      memberName = method.getSelector().toString();
    }
    IClass declaringClass = iMember.getDeclaringClass();
    className = declaringClass.getName();
    classLoaderName = declaringClass.getClassLoader().getName().toString();

    packageName = computePackageName(className.toString());
  }

  // AccessibilityMember(FieldReference fieldRef) {
  // isField = true;
  // memberName = fieldRef.getName().toString();
  // TypeReference typeRef = fieldRef.getType();
  // className = typeRef.getName().toString();
  // packageName = computePackageName(className);
  // classLoaderName = typeRef.getClassLoader().getName().toString();
  // }
  //	
  // AccessibilityMember(MethodReference methodRef) {
  // memberName = methodRef.getSignature();
  // TypeReference declaringClassRef = methodRef.getDeclaringClass();
  // className = declaringClassRef.getName().toString();
  // packageName = computePackageName(className);
  // classLoaderName = declaringClassRef.getClassLoader().getName().toString();
  // }

  public static String computePackageName(String className) {
    int lastSlash = className.lastIndexOf('/');
    if (lastSlash == -1)
      return "";
    return className.substring(0, lastSlash).replace('/', '.');
  }

  public String getMemberName() {
    return memberName;
  }

  public TypeName getClassName() {
    return className;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getClassLoaderName() {
    return classLoaderName;
  }

  public boolean isField() {
    return isField;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof AccessibilityMember))
      return false;
    AccessibilityMember that = (AccessibilityMember) obj;
    return this.isField == that.isField && this.memberName.equals(that.memberName) && this.className.equals(that.className)
        && this.packageName.equals(that.packageName) && this.classLoaderName.equals(that.classLoaderName);
  }

  public int hashCode() {
    return memberName.hashCode() ^ className.hashCode() ^ packageName.hashCode() ^ classLoaderName.hashCode();
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    if (isField())
      buf.append("Field Name: ");
    else
      buf.append("Method Name: ");
    buf.append(getMemberName().replaceAll("<init>", "()") + "\n");
    buf.append("Class Name: " + getClassName().toString().replace('/', '.') + "\n");
    buf.append("Package Name: " + getPackageName().replace('/', '.') + "\n");
    buf.append("Class Loader Name: " + getClassLoaderName() + "\n");
    return buf.toString();
  }
}