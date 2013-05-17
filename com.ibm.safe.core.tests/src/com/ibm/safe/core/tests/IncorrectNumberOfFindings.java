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
package com.ibm.safe.core.tests;

import com.ibm.safe.internal.exceptions.SafeException;

public class IncorrectNumberOfFindings extends SafeException {

  /**
   * generated serialVersionUID
   */
  private static final long serialVersionUID = -3233793404576763550L;

  private final int expectedFindings;

  private final int actualFindings;

  public IncorrectNumberOfFindings(int expectedFindings, int actualFindings) {
    this.expectedFindings = expectedFindings;
    this.actualFindings = actualFindings;
  }

  public String getMessage() {
    return "Expected: " + expectedFindings + " Actual: " + actualFindings;
  }

}
