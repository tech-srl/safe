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

/**
 * 
 * @author Eran Yahav (yahave)
 * 
 */
class Benchmark {

  public static void main(String[] args) {
    Set s = new HashSet();
    s.add("value");
    Loop(s);
  }

  public static void Loop(Set set) {
    Iterator i = set.iterator();
    while (i.hasNext()) {
      String str = (String) i.next();
      set.add(str + "new");
    }
  }
}