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
/**
 * A correct usage of iterator.
 * This case should not report any alarms with any interprocedural
 * solver as strong as unique.  Local solver will be confused and report a false alarm.
 */

package j2se.typestate.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorExample13 {

  public static void main(String[] args) {
    List l1 = new ArrayList();
    List l2 = new ArrayList();

    l1.add("foo");
    l1.add("moo");
    l1.add("zoo");

    for (Iterator it1 = l1.iterator(); it1.hasNext();) {
      bar(it1);
      System.out.println(it1.next());
    }
  }

  public static void bar(Iterator it) {
    // something to confuse slicing
    it.hasNext();
  }
}
