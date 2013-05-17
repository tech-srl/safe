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
package com.ibm.safe.rules;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration that distinguishes the different level for all SAFE rules. A
 * level for a rule establishes a scope of application on which a rule can be
 * related to.
 * 
 * @model
 * @author egeay
 */
public final class RuleLevel extends NamedValue {
  /**
   * Identifies all rules that produce findings at the method level, ie inside
   * methods.
   * 
   * @model name="MethodLevel"
   */
  public static final int METHOD_LEVEL = 0;

  /**
   * Identifies all rules that produce findings at the class level, ie inside
   * class definition.
   * 
   * @model name="ClassLevel"
   */
  public static final int CLASS_LEVEL = 1;

  /**
   * Identifies all rules that produce findings at the package level.
   * 
   * @model name="PackageLevel"
   */
  public static final int PACKAGE_LEVEL = 2;

  /**
   * Identifies all rules that produce findings at the project level.
   * 
   * @model name="ProjectLevel"
   */
  public static final int PROJECT_LEVEL = 3;

  /**
   * The '<em><b>Method Level</b></em>' literal object. <!-- begin-user-doc
   * --> <!-- end-user-doc -->
   * 
   * @see #METHOD_LEVEL
   * @generated
   * @ordered
   */
  public static final RuleLevel METHOD_LEVEL_LITERAL = new RuleLevel(METHOD_LEVEL, "MethodLevel", "MethodLevel");

  /**
   * The '<em><b>Class Level</b></em>' literal object. <!-- begin-user-doc
   * --> <!-- end-user-doc -->
   * 
   * @see #CLASS_LEVEL
   * @generated
   * @ordered
   */
  public static final RuleLevel CLASS_LEVEL_LITERAL = new RuleLevel(CLASS_LEVEL, "ClassLevel", "ClassLevel");

  /**
   * The '<em><b>Package Level</b></em>' literal object. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @see #PACKAGE_LEVEL
   * @generated
   * @ordered
   */
  public static final RuleLevel PACKAGE_LEVEL_LITERAL = new RuleLevel(PACKAGE_LEVEL, "PackageLevel", "PackageLevel");

  /**
   * The '<em><b>Project Level</b></em>' literal object. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @see #PROJECT_LEVEL
   * @generated
   * @ordered
   */
  public static final RuleLevel PROJECT_LEVEL_LITERAL = new RuleLevel(PROJECT_LEVEL, "ProjectLevel", "ProjectLevel");

  /**
   * An array of all the '<em><b>Rule Level</b></em>' enumerators. <!--
   * begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  private static final RuleLevel[] VALUES_ARRAY = new RuleLevel[] { METHOD_LEVEL_LITERAL, CLASS_LEVEL_LITERAL,
      PACKAGE_LEVEL_LITERAL, PROJECT_LEVEL_LITERAL, };

  /**
   * A public read-only list of all the '<em><b>Rule Level</b></em>'
   * enumerators. <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  public static final List<RuleLevel> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

  /**
   * Returns the '<em><b>Rule Level</b></em>' literal with the specified
   * literal value. <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  public static RuleLevel get(String literal) {
    for (int i = 0; i < VALUES_ARRAY.length; ++i) {
      RuleLevel result = VALUES_ARRAY[i];
      if (result.toString().equals(literal)) {
        return result;
      }
    }
    return null;
  }

  /**
   * Returns the '<em><b>Rule Level</b></em>' literal with the specified
   * name. <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  public static RuleLevel getByName(String name) {
    for (int i = 0; i < VALUES_ARRAY.length; ++i) {
      RuleLevel result = VALUES_ARRAY[i];
      if (result.getName().equals(name)) {
        return result;
      }
    }
    return null;
  }

  public static RuleLevel get(int value) {
    switch (value) {
    case METHOD_LEVEL:
      return METHOD_LEVEL_LITERAL;
    case CLASS_LEVEL:
      return CLASS_LEVEL_LITERAL;
    case PACKAGE_LEVEL:
      return PACKAGE_LEVEL_LITERAL;
    case PROJECT_LEVEL:
      return PROJECT_LEVEL_LITERAL;
    }
    return null;
  }

  private RuleLevel(int value, String name, String literal) {
    super(value, name, literal);
  }

}
