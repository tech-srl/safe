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

/**
 * Thrown when handling properties breaks
 * 
 * @author yahave
 * 
 */
public class PropertiesException extends SafeException {

  private static final long serialVersionUID = -2415404226044948224L;

  public PropertiesException(String message) {
    super(message);
  }

  public PropertiesException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

  public PropertiesException() {
    // TODO Auto-generated constructor stub
  }

}
