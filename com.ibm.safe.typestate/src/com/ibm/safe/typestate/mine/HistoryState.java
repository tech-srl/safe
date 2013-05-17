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
package com.ibm.safe.typestate.mine;

public class HistoryState {

  public final Object name;

  public final int count;

  public HistoryState(Object name, int count) {
    this.name = name;
    this.count = count;
  }

  public int hashCode() {
    return name.hashCode() + count;
  }

  public boolean equals(Object other) {
    if (!(other instanceof HistoryState))
      return false;

    HistoryState otherHS = (HistoryState) other;
    return name.equals(otherHS.name) && (count == otherHS.count);
  }

  public String toString() {
    return name.toString() + count;
  }

}
