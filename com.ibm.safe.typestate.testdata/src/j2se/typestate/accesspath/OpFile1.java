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
 * Name: OpFile1.java
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.typestate.accesspath;

public class OpFile1 {

  public static void main(String[] args) {
    int i = 0;
    int x = 3;
    while (i < 5) {
      OpFileComponent f = new OpFileComponent();
      if (x > i) {
        f.open();
        f.read();
      }
      i++;
      x--;
    }
  }
}
