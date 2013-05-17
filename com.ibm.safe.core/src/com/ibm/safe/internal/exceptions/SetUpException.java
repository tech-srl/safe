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

public final class SetUpException extends SafeException {

  public SetUpException(final String message) {
    super(message);
  }

  public SetUpException(final String message, final Throwable cause) {
    super(message, cause);
  }

  // --- Private code

  private static final long serialVersionUID = 156456453576L;

}
