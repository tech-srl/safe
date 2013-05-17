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
 * Name: APKillPaths.java
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.typestate.accesspath;

import j2se.typestate.fileComponent.FileComponent;

public class APKillPaths {

  static FileComponent x;

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent(); // FileComponent Site #1
    FileComponent f2 = new FileComponent(); // FileComponent Site #2
    FileComponent f3 = new FileComponent(); // FileComponent Site #3
    FileComponent f4 = new FileComponent(); // FileComponent Site #4
    FileComponent f5 = new FileComponent(); // FileComponent Site #5
    FileComponent f6 = new FileComponent(); // FileComponent Site #6
    FileComponent f7 = new FileComponent(); // FileComponent Site #7
    x = f1;
    // x.close(); // AS1 becomes closed
    x.read(); // err
    x = f2; // x should be killed from AS1's must-set
    x.close();
    x.close();
    // x.read();
  }
}
