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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

class KernelBenchmark6 {

  static Random r = new Random();

  static int anumber = r.nextInt();

  // ***********************************************************************
  // Simple example
  // ***********************************************************************
  public static void main(String[] args) {

    Set s = new HashSet(); // create a new set
    s.add("aha");
    s.add("oho");
    s.add("eho");

    Iterator i1 = s.iterator(); // create two iterators over set s
    Iterator i2 = s.iterator();
    Iterator i3 = i1;

    i1.next(); // iterate to the next element

    i1.remove(); // update v via i1; other iterators on v invalidated

    if (anumber == 17)
      i2.next(); // CME thrown

    if (anumber == 18)
      i3.next(); // i3 still valid; CME not thrown

    s.add("a new string"); // all iterators over s invalidated

    if (anumber == 19)
      i1.next(); // CME thrown
  }
}
