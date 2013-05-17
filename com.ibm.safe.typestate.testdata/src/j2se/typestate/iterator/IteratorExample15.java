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
import java.util.Iterator;

/**
 * An example that defeats naive unification-based mining
 */
public class IteratorExample15 {

  public static void main(String[] args) {
    bar();
  }

  public static void bar() {
    foo(Collections.singleton("ugh").iterator());
    // at this program point, we've unified two states
    // next and hasNext. this will cause problems in bar 2.
    bar2(false);
  }

  public static void bar2(boolean b) {
    Iterator it = Collections.singleton("ugh").iterator();
    it.hasNext();
    if (b) {
      it.hasNext();
    }
    // we get here and need to unify two states that are hasNext.
    // but previously, we unified one of these states with a next
    // state. as a result, we wind up in hasNext or next after
    // the merge, which is a problem.
    it.next();
  }

  public static void foo(Iterator it) {
    if (it.hasNext()) {
      return;
    }
    it.next();
  }
}
