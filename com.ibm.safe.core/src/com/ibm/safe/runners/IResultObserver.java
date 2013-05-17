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
package com.ibm.safe.runners;

import com.ibm.safe.internal.runners.AbstractSolverRunner.AnalysisStatus;
import com.ibm.safe.reporting.message.ISolverResult;

/**
 * Implementation of this interface are notified of solver results.
 * Responsabilities are implementation-specific but globally they should
 * collect/process relevant information from the results they receive.
 * 
 * @author egeay
 */
public interface IResultObserver {

  /**
   * Method called by SAFE solver runners once they receive some results.
   * 
   * @param result
   *            The {@link ISolverResult} instance that contains the resulting
   *            data.
   */
  public void notify(final ISolverResult result, final AnalysisStatus status);

}
