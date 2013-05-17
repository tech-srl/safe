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
 * $Id: DFATransition.java,v 1.6 2010/10/17 01:20:31 eyahav Exp $
 */
package com.ibm.safe.dfa;

/*
 * DFA Transition 
 */
public class DFATransition implements IDFATransition {

  protected String destination;

  protected String event;

  protected String source;

  public DFATransition() {
    super();
  }

  public String getDestination() {
    return destination;
  }

  public void setDestination(String newDestination) {
    destination = newDestination;
  }

  public String getEvent() {
    return event;
  }

  public void setEvent(String newEvent) {

    event = newEvent;

  }

  public String getSource() {
    return source;
  }

  public void setSource(String newSource) {
    source = newSource;
  }

  public String toString() {
    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (destination: ");
    result.append(destination);
    result.append(", event: ");
    result.append(event);
    result.append(", source: ");
    result.append(source);
    result.append(')');
    return result.toString();
  }

  // --- Non-EMF generated methods

  public boolean equals(final Object rhsObject) {
    if (rhsObject == null)
      return false;
    if (!getClass().equals(rhsObject.getClass()))
      return false;
    final DFATransition rhs = (DFATransition) rhsObject;
    if ((this.source == null) && (this.event == null) && (this.destination == null)) {
      return ((this.source == rhs.source) && (this.event == rhs.event) && (this.destination == rhs.destination));
    } else if ((this.event == null) && (this.destination == null)) {
      return (this.source.equals(rhs.source) && (this.event == rhs.event) && (this.destination == rhs.destination));
    } else if ((this.source == null) && (this.destination == null)) {
      return ((this.source == rhs.source) && this.event.equals(rhs.event) && (this.destination == rhs.destination));
    } else if ((this.source == null) && (this.event == null)) {
      return ((this.source == rhs.source) && (this.event == rhs.event) && this.destination.equals(rhs.destination));
    } else if (this.source == null) {
      return ((this.source == rhs.source) && this.event.equals(rhs.event) && this.destination.equals(rhs.destination));
    } else if (this.event == null) {
      return (this.source.equals(rhs.source) && (this.event == rhs.event) && this.destination.equals(rhs.destination));
    } else if (this.destination == null) {
      return (this.source.equals(rhs.source) && this.event.equals(rhs.event) && (this.destination == rhs.destination));
    } else {
      return (this.source.equals(rhs.source) && this.event.equals(rhs.event) && this.destination.equals(rhs.destination));
    }
  }

  public int hashCode() {
    if ((this.source == null) && (this.event == null) && (this.destination == null)) {
      return -1;
    } else if ((this.event == null) && (this.destination == null)) {
      return this.source.hashCode();
    } else if ((this.source == null) && (this.destination == null)) {
      return this.event.hashCode();
    } else if ((this.source == null) && (this.event == null)) {
      return this.destination.hashCode();
    } else if (this.source == null) {
      return this.event.hashCode() + 123 * this.destination.hashCode();
    } else if (this.event == null) {
      return this.source.hashCode() + 34 * this.destination.hashCode();
    } else if (this.destination == null) {
      return this.source.hashCode() + 54 * this.event.hashCode();
    } else {
      return this.source.hashCode() + 23 * this.event.hashCode() + 34 + this.destination.hashCode();
    }
  }

}
