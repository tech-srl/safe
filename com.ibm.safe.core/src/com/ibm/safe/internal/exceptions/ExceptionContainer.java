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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public final class ExceptionContainer extends SafeException {

  // --- Overridden methods

  public String getMessage() {
    if (this.exceptionContainer.size() == 1) {
      return this.exceptionContainer.iterator().next().getMessage();
    } else {
      return getCompoundMessage();
    }
  }

  public void addException(final SafeException anException) {
    this.exceptionContainer.add(anException);
  }

  public boolean isEmpty() {
    return this.exceptionContainer.isEmpty();
  }

  private String getCompoundMessage() {
    final StringBuffer buf = new StringBuffer();
    int counter = 0;
    for (Iterator<SafeException> iter = this.exceptionContainer.iterator(); iter.hasNext();) {
      final SafeException exception = iter.next();
      buf.append('\n').append(++counter).append(": ").append(exception.getMessage()); //$NON-NLS-1$
    }
    return buf.toString();
  }

  private final Collection<SafeException> exceptionContainer = new ArrayList<SafeException>(10);

  private static final long serialVersionUID = 13564576567688L;

}
