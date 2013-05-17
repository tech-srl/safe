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

public abstract class IRule {

  protected String description;

  protected static final RuleLevel LEVEL_EDEFAULT = RuleLevel.METHOD_LEVEL_LITERAL;

  protected static final RuleSeverity SEVERITY_EDEFAULT = RuleSeverity.INFORMATION_LITERAL;

  protected String example;

  protected RuleLevel level = LEVEL_EDEFAULT;

  protected RuleSeverity severity = SEVERITY_EDEFAULT;

  protected String name;

  protected boolean sureAnalysis;

  protected String action;

  protected String fileName;

  public String getAction() {
    return action;
  }

  public void setAction(String newAction) {
    action = newAction;
  }


  public String getDescription() {
    return description;
  }

  public void setDescription(String newDescription) {
    description = newDescription;
  }

  public String getExample() {
    return example;
  }

  public void setExample(String newExample) {
    example = newExample;
  }

  public RuleLevel getLevel() {
    return level;
  }

  public void setLevel(RuleLevel newLevel) {
    level = newLevel == null ? LEVEL_EDEFAULT : newLevel;
  }

  public String getName() {
    return name;
  }

  public void setName(String newName) {
    name = newName;
  }

  public RuleSeverity getSeverity() {
    return severity;
  }

  public void setSeverity(RuleSeverity newSeverity) {

    severity = newSeverity == null ? SEVERITY_EDEFAULT : newSeverity;
  }

  public boolean isSureAnalysis() {
    return sureAnalysis;
  }

  public void setSureAnalysis(boolean newSureAnalysis) {

    sureAnalysis = newSureAnalysis;
  }

  public void setFileName(String fn) {
    this.fileName = fn;
  }

  public String getFileName() {
    return fileName;
  }

  public boolean equals(Object other) {
    if (!(other instanceof IRule)) {
      return false;
    }
    IRule otherRule = (IRule) other;
    return getName().equals(otherRule.getName());
  }

  public int hashCode() {
    return getName().hashCode();
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append(" (action: ");
    result.append(action);
    result.append(", description: ");
    result.append(description);
    result.append(", example: ");
    result.append(example);
    result.append(", level: ");
    result.append(level);
    result.append(", name: ");
    result.append(name);
    result.append(", severity: ");
    result.append(severity);
    result.append(", sureAnalysis: ");
    result.append(sureAnalysis);
    result.append(')');
    return result.toString();
  }


}
