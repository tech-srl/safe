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
 * Name: IteratorExample1.java
 * Description: A correct usage of iterator.
 * Expected Result: this case should not report any alarms.
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.typestate.iterator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Unique escape reports a false positive on this due to polymorphism in the
 * calls to hasNext() and next().
 * 
 * AP should handle this correctly and report no false positives, by using must
 * info to prune unreachable calls.
 * 
 * LocalMMN should also handle this correctly with no false positives.
 */
public class IteratorExample10 {

  public static void main(String[] args) {
    MyMap l1 = new MyMap();
    Map l2 = new MyMap();

    l1.put("foo", "foo");
    l1.put("moo", "moo");
    l1.put("zoo", "zoo");

    for (Iterator it1 = l1.iterator(); it1.hasNext();) {
      System.out.println(it1.next());
    }
  }

  private static class MyMap extends HashMap {
    public Iterator iterator() {
      if (isEmpty()) {
        return Collections.EMPTY_LIST.iterator();
      } else {
        return keySet().iterator();
      }
    }
  }
}
