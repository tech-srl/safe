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
package com.ibm.safe.perf;

public class TimeoutStopwatch extends NamedTimer {

  private boolean timedOut = false;

  public TimeoutStopwatch(String timerName) {
    super(timerName);
  }

  public final void timeout() {
    this.timedOut = true;
  }

  public boolean timedOut() {
    return this.timedOut;
  }

}
