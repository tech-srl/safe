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

public class IPNullDerefWithCondition {

  public IPNullDerefWithCondition() {
  }

  public static void main(String[] args) {

    IPNullDerefWithCondition example = create(1);

    if (example != null) {
      int i = example.fooTrue(5); // not error
    } else {
      int j = example.fooFalse(5); // error
    }
    int k = example.fooTrue(5); // error

  }

  private static IPNullDerefWithCondition flipflop(IPNullDerefWithCondition a) {
    if (a != null)
      return null;
    else
      return a;
  }

  private static IPNullDerefWithCondition id(IPNullDerefWithCondition e) {
    return e;
  }

  public static IPNullDerefWithCondition create(int i) {
    IPNullDerefWithCondition o = new IPNullDerefWithCondition();
    if (i < 5) {
      return o;
    } else {
      return null;
    }
  }

  public int fooTrue(int j) {
    return j * j;
  }

  public int fooFalse(int j) {
    return j * j;
  }

}
