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

public class DummyRule extends IRule {

  public static final String EMPTY = "";

  private String name;

  public DummyRule(String name) {
    this.name = name;
  }

  public String getAction() {
    return EMPTY;
  }

  public String getDescription() {
    return EMPTY;
  }

  public String getExample() {
    return EMPTY;
  }

  public RuleLevel getLevel() {
    // TODO Auto-generated method stub
    return null;
  }

  public String getName() {
    return name;
  }

  public RuleSeverity getSeverity() {
    return RuleSeverity.ERROR_LITERAL;
  }

  public boolean isSureAnalysis() {
    return false;
  }
}
