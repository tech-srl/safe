/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.rules;

public class NamedValue {
  public NamedValue(int value, String name, String literal) {
    this.value = value;
    this.name = name;
    this.literal = literal;
  }

  public int getValue() {
    return value;
  }

  public String getName() {
    return name;
  }

  public String getLiteral() {
    return literal;
  }

  private int value;

  private String name;

  private String literal;
}
