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
package com.ibm.safe.controller;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.safe.internal.exceptions.MaxFindingsException;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.internal.exceptions.SolverTimeoutException;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.reporting.message.ISolverResult;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;

/**
 * Safe Solver.
 * Created on Dec 15, 2004
 * @author Eran Yahav (yahave)
 */
public interface ISafeSolver {
  /**
   * perform the analysis.
   * 
   * @param monitor
   *            Useful to report progress of SAFE analysis.
   * @return a SafeSolverResult containing the results of the analysis.
   * @throws WalaException
   * @throws SolverTimeoutException
   * @throws MaxFindingsException
   * @throws SetUpException
   * @throws SetUpException
   * @throws CancelException
   */
  public ISolverResult perform(final IProgressMonitor monitor) throws WalaException, SolverTimeoutException, MaxFindingsException,
      PropertiesException, SetUpException, CancelException;

  /**
   * @return a reporting manager
   */
  public IReporter getReporter();
}