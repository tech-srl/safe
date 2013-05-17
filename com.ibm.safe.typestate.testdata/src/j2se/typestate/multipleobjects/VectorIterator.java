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
package j2se.typestate.multipleobjects;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

class VectorIterator {

  public static int anumber;

  public static void main(String[] args) {

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
    while (anumber == 17) {
      v3.add("aha");
      Iterator it3 = v3.iterator();
      while (it3.hasNext())
        it3.next(); // No error but got false alarm
    }

    // ***********************************************************************
    // Example 4: Trivial incorrect usage with type-hierarchy
    if (anumber == 4) {
      Collection c2;
      c2 = new HashSet();
      Iterator it2 = c2.iterator();
      c2.add("aha");
      it2.next(); // Error: concurrent modification
    }

  }

}