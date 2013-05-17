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
package j2se.typestate.fileComponent;

import j2se.typestate.accesspath.OpFileComponent;

import java.util.Random;

/*********************************************************************
 * Author: Eran Yahav (eyahav)
 * This testcase checks join and strong-updates where may-information exists.
 * Note: this testcases uses OpFileComponent, which is a file component that is created in a closed state,
 *       and has to be explicitly opened before being used.
 * Expected Result: In this case, no error should be reported.
 *********************************************************************/
public class FCExample12 {

  static Random r = new Random();

  static int aNumber = r.nextInt();

  static OpFileComponent x;

  public static void main(String[] args) {
    OpFileComponent f1 = new OpFileComponent(); // OpFileComponent Site #1
    OpFileComponent f2 = new OpFileComponent(); // OpFileComponent Site #2
    x = f2;
    x.open();
    if (aNumber > 42) {
      x = f1;
    }
    // at this point, if the phi node merges tuples for x, we will have x as
    // may-point-to
    // to either f1 or f2 (that is, on each separation pass), leading to a
    // weak-update
    // that preserves the non-open state of the component.
    x.open();
    x.read();
  }
}
