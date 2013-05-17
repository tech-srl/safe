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
package j2se.typestate.vector;

import java.util.Vector;

/**
 * @author sfink
 * 
 * A test of recursion. Has one true error.
 * 
 * Exposes a bug in our original slicing: See bug 45924.
 * 
 * Note that if args.length > 1, this program never terminates, and we'd report
 * no findings since we'd never propagate facts out of the infinite loop. This
 * means we can't stupidly slice away B.bar without taking appropriate
 * precautions.
 */
public final class VectorExample26 {

  public static void main(String[] args) {
    Vector v = new Vector();
    A a = (args.length > 1) ? new A() : new B();
    foo(v, a);
    v.removeAllElements();
    v.firstElement();
  }

  private static void foo(Vector v, A a) {
    a.bar(v);
  }

  static class A {
    void bar(Vector v) {
      foo(v, this);
      v.add(new Object());
    }
  }

  static class B extends A {
    void bar(Vector v) {
      return;
    }
  }
}