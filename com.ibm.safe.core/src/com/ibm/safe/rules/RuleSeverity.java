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
 * Enumeration that distinguishes the different kind of severities for SAFE
 * rules.
 * 
 * @author egeay
 * @author yahave
 */
public final class RuleSeverity extends NamedValue {

  public static final int INFORMATION = 0;

  public static final int WARNING = 1;

  public static final int ERROR = 2;

  public static final RuleSeverity INFORMATION_LITERAL = new RuleSeverity(INFORMATION, "Information", "Information");

  public static final RuleSeverity WARNING_LITERAL = new RuleSeverity(WARNING, "Warning", "Warning");

  public static final RuleSeverity ERROR_LITERAL = new RuleSeverity(ERROR, "Error", "Error");

  private static final RuleSeverity[] VALUES_ARRAY = new RuleSeverity[] { INFORMATION_LITERAL, WARNING_LITERAL, ERROR_LITERAL, };

  public static final List<RuleSeverity> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

  public static RuleSeverity get(String literal) {
    for (int i = 0; i < VALUES_ARRAY.length; ++i) {
      RuleSeverity result = VALUES_ARRAY[i];
      if (result.toString().equals(literal)) {
        return result;
      }
    }
    return null;
  }

  public static RuleSeverity getByName(String name) {
    for (int i = 0; i < VALUES_ARRAY.length; ++i) {
      RuleSeverity result = VALUES_ARRAY[i];
      if (result.getName().equals(name)) {
        return result;
      }
    }
    return null;
  }

  public static RuleSeverity get(int value) {
    switch (value) {
    case INFORMATION:
      return INFORMATION_LITERAL;
    case WARNING:
      return WARNING_LITERAL;
    case ERROR:
      return ERROR_LITERAL;
    }
    return null;
  }

  private RuleSeverity(int value, String name, String literal) {
    super(value, name, literal);
  }

}
