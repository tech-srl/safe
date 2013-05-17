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
package com.ibm.safe.internal.exceptions;

public final class AssertionFailedException extends RuntimeException {

  public AssertionFailedException(final String message) {
    super(message);
  }

  public AssertionFailedException(final Throwable exception) {
    super(exception);
  }

  public AssertionFailedException(final String message, final Throwable exception) {
    super(message, exception);
  }

  // --- Private code

  private static final long serialVersionUID = 156546457567657L;

}
