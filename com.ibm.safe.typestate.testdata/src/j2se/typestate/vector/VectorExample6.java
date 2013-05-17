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

import java.util.Random;
import java.util.Vector;

/**
 * Should produce a false positive with base engine, but not with access path
 * 
 * @author Stephen Fink
 */
public final class VectorExample6 {

  public static void main(String[] args) {
    try {
      Vector v1 = new Vector();
      Vector v2 = new Vector();

      init(v1, v2);

      Cell c1 = new Cell();

      boolean b = (new Random()).nextBoolean();
      if (b) {
        c1.v = v1;
        foo(c1);
      } else {
        c1.v = v2;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @param c1
   */
  private static void foo(Cell c1) {
    Vector v3 = c1.v;
    v3.firstElement();
  }

  /**
   * @param v1
   * @param v2
   */
  private static void init(Vector v1, Vector v2) {
    v1.add(new Object());
    v2.removeAllElements();
  }

  private static class Cell {
    Vector v;
  }
}