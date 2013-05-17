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

class KernelBenchmark3 {

  Random r = new Random();

  int anumber = r.nextInt();

  // ***********************************************************************
  // TEST3: Store stuff in the heap
  // ***********************************************************************
  public void test3() {

    Vector v1;
    HoldsVector hv1;
    Vector v2;
    HoldsVector hv2;
    Iterator it1;
    HoldsIterator hit1;
    Iterator it2;
    HoldsIterator hit2;
    Iterator it3;
    HoldsIterator hit3;
    Object o1;
    Object o2;

    // ***********************************************************************
    // Example 1
    System.out.println("Example1");
    v1 = new Vector();
    hv1 = new HoldsVector();
    hv1.v = v1;
    hv1.v.add("aha");
    it1 = hv1.v.iterator();
    hit1 = new HoldsIterator();
    hit1.i = it1;
    hit1.i.next(); // No error;

    // ***********************************************************************
    // Example 2
    if (anumber == 12) {

      System.out.println("Example2");
      v1 = new Vector();
      hv1 = new HoldsVector();
      hv1.v = v1;
      it1 = hv1.v.iterator();
      hit1 = new HoldsIterator();
      hit1.i = it1;
      hv1.v.add("aha");
      hit1.i.next(); // Error: concurrent modification
    }

    // ***********************************************************************
    // Example 3
    System.out.println("Example3");
    v1 = new Vector();
    hv1 = new HoldsVector();
    hv1.v = v1;
    while (anumber == 17) {
      hv1.v.add("aha");
      it1 = hv1.v.iterator();
      hit1 = new HoldsIterator();
      hit1.i = it1;
      while (hit1.i.hasNext())
        hit1.i.next(); // No error.
    }

    // ***********************************************************************
    // Example 4
    System.out.println("Example4");

    v1 = new Vector();
    hv1 = new HoldsVector();
    hv1.v = v1;

    it1 = hv1.v.iterator();
    hit1 = new HoldsIterator();
    hit1.i = it1;
    while (hit1.i.hasNext())
      hit1.i.next(); // No error.

    hv1.v.add("aha");

    it1 = hv1.v.iterator();
    hit1 = new HoldsIterator();
    hit1.i = it1;
    while (hit1.i.hasNext()) {
      hit1.i.next(); // No error.
    }

    // ***********************************************************************
    // Example 5: Trivial correct usage of modification through iterator.
    System.out.println("Example5");
    v1 = new Vector();
    hv1 = new HoldsVector();
    hv1.v = v1;
    hv1.v.add("aha");
    hv1.v.add("oho");

    it1 = hv1.v.iterator();
    hit1 = new HoldsIterator();
    hit1.i = it1;
    while (hit1.i.hasNext()) {
      hit1.i.next(); // No error.
      hit1.i.remove();
    }

    // ***********************************************************************
    // Example 7: Correct usage of multiple iterators.

    System.out.println("Example7");
    v1 = new Vector();
    hv1 = new HoldsVector();
    hv1.v = v1;
    v2 = new Vector();
    hv2 = new HoldsVector();
    hv2.v = v2;
    hv1.v.add("aha");
    hv1.v.add("oho");
    hv2.v.add("aha");
    hv2.v.add("oho");

    it1 = hv1.v.iterator();
    hit1 = new HoldsIterator();
    hit1.i = it1;
    while (hit1.i.hasNext()) {
      o1 = hit1.i.next(); // No error.
      it2 = hv2.v.iterator();
      hit2 = new HoldsIterator();
      hit2.i = it2;
      while (hit2.i.hasNext()) {
        o2 = hit2.i.next(); // No error.
        if (anumber == 17)
          hit2.i.remove();
      }
    }

    // ***********************************************************************
    // Example 8: Incorrect usage of multiple iterators.
    if (anumber == 312) {

      System.out.println("Example8");
      v1 = new Vector();
      hv1 = new HoldsVector();
      hv1.v = v1;
      hv1.v.add("aha");
      hv1.v.add("oho");

      it1 = hv1.v.iterator();
      hit1 = new HoldsIterator();
      hit1.i = it1;
      while (hit1.i.hasNext()) {
        o1 = hit1.i.next(); // Error: concurrent modification
        it2 = hv1.v.iterator();
        hit2 = new HoldsIterator();
        hit2.i = it2;
        while (hit2.i.hasNext()) {
          o2 = hit2.i.next(); // No error.
          if (anumber == 17)
            hit2.i.remove();
        }
      }
    }
    // ***********************************************************************
    // Example 9: Iterators are heap-allocated objects too.
    if (anumber == 123) {
      System.out.println("Example9");
      v1 = new Vector();
      hv1 = new HoldsVector();
      hv1.v = v1;
      hv1.v.add("aha");
      hv1.v.add("oho");

      it1 = v1.iterator();
      hit1 = new HoldsIterator();
      hit1.i = it1;
      hit2 = hit1;
      it3 = v1.iterator();
      hit3 = new HoldsIterator();
      hit3.i = it3;
      hit1.i.next();
      hit1.i.remove();
      hit2.i.next(); // Okay ... it2 and it3 are the same.
      hit3.i.next(); // Error ... concurrent modification.
    }
  }

  public static void main(String[] args) {
    KernelBenchmark3 k = new KernelBenchmark3();
    k.test3();
  }

}
