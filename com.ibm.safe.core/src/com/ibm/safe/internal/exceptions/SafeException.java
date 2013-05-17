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

/**
 * The class SafeException and its subclasses identifies exceptions that are
 * intentionally thrown by SAFE application to report different kinds of errors
 * that may happen during a run and that cannot be simply logged.
 * 
 * @author egeay
 */
public class SafeException extends Exception {

  /**
   * Constructs a new SAFE exception with the specified detail message.
   * 
   * @param message
   *            The detail message. The detail message is saved for later
   *            retrieval by the Throwable.getMessage() method.
   */
  public SafeException(final String message) {
    super(message);
  }

  /**
   * Constructs a new SAFE exception with the specified detail message and
   * cause. Note that the detail message associated with cause is not
   * automatically incorporated in this exception's detail message.
   * 
   * @param message
   *            The detail message, which is saved for later retrieval by the
   *            Throwable.getMessage() method.
   * @param cause
   *            The cause, which is saved for later retrieval by the
   *            Throwable.getCause() method. A null value is permitted, and
   *            indicates that the cause is nonexistent or unknown.
   */
  public SafeException(final String message, final Throwable cause) {
    super(message, cause);
  }

  // --- Private code

  protected SafeException() {
  }

  private static final long serialVersionUID = 6546467567567L;

}
