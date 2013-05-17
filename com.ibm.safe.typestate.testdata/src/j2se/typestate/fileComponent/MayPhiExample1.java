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

import java.util.Random;

/*********************************************************************
 * Author: Eran Yahav (eyahav)
 * Description: both branches of a conditional assign the same value to x.
 * x is then used for opennind and reading. 
 * Can our phi function account for the fact that "x" AND "MayPhiExample1.sf" are
 * must-points-to ? 
 *********************************************************************/
public class MayPhiExample1 {

  static Random r = new Random();

  OpFileComponent sf;

  public static void main(String[] args) {
    OpFileComponent f1 = new OpFileComponent(); // FileComponent Site #1
    OpFileComponent x;

    MayPhiExample1 mpx = new MayPhiExample1();

    mpx.sf = f1;

    if (r.nextInt() > 1) {
      x = mpx.sf;
    } else {
      x = mpx.sf;
    }

    x.open();
    x.read();
  }
}
