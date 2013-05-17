/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.accesspath;


import com.ibm.wala.ipa.callgraph.propagation.AbstractPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;

/**
 * An element in an access path representing a local pointer.
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public class LocalPathElement implements PointerPathElement {

  /**
   * A unique identifier of a local pointer variable in the program.
   */
  private AbstractPointerKey lpk;

  /**
   * create a new anchor element
   * 
   * @param lpk -
   *            anchor element to serve as head of path
   */
  public LocalPathElement(AbstractPointerKey lpk) {
    this.lpk = lpk;
    assert(lpk != null);
  }


  @Override
  public String toString() {
    return lpk.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (o != null && getClass().equals(o.getClass())) {
      LocalPathElement other = (LocalPathElement) o;
      return lpk.equals(other.lpk);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return 5351 * lpk.hashCode();
  }


  public boolean isAnchor() {
    return true;
  }

  public PointerKey getPointerKey() {
    return lpk;
  }
}
