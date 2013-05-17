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

class KernelBenchmark4 {

  Random r = new Random();

  int anumber = r.nextInt();

  // ***********************************************************************
  // TEST4: Store all vectors in a data structure
  // ***********************************************************************

  HoldsVector t1 = new HoldsVector();

  HoldsVector t2 = new HoldsVector();

  public HoldsVector gethv1() {
    return t1;
  };

  public HoldsVector gethv2() {
    return t1.nextV;
  };

  HoldsIterator ti1 = new HoldsIterator();

  HoldsIterator ti2 = new HoldsIterator();

  HoldsIterator ti3 = new HoldsIterator();

  public HoldsIterator gethit1() {
    return ti1;
  };

  public HoldsIterator gethit2() {
    return ti1.nextI;
  };

  public HoldsIterator gethit3() {
    return ti1.nextI.nextI;
  };

  public void test4() {

    Vector v1;
    Vector v2;
    Iterator it1;
    Iterator it2;
    Iterator it3;
    Object o1;
    Object o2;

    // ***********************************************************************
    // Initializations
    t2.nextV = null;
    t1.nextV = t2;

    ti3.nextI = null;
    ti2.nextI = ti3;
    ti1.nextI = ti2;

    // ***********************************************************************
    // Example 1
    gethv1().v = new Vector();
    gethv1().v.add("aha");
    gethit1().i = gethv1().v.iterator();
    gethit1().i.next(); // No error;

    // ***********************************************************************
    // Example 2
    if (anumber == 12) {
      gethv1().v = new Vector();
      gethit1().i = gethv1().v.iterator();
      gethv1().v.add("aha");
      gethit1().i.next(); // Error: concurrent modification
    }

    // ***********************************************************************
    // Example 3
    gethv1().v = new Vector();
    while (anumber == 17) {
      gethv1().v.add("aha");
      gethit1().i = gethv1().v.iterator();
      while (gethit1().i.hasNext())
        gethit1().i.next(); // No error.
    }

    // ***********************************************************************
    // Example 4
    gethv1().v = new Vector();

    gethit1().i = gethv1().v.iterator();
    while (gethit1().i.hasNext())
      gethit1().i.next(); // No error.

    gethv1().v.add("aha");

    gethit1().i = gethv1().v.iterator();
    while (gethit1().i.hasNext()) {
      gethit1().i.next(); // No error.
    }

    // ***********************************************************************
    // Example 5: Trivial correct usage of modification through iterator.
    gethv1().v = new Vector();
    gethv1().v.add("aha");
    gethv1().v.add("oho");

    gethit1().i = gethv1().v.iterator();
    while (gethit1().i.hasNext()) {
      gethit1().i.next(); // No error.
      gethit1().i.remove();
    }

    // ***********************************************************************
    // Example 7: Correct usage of multiple iterators.
    gethv1().v = new Vector();
    gethv2().v = new Vector();
    gethv1().v.add("aha");
    gethv1().v.add("oho");
    gethv2().v.add("aha");
    gethv2().v.add("oho");

    gethit1().i = gethv1().v.iterator();
    while (gethit1().i.hasNext()) {
      o1 = gethit1().i.next(); // No error.
      gethit2().i = gethv2().v.iterator();
      while (gethit2().i.hasNext()) {
        o2 = gethit2().i.next(); // No error.
        if (anumber == 17)
          gethit2().i.remove();
      }
    }

    // ***********************************************************************
    // Example 8: Incorrect usage of multiple iterators.
    if (anumber == 13) {

      gethv1().v = new Vector();
      gethv1().v.add("aha");
      gethv1().v.add("oho");

      gethit1().i = gethv1().v.iterator();
      while (gethit1().i.hasNext()) {
        o1 = gethit1().i.next(); // Error: concurrent modification
        gethit2().i = gethv1().v.iterator();
        while (gethit2().i.hasNext()) {
          o2 = gethit2().i.next(); // No error.
          if (anumber == 17)
            gethit2().i.remove();
        }
      }
    }

    // ***********************************************************************
    // Example 9: Iterators are heap-allocated objects too.
    if (anumber == 16) {

      gethv1().v = new Vector();
      gethv1().v.add("aha");
      gethv1().v.add("oho");

      gethit1().i = gethv1().v.iterator();
      gethit2().i = gethit1().i;
      gethit3().i = gethv1().v.iterator();
      gethit1().i.next();
      gethit1().i.remove();
      gethit2().i.next(); // Okay ... it2 and it3 are the same.
      gethit3().i.next(); // Error ... concurrent modification.
    }
  }

  public static void main(String[] args) {
    KernelBenchmark4 k = new KernelBenchmark4();
    k.test4();
  }

}
