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
 * Name: APExample5.java
 * Author: Eran Yahav (eyahav)
 *
 * A variant of APExample2.
 *
 * Two file components are created and being placed into two different
 * file-component containers. Calls are then made through the file-component-containers
 * to close one filecomponent, and read from the other (which is still open).
 * Expected Result: In this case, no error should be reported.
 *********************************************************************/

package j2se.typestate.accesspath;

import j2se.typestate.fileComponent.FileComponent;
import j2se.typestate.fileComponent.FileComponentContainer;

public class APComplicatedExample1 {

  public static void main(String[] args) {

    FileComponent f1 = new FileComponent(); // FileComponent Site #1
    FileComponentContainer c1;
    FileComponentContainer c2;
    FileComponent f3;

    c1 = new FileComponentContainer(); // FileComponentContainer Site #1
    c2 = c1;

    c1.theComponent = f1; /*
                           * {c1.theComponent,f1} should be in the must and
                           * c2.theComponet is in the may
                           */
    f3 = c1.component(); // f3 should be in must
    f3.close(); // definite close
    f3 = c2.component(); // f3 should be in may
    f3.read(); // we should get an error
  }

}
