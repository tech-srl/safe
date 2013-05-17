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

class KernelBenchmark3Small {

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
    // System.out.println("Example4");
    //
    // v1 = new Vector();
    // hv1 = new HoldsVector();
    // hv1.v = v1;
    //
    // it1 = hv1.v.iterator();
    // hit1 = new HoldsIterator();
    // hit1.i = it1;
    // while (hit1.i.hasNext())
    // hit1.i.next(); // No error.
    //
    // hv1.v.add("aha");
    //
    // it1 = hv1.v.iterator();
    // hit1 = new HoldsIterator();
    // hit1.i = it1;
    // while (hit1.i.hasNext()) {
    // hit1.i.next(); // No error.
    // }

  }

  public static void main(String[] args) {
    KernelBenchmark3Small k = new KernelBenchmark3Small();
    k.test3();
  }

}
