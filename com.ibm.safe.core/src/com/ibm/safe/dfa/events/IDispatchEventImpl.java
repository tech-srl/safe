/*******************************************************************************
 * Copyright (c) 2002 - 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.dfa.events;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IDispatchEventImpl extends IEventImpl implements IDispatchEvent {

  protected String pattern;

  public IDispatchEventImpl() {
    super();
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String newPattern) {
    pattern = newPattern;
  }

  public boolean equals(final Object rhsObject) {
    if (rhsObject == null)
      return false;
    if (!getClass().equals(rhsObject.getClass())) {
      return false;
    }
    if (this.pattern == null) {
      return (this.pattern == ((IDispatchEventImpl) rhsObject).pattern) && super.equals(rhsObject);
    } else {
      return (super.equals(rhsObject) && this.pattern.equals(((IDispatchEventImpl) rhsObject).pattern));
    }
  }

  public int hashCode() {
    return (this.pattern != null) ? this.pattern.hashCode() + super.hashCode() : super.hashCode();
  }

  public boolean match(final String elementToMatch) {
    final String regex = this.pattern;
    final Pattern pattern = Pattern.compile(regex);
    final Matcher matcher = pattern.matcher(elementToMatch);
    return matcher.matches();
  }

}
