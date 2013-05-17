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
/*
 * Created on Dec 16, 2004
 */
package com.ibm.safe.structural.impl;

import com.ibm.safe.reporting.message.Location;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.rules.StructuralRule;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class StructuralMessage implements Message {
  protected StructuralRule rule;

  protected Location location;

  public StructuralMessage(StructuralRule rule, Location location) {
    assert rule != null && location != null : "message with null rule or location";
    this.rule = rule;
    this.location = location;
  }

  public Location getLocation() {
    return location;
  }

  public String getMessageType() {
    return "structural"; //$NON-NLS-1$
  }

  public String getText() {
    return rule.getName();
  }

  public IRule getRule() {
    return this.rule;
  }

  public boolean equals(Object other) {
    if (!(other instanceof StructuralMessage)) {
      return false;
    }
    StructuralMessage otherMessage = (StructuralMessage) other;
    return (rule.equals(otherMessage.rule) && location.equals(otherMessage.location));
  }

  public int hashCode() {
    return rule.hashCode() + location.hashCode();
  }

  public String toString() {
    final StringBuilder strBuilder = new StringBuilder();
    strBuilder.append(this.rule.getName()).append(" (").append(getLocation().getSourceLocation()) //$NON-NLS-1$
        .append(')');
    return strBuilder.toString();
  }

}