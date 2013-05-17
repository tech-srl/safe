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
 * Name: APExample7.java
 * Author: Eran Yahav (eyahav)
 * Expected Result: In this case, no error should be reported.
 *********************************************************************/
public class FCExample7 {

  static Random r = new Random();

  static int aNumber = r.nextInt();

  static FileComponent x;

  static Object y;

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent(); // FileComponent Site #1
    FileComponent f2 = new FileComponent(); // FileComponent Site #2
    Object o1 = new Object();
    Object o2 = new Object();
    x = f1;
    if (aNumber > 1) {
      y = o1;
    } else {
      y = o2;
    }
    x.close();
    f2.read();
  }
}
