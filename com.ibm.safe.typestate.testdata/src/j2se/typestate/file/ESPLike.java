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
package j2se.typestate.file;

import java.util.Random;

/**
 * @author eyahav
 */
public class ESPLike {

  static Random r = new Random();

  static int aNumber = r.nextInt();

  public static void main(String[] args) {

    boolean p1, p2, p3, p4, p5, p6, flag;

    FileComponent f1 = new FileComponent();
    if (aNumber >= 41) {
      p1 = true;
      f1.close();
    } else {
      p1 = false;
    }

    FileComponent f2 = new FileComponent();
    if (aNumber >= 42) {
      p2 = true;
      f2.close();
    } else {
      p2 = false;
    }

    FileComponent f3 = new FileComponent();
    if (aNumber >= 43) {
      p3 = true;
      f3.close();
    } else {
      p3 = false;
    }

    FileComponent f4 = new FileComponent();
    if (aNumber >= 44) {
      p4 = true;
      f4.close();
    } else {
      p4 = false;
    }

    FileComponent f5 = new FileComponent();
    if (aNumber >= 45) {
      p5 = true;
      f5.close();
    } else {
      p5 = false;
    }

    FileComponent f6 = new FileComponent();
    if (aNumber >= 46) {
      p6 = true;
      f6.close();
    } else {
      p6 = false;
    }

    FileComponent r = new FileComponent();

    if (aNumber >= 45) {
      flag = true;
      r.close();
    } else {
      flag = false;
    }

    if (!flag) {
      r.read();
    }
  }
}
