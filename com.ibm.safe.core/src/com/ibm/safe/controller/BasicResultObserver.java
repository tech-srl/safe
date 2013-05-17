/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.controller;

import java.util.Iterator;

import com.ibm.safe.internal.runners.AbstractSolverRunner.AnalysisStatus;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.reporting.message.ISolverResult;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.runners.IResultObserver;

public final class BasicResultObserver implements IResultObserver {

  public BasicResultObserver(final IReporter safeReporter) {
    this.reporter = safeReporter;
  }

  public void notify(final ISolverResult result, final AnalysisStatus status) {
    this.reporter.reportAnalysisStatus(status);
    updateStatus(status);
    if (result != null) {
      for (Iterator<? extends Message> iter = result.getMessages().iterator(); iter.hasNext();) {
        this.reporter.reportMessage(iter.next());
      }
      this.numberOfFindings += result.getMessages().size();
    }
  }

  /**
   * updates aggregate status
   * 
   * @param status
   */
  private void updateStatus(AnalysisStatus status) {
    if (this.status == AnalysisStatus.INCOMPLETE) {
      this.status = status;
    } else if (this.status == AnalysisStatus.NORMAL) {
      if (status == AnalysisStatus.ABORT_TIMEOUT || status == AnalysisStatus.ABORT_MAX_FIND) {
        this.status = status;
      }
    }
  }

  public int getNumberOfFindings() {
    return this.numberOfFindings;
  }

  private final IReporter reporter;

  private int numberOfFindings;

  private AnalysisStatus status = AnalysisStatus.INCOMPLETE;

  public AnalysisStatus getAnalysisStatus() {
    return status;
  }

}