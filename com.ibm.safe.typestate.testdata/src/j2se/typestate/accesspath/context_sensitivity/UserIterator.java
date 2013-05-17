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
package j2se.typestate.accesspath.context_sensitivity;

import java.util.Iterator;

final class UserIterator implements Iterator {

  public UserIterator(final String[] theMethods) {
    this.methods = theMethods;
  }

  public boolean hasNext() {
    return this.index < this.methods.length;
  }

  public Object next() {
    return this.methods[this.index++];
  }

  public void remove() {
    // Do nothing !
  }

  private final String[] methods;

  private int index;

}
