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
package com.ibm.safe.internal.exceptions;

import com.ibm.safe.reporting.message.ISolverResult;

/**
 * @author sfink
 * @author eyahav
 * 
 * an exception used to indicate that the solver should not report any more
 * findings
 * 
 */
public class MaxFindingsException extends SafeException {

  private static final long serialVersionUID = 1L;

  private final ISolverResult result;

  public MaxFindingsException(ISolverResult result) {
    super();
    this.result = result;
  }

  public ISolverResult getResult() {
    return result;
  }

}
