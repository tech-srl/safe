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
 * $Id: IObjectDeathEventImpl.java,v 1.3 2010/10/17 01:20:31 eyahav Exp $
 */
package com.ibm.safe.dfa.events;




public class IObjectDeathEventImpl extends IEventImpl implements IObjectDeathEvent {

  private static final String OBJECT_DEATH = "ObjectDeath";

  private static final IObjectDeathEventImpl instance = new IObjectDeathEventImpl();

  public static IObjectDeathEventImpl singleton() {
    return instance;
  }

  protected IObjectDeathEventImpl() {
    super();
  }

  public String getName() {
    return OBJECT_DEATH;
  }

}
