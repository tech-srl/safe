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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A correct program that the unique-escape engine should analyze correctly, but
 * should lead to a false positive with the unique engine without live analysis
 * 
 * @author sfink
 */
public class IteratorExample9 {

  public static void main(String[] args) {
    List l1 = new ArrayList();

    while (true) {
      foo(l1);
    }
  }

  public static void foo(List l1) {
    l1.add("foo");
    l1.add("moo");
    l1.add("zoo");

    for (Iterator it1 = l1.iterator(); it1.hasNext();) {
      Object item = it1.next();
    }
  }
}
