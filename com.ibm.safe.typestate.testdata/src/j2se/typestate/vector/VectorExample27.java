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
 * The current AP solver gets a false positive on this. A similar problem causes
 * 32 ugly false positives on bcel with EmptyStack rule.
 * 
 * APMustMustNot should handle this correctly.
 * 
 * @author sfink
 */
public final class VectorExample27 {

  static Cell s;

  public static void main(String[] args) {
    Cell c = new Cell();
    s = c; // pollutes global may-allias paths to the Cell object
    Vector v = null;
    for (int i = 0; i < 2; i++) {
      // put allocation in a loop to confuse unique logic
      v = new Vector();
    }
    c.v = v; // we give up on must-complete bit because may-alias path s->v is
    // not represented
    v.add(new Object());
    v.firstElement(); // we look stupid
  }

  private static class Cell {
    Vector v;
  }
}