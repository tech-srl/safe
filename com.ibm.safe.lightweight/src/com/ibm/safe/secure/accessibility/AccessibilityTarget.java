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

import java.util.HashMap;
import java.util.Map;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMember;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;

public final class AccessibilityTarget extends AccessibilityMember {
  private int currentModifier;

  private int suggestedModifier;

  private boolean isStatic = false;

  private boolean isFinal = false;

  private int superModifier;

  private static Map<IMember, AccessibilityTarget> cache = new HashMap<IMember, AccessibilityTarget>();

  private AccessibilityTarget(IMember member) {
    super(member);
    if (member instanceof IMethod) {
      ShrikeCTMethod method = (ShrikeCTMethod) member;
      if (method.isPrivate())
        currentModifier = AccessibilityConstants.PRIVATE;
      else if (method.isProtected())
        currentModifier = AccessibilityConstants.PROTECTED;
      else if (method.isPublic())
        currentModifier = AccessibilityConstants.PUBLIC;
      else
        currentModifier = AccessibilityConstants.DEFAULT;
      if (method.isStatic())
        isStatic = true;
      if (method.isFinal())
        isFinal = true;
    } else {
      IField field = (IField) member;
      if (field.isPrivate())
        currentModifier = AccessibilityConstants.PRIVATE;
      else if (field.isProtected())
        currentModifier = AccessibilityConstants.PROTECTED;
      else if (field.isPublic())
        currentModifier = AccessibilityConstants.PUBLIC;
      else
        currentModifier = AccessibilityConstants.DEFAULT;
      if (field.isStatic())
        isStatic = true;
      if (field.isFinal())
        isFinal = true;
    }
    suggestedModifier = -1;
  }

  public static AccessibilityTarget getAccessibilityTarget(IMember member) {
    AccessibilityTarget at = cache.get(member);
    if (at == null) {
      at = new AccessibilityTarget(member);
      cache.put(member, at);
    }
    return at;
  }

  public int getCurrentModifier() {
    return currentModifier;
  }

  public int getSuggestedModifier() {
    return suggestedModifier;
  }

  void setSuggestedModifier(int suggestedModifier) {
    this.suggestedModifier = suggestedModifier;
  }

  public boolean isFinal() {
    return isFinal;
  }

  public boolean isStatic() {
    return isStatic;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer(super.toString());
    buf.append("Modifiers: ");
    if (currentModifier == AccessibilityConstants.PRIVATE)
      buf.append("private ");
    else if (currentModifier == AccessibilityConstants.DEFAULT)
      buf.append("default ");
    else if (currentModifier == AccessibilityConstants.PROTECTED)
      buf.append("protected ");
    else
      buf.append("public ");
    if (isStatic)
      buf.append("static ");
    if (isFinal)
      buf.append("final");
    buf.append("\n");
    return buf.toString();
  }

  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  public int hashCode() {
    // TODO Auto-generated method stub
    return super.hashCode();
  }

  public void setSuperModifier(int superModifier) {
    this.superModifier = superModifier;
  }

  public int getSuperModifier() {
    return superModifier;
  }
}