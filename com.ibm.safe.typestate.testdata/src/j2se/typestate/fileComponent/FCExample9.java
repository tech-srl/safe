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
 * Author: Eran Yahav (eyahav)
 * Expected Result: In this case, an error should be reported.
 *********************************************************************/
public class FCExample9 {
  static FileComponent x;

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent(); // FileComponent Site #1
    FileComponent f2 = new FileComponent(); // FileComponent Site #2
    x = f1;
    x.close();
    f2 = x;
    f2.read();
  }
}
