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
package com.ibm.safe.internal.runners;

import java.util.Collection;
import java.util.Iterator;
import java.util.Stack;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.ibm.safe.internal.exceptions.ExceptionContainer;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.runners.IResultObserver;
import com.ibm.safe.runners.ISolverRunner;
import com.ibm.safe.utils.SafeLogger;
import com.ibm.wala.util.CancelException;

public final class CompositeSolverRunner implements ISolverRunner {

  // --- Interface methods implementation

  public void run(final IResultObserver observer, final IProgressMonitor monitor) throws SafeException, CancelException, IllegalArgumentException, CoreException {
    final ExceptionContainer exceptContainer = new ExceptionContainer();

    boolean oneRunnerSucceeded = false;
    monitor.beginTask(null, this.solverRunners.size());
    for (Iterator<ISolverRunner> iter = this.solverRunners.iterator(); iter.hasNext();) {
      final ISolverRunner solverRunner = iter.next();
      try {
        solverRunner.run(observer, new SubProgressMonitor(monitor, 1));
        oneRunnerSucceeded = true;
      } catch (SetUpException except) {
        exceptContainer.addException(except);
      }
    }

    if (!exceptContainer.isEmpty()) {
      if (oneRunnerSucceeded) {
        SafeLogger.severe("Set up before analyzer run failed.", exceptContainer);
      } else {
        throw exceptContainer;
      }
    }
  }

  // --- Public services

  public void addSolverRunner(final ISolverRunner solverRunner) {
    this.solverRunners.add(solverRunner);
  }

  public boolean isEmpty() {
    return this.solverRunners.isEmpty();
  }

  // --- Private code

  private final Collection<ISolverRunner> solverRunners = new Stack<ISolverRunner>();

}
