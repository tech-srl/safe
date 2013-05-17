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
 * @author Eran Yahav (eyahav)
 *********************************************************************/
public class StagingExample2 {

  static Random r = new Random();

  public static void main(String[] args) {
    OpFileComponent f1 = new OpFileComponent(); // FileComponent Site #1
    f1.open();

    readFile(f1); // move this to point (*) and kaboom

    OpFileComponent f2 = new OpFileComponent(); // FileComponent Site #2

    OpFileComponent x;

    if (r.nextBoolean()) {
      x = f1;
    } else {
      x = f2;
      x.open();
      x.read();
    }

    // point (*) - don't forget to rebuild

  }

  public static void readFile(OpFileComponent r) {
    r.read();
  }

}
