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

import com.ibm.safe.reporting.message.Location;
import com.ibm.safe.rules.StructuralRule;
import com.ibm.safe.structural.impl.StructuralMessage;

public class AccessibilityMessage extends StructuralMessage {

  private AccessibilityTarget target;

  public AccessibilityMessage(StructuralRule rule, Location location, AccessibilityTarget target) {
    super(rule, location);
    this.target = target;
  }

  public boolean equals(Object other) {
    if (!(other instanceof AccessibilityMessage)) {
      return false;
    }
    AccessibilityMessage otherMessage = (AccessibilityMessage) other;
    return target.equals(otherMessage.target) && rule == otherMessage.rule && location.equals(otherMessage.location);
  }

  public int hashCode() {
    return target.hashCode() ^ rule.hashCode() ^ location.hashCode();
  }

  public String toString() {
    final StringBuilder strBuilder = new StringBuilder();
    strBuilder.append(getRule().getName()).append(" (").append(getLocation().getSourceLocation()) //$NON-NLS-1$
        .append(')').append(" - target: ").append(target);
    return strBuilder.toString();
  }
}
