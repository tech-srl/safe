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
/*******************************************************************************
 * Name: APDoubleClose1.java Author: Eran Yahav (eyahav)
 ******************************************************************************/

package j2se.typestate.accesspath;

import j2se.typestate.fileComponent.FileComponent;

public class APDoubleClose1 {

  static FileComponent x;

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent(); // FileComponent Site #1
    FileComponent f2 = new FileComponent(); // FileComponent Site #2
    // x = f1;
    // x.read();
    x = f2; // x should be killed from AS1's must-set
    x.close();
    x.close();
  }
}
