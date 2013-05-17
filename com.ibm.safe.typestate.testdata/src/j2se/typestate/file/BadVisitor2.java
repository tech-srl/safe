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
package j2se.typestate.file;

import java.util.Iterator;
import java.util.Vector;

/**
 * 
 * @author yahave
 *
 */
public class BadVisitor2 {
  public static void main(String args[]) {
    Vector v = new Vector();
    v.add("one");
    v.add("two");
    v.add("three");
    v.add("four");

    Iterator iter = v.iterator();
    while (iter.hasNext()) {
      String s = (String) iter.next();
      if (s.equals("two"))
        v.remove("two");
      else {
        // Visit
        System.out.println(s);
      }
    }

    // see what's left
    System.out.println("What's really there...");
    iter = v.iterator();
    while (iter.hasNext()) {
      String s = (String) iter.next();
      System.out.println(s);
    }
  }
}
