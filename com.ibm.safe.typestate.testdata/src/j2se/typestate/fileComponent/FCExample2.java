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

/**
 * Author: Eran Yahav (eyahav)
 *
 * Two file components are created and being placed into two different
 * file-component containers. Calls are then made through the file-component-containers
 * to close one filecomponent, and read from the other (which is still open).
 * Expected Result: With perfect alias analysis, no error should be reported.
 * 
 * The basic AP engine should report a false positive since it will have to
 * rely on the global points-to to determine the contents of the component().
 **/
public class FCExample2 {

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent();
    FileComponentContainer c1;
    FileComponent f2 = new FileComponent();
    FileComponentContainer c2;

    c1 = new FileComponentContainer(f1);
    c2 = new FileComponentContainer(f2);

    c1.component().close();
    c2.component().read();
  }
}
