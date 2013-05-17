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

class KernelBenchmark1Small {

  Random r = new Random();

  int anumber = r.nextInt();

  // ***********************************************************************
  // TEST1: Concatenation of kernels
  // ***********************************************************************
  public void test1() {

    // ***********************************************************************
    // Example 1: Trivial correct usage
    Vector v1 = new Vector();
    v1.add("aha");
    Iterator it1 = v1.iterator();
    it1.next(); // No error;

    // ***********************************************************************
    // Example 2: Trivial incorrect usage.
    if (anumber == 3) {
      Vector v2 = new Vector();
      Iterator it2 = v2.iterator();
      v2.add("aha");
      it2.next(); // Error: concurrent modification
    }

    // ***********************************************************************
    // Example 3: Correct usage example. Illustrates problems with
    // merging multiple iterators into one abstract iterator.
    Vector v3 = new Vector();
    ;
    while (this.anumber == 17) {
      v3.add("aha");
      Iterator it3 = v3.iterator();
      while (it3.hasNext())
        it3.next(); // No error.
    }

    // ***********************************************************************
    // Example 4:
    // The following example shows possible problems in merging iterators by
    // allocation site ignoring calling context ... e.g., since the iterator
    // is created inside a procedure "s.iterator()" both iterators may be
    // abstracted into one, creating problems.
    Vector v4 = new Vector();
    Iterator it4;

    it4 = v4.iterator();
    while (it4.hasNext())
      it4.next(); // No error.

    v4.add("aha");

    it4 = v4.iterator();
    while (it4.hasNext()) {
      it4.next(); // No error.
    }

    // ***********************************************************************
    // Example 5: Trivial correct usage of modification through iterator.
    Vector v5 = new Vector();
    v5.add("aha");
    v5.add("oho");

    Iterator it5 = v5.iterator();
    while (it5.hasNext()) {
      it5.next(); // No error.
      it5.remove();
    }

    // ***********************************************************************
    // Example 7: Correct usage of multiple iterators.
    Vector v7a = new Vector();
    Vector v7b = new Vector();
    v7a.add("aha");
    v7a.add("oho");
    v7b.add("aha");
    v7b.add("oho");

    Iterator it7a = v7a.iterator();
    while (it7a.hasNext()) {
      Object o7a = it7a.next(); // No error.
      Iterator it7b = v7b.iterator();
      while (it7b.hasNext()) {
        Object o7b = it7b.next(); // No error.
        if (anumber == 17)
          it7b.remove();
      }
    }

  }

  public static void main(String[] args) {
    KernelBenchmark1Small k = new KernelBenchmark1Small();
    k.test1();
  }

}
