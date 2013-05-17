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
 * Should produce a false positive with base engine, but not with access path
 * 
 * Actually .. it does produce a false positive with AP, since it does not think
 * Cell is a tracked type until too late.
 * 
 * @author Stephen Fink
 */
public final class VectorExample8 {

  public static void main(String[] args) {
    try {
      Vector v1 = new Vector();
      Vector v2 = new Vector();

      init(v1, v2);

      Cell head = new Cell();
      Cell tail = head;
      for (int i = 0; i < 100; i++) {
        tail.next = new Cell();
        tail = tail.next;
      }
      head.next.next.next.next.v = v1;
      head.next.next.next.v = v2;

      head.next.next.next.next.v.firstElement();

    } catch (Exception e) {
      e.printStackTrace();
    }
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
    Cell next;

    Vector v;
  }
}