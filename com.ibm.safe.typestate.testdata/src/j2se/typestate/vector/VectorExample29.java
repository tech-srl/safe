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
 * This test is designed to demonstrate the precision inversion between
 * UniqueEscape and MustMustNot.
 * 
 * UniqueEscape should report 0 findings, while the current MMN should report 1.
 * Update: I've now strengthened the AP solvers so that all AP solvers should
 * report 0.
 * 
 * @author sfink
 */
public final class VectorExample29 {

  static Cell s;

  public static void main(String[] args) {
    Cell c = new Cell();
    s = c; // pollutes global may-allias paths to the Cell object
    Vector v = new Vector();
    c.v = v; // we give up on must-complete bit because may-alias path s->v is
    // not represented
    s.v.add(new Object());
    v.firstElement(); // we look stupid. MMN fails because it doesn't have must
    // info for the s.v path
  }

  private static class Cell {
    Vector v;
  }
}