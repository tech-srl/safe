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

import java.util.Collection;
import java.util.HashSet;

public class IPNullDerefSimpleFields {

  Collection fClean;

  Collection fError;

  public IPNullDerefSimpleFields() {

  }

  public static void main(String[] args) {

    IPNullDerefSimpleFields example = create(1);

    example.fClean.add("hi"); // no error

    IPNullDerefSimpleFields example2 = create2(2);

    example2.fError.add("there"); // error

    IPNullDerefSimpleFields example3 = create3(1);
    example3.fError = new HashSet(); // error

  }

  public static IPNullDerefSimpleFields create(int i) {
    IPNullDerefSimpleFields o = new IPNullDerefSimpleFields();
    o.fClean = new HashSet();
    return o;

  }

  public static IPNullDerefSimpleFields create2(int i) {
    IPNullDerefSimpleFields o = new IPNullDerefSimpleFields();
    if (i > 5)
      o.fError = null;
    else
      o.fError = new HashSet();
    return o;
  }

  public static IPNullDerefSimpleFields create3(int i) {
    IPNullDerefSimpleFields o = new IPNullDerefSimpleFields();
    if (i > 5) {
      return o;
    } else
      return null;

  }

  public int foo(int j) {
    return j * j;
  }

}
