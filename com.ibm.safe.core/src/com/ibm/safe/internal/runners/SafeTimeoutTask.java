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
package com.ibm.safe.internal.runners;

import java.util.TimerTask;

final class SafeTimeoutTask extends TimerTask {

  /**
   * SAFE solver thread to be interrupted
   */
  private Thread safeSolverThread;

  public SafeTimeoutTask(final Thread solverThread) {
    this.safeSolverThread = solverThread;
  }

  public void run() {
    this.safeSolverThread.interrupt();
  }

}
