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
 * Should produce 1 warning with Base TypeState engine. Tests recursion.
 * 
 * @author Stephen Fink
 */
public final class VectorExample14 {

  public static void main(String[] args) {
    try {
      Vector v = new Vector();
      v.add(new Object());
      foo(v);
      v.firstElement();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void foo(Vector v) {
    if (new Random().nextBoolean()) {
      // defeat slicing.
      foo(v);
      v.removeAllElements();
    } else {
      return;
    }
  }
}