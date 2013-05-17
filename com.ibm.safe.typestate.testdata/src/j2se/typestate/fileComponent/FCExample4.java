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
 *
 * Expected Result: no error should be reported
 *********************************************************************/
public class FCExample4 {

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent();
    FileComponentContainer c1;
    FileComponent f2 = new FileComponent();
    FileComponentContainer c2;

    c1 = new FileComponentContainer(f1);
    c2 = new FileComponentContainer(f2);

    c2.component().read();
    c1.component().close();
  }
}
