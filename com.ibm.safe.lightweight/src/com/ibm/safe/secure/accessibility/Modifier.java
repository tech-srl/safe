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

/**
 * This class contains the numeric constants that match the access modifiers as
 * described by the Java Virtual Machine (JVM) Specification. The modifiers are:
 * <ul>
 * <li><code>public</code>
 * <li><code>protected</code>
 * <li><code>private</code>
 * <li><code>static</code>
 * <li><code>abstract</code>
 * <li><code>final</code>
 * <li><code>native</code>
 * <li><code>synchronized</code>
 * <li><code>transient</code>
 * <li><code>volatile</code>
 * <li><code>strictfp</code>
 * </ul>
 * 
 * @author Marco Pistoia
 */
public final class Modifier {

  /**
   * Modifier constant (bit mask, value 0) indicating no modifiers.
   */
  public static final int NONE = 0x0000;

  /**
   * <code>public</code> modifier constant (bit mask). Applicable to types,
   * methods, constructors, and fields.
   */
  public static final int PUBLIC = 0x0001;

  /**
   * <code>private</code> modifier constant (bit mask). Applicable to types,
   * methods, constructors, and fields.
   */
  public static final int PRIVATE = 0x0002;

  /**
   * <code>protected</code> modifier constant (bit mask). Applicable to types,
   * methods, constructors, and fields.
   */
  public static final int PROTECTED = 0x0004;

  /**
   * <code>static</code> modifier constant (bit mask). Applicable to types,
   * methods, fields, and initializers.
   */
  public static final int STATIC = 0x0008;

  /**
   * <code>final</code> modifier constant (bit mask). Applicable to types,
   * methods, fields, and variables.
   */
  public static final int FINAL = 0x0010;

  /**
   * <code>synchronized</code> modifier constant (bit mask). Applicable only
   * to methods.
   */
  public static final int SYNCHRONIZED = 0x0020;

  /**
   * <code>volatile</code> modifier constant (bit mask). Applicable only to
   * fields.
   */
  public static final int VOLATILE = 0x0040;

  /**
   * <code>transient</code> modifier constant (bit mask). Applicable only to
   * fields.
   */
  public static final int TRANSIENT = 0x0080;

  /**
   * <code>native</code> modifier constant (bit mask). Applicable only to
   * methods.
   */
  public static final int NATIVE = 0x0100;

  /**
   * <code>abstract</code> modifier constant (bit mask). Applicable to types
   * and methods.
   */
  public static final int ABSTRACT = 0x0400;

  /**
   * <code>strictfp</code> modifier constant (bit mask). Applicable to types
   * and methods.
   */
  public static final int STRICTFP = 0x0800;

  /**
   * Returns whether the given flags includes the <code>public</code>
   * modifier. Applicable to types, methods, constructors, and fields.
   * 
   * @param flags
   *            the modifier flags
   * @return <code>true</code> if the <code>PUBLIC</code> bit is set, and
   *         <code>false</code> otherwise
   */
  public static boolean isPublic(int flags) {
    return (flags & PUBLIC) != 0;
  }

  /**
   * Returns whether the given flags includes the <code>private</code>
   * modifier. Applicable to types, methods, constructors, and fields.
   * 
   * @param flags
   *            the modifier flags
   * @return <code>true</code> if the <code>PRIVATE</code> bit is set, and
   *         <code>false</code> otherwise
   */
  public static boolean isPrivate(int flags) {
    return (flags & PRIVATE) != 0;
  }

  /**
   * Returns whether the given flags includes the <code>protected</code>
   * modifier. Applicable to types, methods, constructors, and fields.
   * 
   * @param flags
   *            the modifier flags
   * @return <code>true</code> if the <code>PROTECTED</code> bit is set, and
   *         <code>false</code> otherwise
   */
  public static boolean isProtected(int flags) {
    return (flags & PROTECTED) != 0;
  }

  /**
   * Returns whether the given flags does not include any of the modifiers
   * <code>public</code>, <code>protected</code>, and <code>default</code>.
   * Applicable to types, methods, constructors, and fields.
   * 
   * @param flags
   *            the modifier flags
   * @return <code>true</code> if none of the bits <code>PUBLIC</code>,
   *         <code>PROTECTED</code>, and <code>PRIVATE</code> bits are set,
   *         and <code>false</code> otherwise.
   */
  public static boolean isDefault(int flags) {
    return !isPublic(flags) && !isProtected(flags) && !isPrivate(flags);
  }

  /**
   * Returns whether the given flags includes the <code>static</code>
   * modifier. Applicable to types, methods, fields, and initializers.
   * 
   * @param flags
   *            the modifier flags
   * @return <code>true</code> if the <code>STATIC</code> bit is set, and
   *         <code>false</code> otherwise
   */
  public static boolean isStatic(int flags) {
    return (flags & STATIC) != 0;
  }

  /**
   * Returns whether the given flags includes the <code>final</code> modifier.
   * Applicable to types, methods, fields, and variables.
   * 
   * @param flags
   *            the modifier flags
   * @return <code>true</code> if the <code>FINAL</code> bit is set, and
   *         <code>false</code> otherwise
   */
  public static boolean isFinal(int flags) {
    return (flags & FINAL) != 0;
  }

  /**
   * Returns whether the given flags includes the <code>synchronized</code>
   * modifier. Applicable only to methods.
   * 
   * @param flags
   *            the modifier flags
   * @return <code>true</code> if the <code>SYNCHRONIZED</code> bit is set,
   *         and <code>false</code> otherwise
   */
  public static boolean isSynchronized(int flags) {
    return (flags & SYNCHRONIZED) != 0;
  }

  /**
   * Returns whether the given flags includes the <code>volatile</code>
   * modifier. Applicable only to fields.
   * 
   * @param flags
   *            the modifier flags
   * @return <code>true</code> if the <code>VOLATILE</code> bit is set, and
   *         <code>false</code> otherwise
   */
  public static boolean isVolatile(int flags) {
    return (flags & VOLATILE) != 0;
  }

  /**
   * Returns whether the given flags includes the <code>transient</code>
   * modifier. Applicable only to fields.
   * 
   * @param flags
   *            the modifier flags
   * @return <code>true</code> if the <code>TRANSIENT</code> bit is set, and
   *         <code>false</code> otherwise
   */
  public static boolean isTransient(int flags) {
    return (flags & TRANSIENT) != 0;
  }

  /**
   * Returns whether the given flags includes the <code>native</code>
   * modifier. Applicable only to methods.
   * 
   * @param flags
   *            the modifier flags
   * @return <code>true</code> if the <code>NATIVE</code> bit is set, and
   *         <code>false</code> otherwise
   */
  public static boolean isNative(int flags) {
    return (flags & NATIVE) != 0;
  }

  /**
   * Returns whether the given flags includes the <code>abstract</code>
   * modifier. Applicable to types and methods.
   * 
   * @param flags
   *            the modifier flags
   * @return <code>true</code> if the <code>ABSTRACT</code> bit is set, and
   *         <code>false</code> otherwise
   */
  public static boolean isAbstract(int flags) {
    return (flags & ABSTRACT) != 0;
  }

  /**
   * Returns whether the given flags includes the <code>strictfp</code>
   * modifier. Applicable to types and methods.
   * 
   * @param flags
   *            the modifier flags
   * @return <code>true</code> if the <code>STRICTFP</code> bit is set, and
   *         <code>false</code> otherwise
   */
  public static boolean isStrictfp(int flags) {
    return (flags & STRICTFP) != 0;
  }
}