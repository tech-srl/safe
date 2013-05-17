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
package com.ibm.safe.runners;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.wala.util.CancelException;

/**
 * Implementation of this interface are able to run a given
 * {@link com.ibm.safe.runners.AnalysisNature} analysis, or more precisely to
 * run a given {@link com.ibm.safe.controller.ISafeSolver} implementation.
 * 
 * @author egeay
 */
public interface ISolverRunner {

	/**
	 * Should be called to start solver analysis.
	 * 
	 * @param observer
	 *            The observer that basically will be called by this runner once
	 *            it receives some results from it's underlying solver.
	 * @param monitor
	 *            Useful to report progress of SAFE analysis.
	 * @throws SafeException
	 *             Occurs for all cases of bad configuration for the given
	 *             solver to run.
	 * @throws CancelException
	 * @throws CoreException 
	 * @throws IllegalArgumentException 
	 */
	public void run(final IResultObserver observer,
			final IProgressMonitor monitor) throws SafeException,
			CancelException, IllegalArgumentException, CoreException;

}
