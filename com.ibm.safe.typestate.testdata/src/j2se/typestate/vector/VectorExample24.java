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
 * A test designed to throw the AP engine into infinite recursion unless
 * something is done to avoid it.
 * 
 * Has one true error.
 * 
 * This example also shows an issue with the AP solver. We go to a lot of work
 * to compute the aliasing induced by the for-loop, but in the end, none of that
 * aliasing matters. Can we avoid this work somehow?
 */
public final class VectorExample24 {

  public static void main(String[] args) {
    Vector v = new Vector();
    Cell c = new Cell();
    c.v = v;
    Cell first = c;
    for (int i = 0; i < args.length; i++) {
      Cell temp = new Cell();
      temp.next = first;
      first = temp;
    }
    c.v.firstElement();
  }

  private static class Cell {
    Vector v;

    Cell next;
  }
}