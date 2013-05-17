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
 * This exception class is thrown when analysis is cancelled by a progress
 * monitor or something else.
 * 
 * @author egeay
 */
public final class CancelationWorkException extends SafeException {

  private static final long serialVersionUID = -1098947200549782350L;

  /**
   * Constructs a {@link CancelationWorkException} with the message transmitted.
   * 
   * @param message
   *            The message indicated which task has been cancelled.
   */
  public CancelationWorkException(final String message) {
    super(message);
  }

}
