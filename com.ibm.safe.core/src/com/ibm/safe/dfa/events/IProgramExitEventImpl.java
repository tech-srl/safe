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
 /*
 * $Id: IProgramExitEventImpl.java,v 1.3 2010/09/02 01:08:06 eyahav Exp $
 */
package com.ibm.safe.dfa.events;



public class IProgramExitEventImpl extends IEventImpl implements IProgramExitEvent {

  private static final String PROGRAM_EXIT = "ProgramExit";

  private static final IProgramExitEventImpl instance = new IProgramExitEventImpl();

  public static IProgramExitEvent singleton() {
    return instance;
  }

  public IProgramExitEventImpl() {
    super();
  }

  public boolean equals(final Object rhsObject) {
    if (rhsObject == null)
      return false;
    if (!getClass().equals(rhsObject.getClass())) {
      return false;
    }
    return super.equals(rhsObject);
  }

  public String getName() {
    return PROGRAM_EXIT;
  }

}
