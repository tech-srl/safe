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

public final class ReportLocation extends NamedValue {

  public static final byte NAME = 0;

  public static final byte SIGNATURE = 1;

  public static final ReportLocation NAME_LITERAL = new ReportLocation(NAME, "Name", "Name");

  public static final ReportLocation SIGNATURE_LITERAL = new ReportLocation(SIGNATURE, "Signature", "Signature");

  private static final ReportLocation[] VALUES_ARRAY = new ReportLocation[] { NAME_LITERAL, SIGNATURE_LITERAL, };

  public static final List<ReportLocation> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

  public static ReportLocation get(String literal) {
    for (int i = 0; i < VALUES_ARRAY.length; ++i) {
      ReportLocation result = VALUES_ARRAY[i];
      if (result.toString().equals(literal)) {
        return result;
      }
    }
    return null;
  }

  public static ReportLocation getByName(String name) {
    for (int i = 0; i < VALUES_ARRAY.length; ++i) {
      ReportLocation result = VALUES_ARRAY[i];
      if (result.getName().equals(name)) {
        return result;
      }
    }
    return null;
  }

  public static ReportLocation get(int value) {
    switch (value) {
    case NAME:
      return NAME_LITERAL;
    case SIGNATURE:
      return SIGNATURE_LITERAL;
    }
    return null;
  }

  private ReportLocation(int value, String name, String literal) {
    super(value, name, literal);
  }

}
