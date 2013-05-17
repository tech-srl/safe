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
 * A variant of Example2.
 *
 * Two file components are created and being placed into two different
 * file-component containers. Calls are then made through the file-component-containers
 * to close one filecomponent, and read from the other (which is still open).
 * Expected Result: In this case, no error should be reported.
 *********************************************************************/
public class FCExample5 {

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent(); // FileComponent Site #1
    FileComponentContainer c1;
    FileComponent f2 = new FileComponent(); // FileComponent Site #2
    FileComponentContainer c2;
    FileComponent f3;
    FileComponent f4;

    c1 = new FileComponentContainer(); // FileComponentContainer Site #1
    c2 = new FileComponentContainer(); // FileComponentContainer Site #2

    c1.theComponent = f1;
    c2.theComponent = f2;

    // changed method call to direct-field access to avoid FA. [EY]
    // f3 = c1.component();
    f3 = c1.theComponent;
    f3.close();
    // changed method call to direct-field access to avoid FA. [EY]
    // f4 = c2.component();
    f4 = c2.theComponent;
    f4.read();
  }
}
