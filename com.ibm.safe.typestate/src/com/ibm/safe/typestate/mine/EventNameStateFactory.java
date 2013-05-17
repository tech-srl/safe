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

public class EventNameStateFactory implements IDFAStateFactory {

  private static EventNameStateFactory theInstance;

  public Object createState(Object conent) {
    return new HistoryState(conent, 0);
  }

  public static IDFAStateFactory getInstance() {
    if (theInstance == null) {
      theInstance = new EventNameStateFactory();
    }
    return theInstance;
  }

}
