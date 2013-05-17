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
 * Name: IteratorExample4.java
 * Description: Unique should now get no false alarm with improved
 * intraprocedural live range analysis
 * 
 * LocalMMN should not produce the false alarm.
 * 
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.typestate.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IteratorExample4 {

  public static void main(String[] args) {
    List l1 = new ArrayList();
    List l2 = new ArrayList();

    while (true) {

      l1.add("foo");
      l1.add("moo");
      l1.add("zoo");

      for (Iterator it1 = l1.iterator(); it1.hasNext();) {
        Object item = it1.next();
      }
    }
  }
}
