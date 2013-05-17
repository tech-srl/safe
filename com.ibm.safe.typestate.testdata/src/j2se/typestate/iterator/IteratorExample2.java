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
/*********************************************************************
 * Name: IteratorExample2.java
 * Description: A correct usage of iterator,
 * current engine should be capable of identifying the iterator allocation
 * site as a non-summary allocation site, and therefore should be
 * able to apply strong-updates to the iterator object state.
 * Expected Result: this case should not report a false alarm.
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.typestate.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorExample2 {

  public static void main(String[] args) {
    List l1 = new ArrayList();
    List l2 = new ArrayList();

    l1.add("foo");
    l1.add("moo");
    l1.add("zoo");

    for (Iterator it1 = l1.iterator(); it1.hasNext();) {
      Object item = it1.next();
      foo(it1);
    }
  }

  public static void foo(Iterator it) {
    System.out.println(it.toString());
  }

}
