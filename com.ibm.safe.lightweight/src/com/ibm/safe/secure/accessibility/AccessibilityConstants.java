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

public interface AccessibilityConstants {
  public static final int PRIVATE = 0;

  public static final int DEFAULT = 1;

  public static final int PROTECTED = 2;

  public static final int PUBLIC = 3;

  /** *********************************************************************** */

  public static final String PUBLIC_FIELD_PROTECTED = "Public field could be made protected";

  public static final String PUBLIC_FIELD_DEFAULT = "Public field could be made default";

  public static final String PUBLIC_FIELD_PRIVATE = "Public field could be made private";

  public static final String PROTECTED_FIELD_DEFAULT = "Protected field could be made default";

  public static final String PROTECTED_FIELD_PRIVATE = "Protected field could be made private";

  public static final String DEFAULT_FIELD_PRIVATE = "Default field could be made private";

  public static final String PUBLIC_METHOD_PROTECTED = "Public method could be made protected";

  public static final String PUBLIC_METHOD_DEFAULT = "Public method could be made default";

  public static final String PUBLIC_METHOD_PRIVATE = "Public method could be made private";

  public static final String PROTECTED_METHOD_DEFAULT = "Protected method could be made default";

  public static final String PROTECTED_METHOD_PRIVATE = "Protected method could be made private";

  public static final String DEFAULT_METHOD_PRIVATE = "Default method could be made private";

  public static final String FIELD_UNREFERENCED = "Unreferenced field";

  public static final String PUBLIC_METHOD_UNREACHABLE = "Unreachable public method: Dead code? Entry Point?";

  public static final String PROTECTED_METHOD_UNREACHABLE = "Unreachable protected method: Dead code? Entry Point?";

  public static final String DEFAULT_METHOD_UNREACHABLE = "Unreachable default method: Dead code";

  public static final String PRIVATE_METHOD_UNREACHABLE = "Unreachable private method: Dead code";

  public static final String FIELD_MAKE_FINAL = "Non-private, non-final, static field";

  public static final String PUBLIC_CLASS_DEFAULT = "Public class could be made default";

  /** *********************************************************************** */

  public static final Integer PUBLIC_FIELD_PROTECTED_ID = new Integer(1);

  public static final Integer PUBLIC_FIELD_DEFAULT_ID = new Integer(2);

  public static final Integer PUBLIC_FIELD_PRIVATE_ID = new Integer(3);

  public static final Integer PROTECTED_FIELD_DEFAULT_ID = new Integer(4);

  public static final Integer PROTECTED_FIELD_PRIVATE_ID = new Integer(5);

  public static final Integer DEFAULT_FIELD_PRIVATE_ID = new Integer(6);

  public static final Integer PUBLIC_METHOD_PROTECTED_ID = new Integer(7);

  public static final Integer PUBLIC_METHOD_DEFAULT_ID = new Integer(8);

  public static final Integer PUBLIC_METHOD_PRIVATE_ID = new Integer(9);

  public static final Integer PROTECTED_METHOD_DEFAULT_ID = new Integer(10);

  public static final Integer PROTECTED_METHOD_PRIVATE_ID = new Integer(11);

  public static final Integer DEFAULT_METHOD_PRIVATE_ID = new Integer(12);

  public static final Integer FIELD_UNREFERENCED_ID = new Integer(13);

  public static final Integer PUBLIC_METHOD_UNREACHABLE_ID = new Integer(14);

  public static final Integer PROTECTED_METHOD_UNREACHABLE_ID = new Integer(15);

  public static final Integer DEFAULT_METHOD_UNREACHABLE_ID = new Integer(16);

  public static final Integer PRIVATE_METHOD_UNREACHABLE_ID = new Integer(17);

  public static final Integer FIELD_MAKE_FINAL_ID = new Integer(18);

  public static final Integer PUBLIC_CLASS_DEFAULT_ID = new Integer(19);

  public final String MAKE_PRIVATE = "Only accessed from its own class.  It should be given private scope.\n";

  public final String MAKE_DEFAULT = "Only accessed from its own package.  It should be given default scope.\n";

  public final String MAKE_PROTECTED = "Only accessed from its own package and inherited and used outside of its package.  It should be given protected scope.\n";
}