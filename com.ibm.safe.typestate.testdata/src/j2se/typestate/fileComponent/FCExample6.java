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
 * Expected Result: In this case, no error should be reported.
 *********************************************************************/
public class FCExample6 {

  static Random r = new Random();

  static int aNumber = r.nextInt();

  static FileComponent x;

  static FileComponent y;

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent(); // FileComponent Site #1
    FileComponent f2 = new FileComponent(); // FileComponent Site #2

    if (aNumber > 1) {
      x = f1;
      y = f2;
    } else {
      x = f2;
      y = f1;
    }
    x.close();
    y.read();
  }
}
