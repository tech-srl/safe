/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 /*
 * $Id: IEventImpl.java,v 1.3 2010/10/17 01:20:31 eyahav Exp $
 */
package com.ibm.safe.dfa.events;


public class IEventImpl implements IEvent {


  protected String name = NAME_EDEFAULT;

  public IEventImpl() {
    super();
  }

  public String getName() {
    return name;
  }

  public void setName(String newName) {

    name = newName;

  }

  public boolean match(String elementToMatch) {
    // TODO: implement this method
    // Ensure that you remove @generated or mark it @generated NOT
    throw new UnsupportedOperationException();
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("IEventImpl:");
    result.append(name);
    return result.toString();
  }

  protected static final String NAME_EDEFAULT = "*"; //$NON-NLS-1$

  public boolean equals(final Object rhsObject) {
    if (!(rhsObject instanceof IEventImpl))
      return false;
    if (this.name == null) {
      return (this.name == ((IEventImpl) rhsObject).name);
    } else {
      return this.name.equals(((IEventImpl) rhsObject).name);
    }
  }

  public int hashCode() {
    return (this.name == null) ? -1 : this.name.hashCode();
  }

  public static final IEvent GENERIC_EVENT = new IEventImpl();

}
