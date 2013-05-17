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

/*********************************************************************
 * Name: APExample11.java
 * Author: Eran Yahav (eyahav)
 * This testcase tests reuse across allocation sites
 * Expected Result: In this case, an error should be reported.
 *********************************************************************/
public class FCExample11 {

  static FileComponent x;

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent(); // FileComponent Site #1
    FileComponent f2 = new FileComponent(); // FileComponent Site #2
    FileComponent f3 = new FileComponent(); // FileComponent Site #3
    x = f1;
    x.close();
    x.read();
    x = f2;
    x.close();
    x.read();
    x = f3;
    x.close();
    x.read();
  }
}
