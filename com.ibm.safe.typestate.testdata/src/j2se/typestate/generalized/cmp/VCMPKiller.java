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
import java.util.Set;

class PointerToObject {
  PointerToObject pto;

  VCMPKiller hi;
}

public class VCMPKiller {
  public Iterator i1, i2;

  public Set s1, s2;

  static final int K = 30;

  public static void main(String[] args) {
    VCMPKiller hIters = new VCMPKiller();

    hIters.s1 = new HashSet();
    hIters.s1.add("aha");
    hIters.s1.add("oho");
    hIters.s1.add("uhu");

    hIters.s2 = new HashSet();
    hIters.s2.add("aha");
    hIters.s2.add("oho");
    hIters.s2.add("uhu");

    hIters.i1 = hIters.s1.iterator();
    hIters.i2 = hIters.s2.iterator();

    // L1:
    // after that statement versions of i1 and i2 will be blured by
    // most heap analysis algorithms
    PointerToObject pt = setVCMPKillerAtDepthK(K, hIters);

    Iterator i = getI1FieldFromDepthK(K, pt);

    // our analysis doesn't produce a false alarm, but
    // any heap analysis algorithm that uses the r_by until
    // the depth K (even considering fields) will fail,
    // because at L1 the version numbers of two CollectionObjects
    // will be blured together
    i.next(); // CME is not thrown
  }

  // Function creates a link list of objects of PointerToObject type. The list
  // contains (depth - 1) such object. The last object points to hiters
  public static PointerToObject setVCMPKillerAtDepthK(int depth, VCMPKiller hiters) {

    PointerToObject pToFirst = new PointerToObject();
    PointerToObject pToLast = pToFirst;

    for (int i = 0; i <= depth; i++) {
      PointerToObject p = new PointerToObject();
      pToLast.pto = p;
      pToLast = p;
    }

    pToLast.hi = hiters;
    return pToFirst;
  }

  public static Iterator getI1FieldFromDepthK(int depth, PointerToObject pto) {
    PointerToObject p = pto;
    for (int i = 0; i <= depth; i++) {
      p = p.pto;
    }

    return p.hi.i1;

  }

}
