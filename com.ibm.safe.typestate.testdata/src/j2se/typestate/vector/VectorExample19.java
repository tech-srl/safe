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
 * This has a true error which the unsound must engine will miss due lack of
 * must alias.
 */
public final class VectorExample19 {

  public static void main(String[] args) {
    Vector v1 = new Vector();
    Cell c1 = new Cell();
    Cell c2 = (args.length > 1) ? c1 : new Cell();
    c1.v = v1;
    c2.v.get(0);

  }

  private static class Cell {
    Vector v;
  }

}