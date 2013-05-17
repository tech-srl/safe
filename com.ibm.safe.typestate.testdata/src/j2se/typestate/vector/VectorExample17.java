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
 * Has no true error. AP engine should not report a false positive.
 * 
 * Note: need k >= 2 to pass this!
 * 
 * @author Stephen Fink
 */
public final class VectorExample17 {

  public static void main(String[] args) {
    Vector v1 = null;
    for (int i = 0; i < 2; i++) {
      // put in a loop to defeat unique logic
      v1 = new Vector();
    }
    Cell c = new Cell();
    c.v = v1;
    foo(c);
    v1.get(0);
  }

  private static void foo(Cell c) {
    c.v.add(new Object());
  }

  private static class Cell {
    public Vector v;
  }
}