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

/*
 * Created on Aug 11, 2003
 */

/**
 * @author eyahav
 */
public class PathSimple_m1 {

  static Random r = new Random();

  static int aNumber = r.nextInt();

  public static void main(String[] args) {

    boolean flag;

    FileComponent x = new FileComponent();
    if (aNumber >= 41) {
      x.close();
      flag = false;
    } else {
      flag = true;
    }

    if (!flag)
      x.read();
  }
}
