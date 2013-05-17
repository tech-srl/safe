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
package j2se.typestate.vector;

import java.util.Vector;

/**
 * @author sfink
 * 
 * Has no true error, but AP currently reports a false positive because the
 * checkcast processing can't deal with access-paths of length > 1
 */
public final class VectorExample23 {

  public static void main(String[] args) {
    Vector v = null;
    for (int i = 0; i < 2; i++) {
      // put allocation in a loop to defeat unique logic
      v = new Vector();
    }
    Cell c = new Cell();
    c.v = v;
    foo(c, v);
  }

  private static class Cell {
    Vector v;
  }

  private static void foo(Object o, Vector v) {
    if (o instanceof Cell) {
      Cell c = (Cell) o;
      c.v.add(new Object());
      v.firstElement();
    }
  }
}