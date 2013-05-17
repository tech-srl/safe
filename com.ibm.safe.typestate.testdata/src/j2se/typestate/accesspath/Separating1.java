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
/*********************************************************************
 * Name: Separating1.java
 * Description: This example is meant to perform a stress-test of the
 * non-separating engine vs. the separating engine that analyses each
 * allocation site separately.
 * Expected Result: this case should report a true alarm due to the
 * possibility of f1.close() being called before f1.read().
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.typestate.accesspath;

import j2se.typestate.fileComponent.FileComponent;

import java.util.Random;

public class Separating1 {

  static Random r = new Random();

  static int aNumber = r.nextInt();

  public static void main(String[] args) {

    FileComponent f1 = new FileComponent();
    FileComponent f2 = new FileComponent();
    FileComponent f3 = new FileComponent();
    FileComponent f4 = new FileComponent();
    FileComponent f5 = new FileComponent();
    FileComponent f6 = new FileComponent();
    FileComponent f7 = new FileComponent();
    FileComponent f8 = new FileComponent();
    FileComponent f9 = new FileComponent();
    FileComponent f10 = new FileComponent();
    FileComponent f11 = new FileComponent();

    if (aNumber > 1) {
      f1.close();
    }

    if (aNumber > 2) {
      f2.close();
    }

    if (aNumber > 3) {
      f3.close();
    }

    if (aNumber > 4) {
      f4.close();
    }

    if (aNumber > 5) {
      f5.close();
    }

    if (aNumber > 6) {
      f6.close();
    }

    if (aNumber > 7) {
      f7.close();
    }

    if (aNumber > 8) {
      f8.close();
    }

    if (aNumber > 9) {
      f9.close();
    }

    if (aNumber > 10) {
      f10.close();
    }

    if (aNumber > 11) {
      f11.close();
    }

    f1.read();
  }
}
