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
 * Description: both branches of a conditional assign the same value to x.
 * x is then used for opennind and reading. 
 * Can our phi function account for the fact that "x" AND "MayPhiExample1.sf" are
 * must-points-to ?
 *  
 * @author Eran Yahav (eyahav)
 *********************************************************************/
public class MayPhiExample2 {

  static Random r = new Random();

  public OpFileComponent sf;

  public OpFileComponent sf2; // ,sf3;

  public static void main(String[] args) {
    OpFileComponent f1 = new OpFileComponent(); // FileComponent Site #1
    OpFileComponent x, y;

    MayPhiExample2 mpx = new MayPhiExample2();

    mpx.sf = f1;

    if (r.nextInt() > 1) {
      x = mpx.sf;
    } else {
      x = mpx.sf;
    }

    mpx.sf2 = x;
    // y = mpx.sf2;

    // mpx.sf3 = f1;

    x.open();
    x.read();
  }
}
