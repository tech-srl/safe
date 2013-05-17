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
package com.ibm.safe.typestate.mine;

import com.ibm.safe.dfa.IDFAStateFactory;
import com.ibm.safe.utils.Trace;

public class UniqueNameStateFactory implements IDFAStateFactory {

  private static final boolean DEBUG = false;

  private static UniqueNameStateFactory theInstance;

  private static int stateIdCounter = 0;

  public static UniqueNameStateFactory getInstance() {
    if (theInstance == null) {
      theInstance = new UniqueNameStateFactory();
    }
    return theInstance;
  }

  public Object createState(Object content) {
    HistoryState result = new HistoryState("", stateIdCounter);
    stateIdCounter++;
    if (DEBUG) {
      Trace.println("StateIdCounter:" + stateIdCounter);
    }

    return result;
  }
}
