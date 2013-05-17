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
package j2se.typestate.generalized.cmp;

import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

class KernelBenchmark7 {

  static Random r = new Random();

  static int anumber = r.nextInt();

  // ***********************************************************************
  // Example showing a case when pointer analysis won't be enough
  // ***********************************************************************

  public static void main(String[] args) {

    Vector S1 = new Vector(); // create a new vector
    S1.add("aha");
    S1.add("oho");
    S1.add("eho");

    Vector S2 = new Vector(); // create a new vector
    S2.add("aha");
    S2.add("oho");
    S2.add("eho");

    Iterator x, y;

    if (anumber == 12) {
      x = S1.iterator();
      y = x;
      // Now, x->S1 and y->S1 are true
    } else {
      x = S2.iterator();
      y = x;
      // Here, x->S2 and y->S2 holds true.
    }

    x.next();
    x.remove();
    y.next();
  }
}
