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

import java.util.Iterator;

/**
 * Unique engine will succeed only with fine-grained intraprocedural live
 * analysis
 */
public class IteratorExample16 {

  public static void main(String[] args) {
    for (Iterator it1 = new MyIterator(); it1.hasNext();) {
      it1.next();
      Iterator it2 = new MyIterator();
      // need to introduce a procedure call to defeat the
      // "must be unique in node" logic.
      foo(it2);
    }
  }

  private static void foo(Iterator it2) {
    while (it2.hasNext()) {
      it2.next();
    }
  }

  private static class MyIterator implements Iterator {
    Object sillyFieldToAvoidSmushing;

    public void remove() {
    }

    public boolean hasNext() {
      return false;
    }

    public Object next() {
      return null;
    }

  }
}
