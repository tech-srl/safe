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
 * Name: StagingExample1.java
 * @author Eran Yahav (eyahav)
 *********************************************************************/
public class StagingExample1 {

  static Random r = new Random();

  static OpFileComponent fld;

  public static void main(String[] args) {
    OpFileComponent f1 = new OpFileComponent(); // FileComponent Site #1
    f1.open();
    readFile(f1);

    OpFileComponent f2 = new OpFileComponent(); // FileComponent Site #2

    OpFileComponent x;

    if (r.nextBoolean()) {
      fld = f1; // confuse unique via field
    } else {
      fld = f2;
    }

    x = fld;
    x.open();
    x.read(); // LocalMMN gets me (but not unique)
  }

  public static void readFile(OpFileComponent r) {
    r.read(); // Unique gets me (but not localMMN)
  }
}
