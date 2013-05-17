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

import java.util.Set;

import com.ibm.wala.util.collections.HashSetFactory;

public class EventFilter {

  private static Set<String> ignoredEvents;

  static {
    ignoredEvents = HashSetFactory.make();
    ignoredEvents.add("isPaused");
  }

  public static boolean shouldIgnore(String event) {
    return (ignoredEvents.contains(event));
  }

}
