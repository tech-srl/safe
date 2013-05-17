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

public class IPNullDerefsNotFound {

  public IPNullDerefsNotFound() {
  }

  public static void main(String[] args) {

    IPNullDerefsNotFound example = create(1);

    int i = example.foo(5);

    IPNullDerefsNotFound example2 = create2(2);

    if (example2 != null) {
      example2.foo(5);
    }

  }

  public static IPNullDerefsNotFound create(int i) {
    IPNullDerefsNotFound o = new IPNullDerefsNotFound();
    return null;

  }

  public static IPNullDerefsNotFound create2(int i) {
    IPNullDerefsNotFound o = new IPNullDerefsNotFound();
    if (i < 5) {
      return o;
    } else {
      return null;
    }
  }

  public int foo(int j) {
    return j * j;
  }

}
