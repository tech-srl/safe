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
package j2se.typestate.iterator;

import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

public class IteratorExample11 {

  /**
   * @param args
   */
  public static void main(String[] args) {
    final Set one = Collections.singleton(new Object());
    final Iterator it = one.iterator();
    PrintStream myStream = new PrintStream(System.out) {
      public void println(String s) {
        super.println(s);
        while (it.hasNext()) {
          it.next();
        }
      }
    };
    System.setOut(myStream);
    foo(it);
  }

  public static void foo(Iterator it) {
    while (it.hasNext()) {
      System.out.println("got one");
      it.next();
    }
  }

}
