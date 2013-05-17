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
 * Name: IteratorExample6.java
 * Description: An incorrect usage of iterator, it1.next() is
 * called before checking it1.hasNext().
 * Expected Result: this case should produce a real alarm.
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.typestate.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorExample6 {

  public void dummy(int[] args) {
    List l1 = new ArrayList();
    List l2 = new ArrayList();

    Iterator it1 = l1.iterator();
    Object item = it1.next();
    it1.hasNext();
    foo(it1);
  }

  public static void foo(Iterator it) {
    System.out.println(it.toString());
  }

}
