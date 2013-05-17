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
 * Name: OpFile2.java
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.typestate.accesspath;

public class OpFile2 {

  public static void main(String[] args) {
    OpFileComponent f = new OpFileComponent();
    int x = 5;
    int y = 0;
    if (x > y + 1) {
      f.open();
    }
    f.open();
    f.read();
  }
}
