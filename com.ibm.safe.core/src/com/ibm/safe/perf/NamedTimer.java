/*******************************************************************************
 * Copyright (c) 2002-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.perf;

import com.ibm.wala.util.perf.Stopwatch;

public class NamedTimer extends Stopwatch {

  public NamedTimer(final String timerName) {
    this.name = timerName;
  }

  public String getName() {
    return this.name;
  }

  public String toString() {
    return this.name;
  }

  private final String name;

}
