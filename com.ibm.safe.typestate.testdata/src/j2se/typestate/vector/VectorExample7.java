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
 * Should produce a false positive with current engines, but not with a
 * path-sensitive access path engine
 * 
 * @author Stephen Fink
 */
public final class VectorExample7 {

  public static void main(String[] args) {
    try {
      Vector v1 = new Vector();
      Vector v2 = new Vector();

      v1.add(new Object());
      v2.removeAllElements();

      Cell c1 = new Cell();

      boolean b = (new Random()).nextBoolean();
      if (b) {
        c1.v = v1;
        Vector v3 = c1.v;
        v3.firstElement();
      } else {
        c1.v = v2;
      }
      if (b) {
        Vector v3 = c1.v;
        v3.firstElement();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static class Cell {
    Vector v;
  }
}