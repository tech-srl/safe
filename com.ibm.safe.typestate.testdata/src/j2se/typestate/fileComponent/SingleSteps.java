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
 * Expected Result: a true error should be reported
 * @author Eran Yahav (eyahav)
 *********************************************************************/
public class SingleSteps {

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent(); // FileComponent Site #1
    FileComponentContainer c1 = new FileComponentContainer(); // FileComponentContainer
                                                              // Site #1
    FileComponent f2;

    c1.theComponent = f1;

    f2 = c1.theComponent;

    f2.close();
    f2.read();
  }
}
