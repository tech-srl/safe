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
package j2se.typestate.ipnullderef;

import java.util.Random;

public class IPNullDeref1 {

  Object inner;

  public IPNullDeref1() {
    if (new Random().nextBoolean())
      inner = null;
    else
      inner = new Object();
  }

  public static void main(String[] args) {

    IPNullDeref1 example = create(1);

    int i = example.foo(5);
    checkme();
  }

  public static IPNullDeref1 create(int i) {
    IPNullDeref1 o = new IPNullDeref1();
    if (i < 5) {
      return o;
    } else {
      return null;
    }
  }

  public int foo(int j) {
    return j * j;
  }

  private static void checkme() {
    IPNullDeref1 o = new IPNullDeref1();
    o.setInner();
    o.inner.toString(); // should not report error. For this we must handle the
                        // call to return and pass
    // only those that do not change (locals)
  }

  private void setInner() {
    inner = new Object();
  }

}
