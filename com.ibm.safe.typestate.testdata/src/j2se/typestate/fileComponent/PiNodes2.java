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

import java.util.Random;

/*********************************************************************
 * Description: This is intended to check correctness of accesspath engine
 *              in the presence of Pi-nodes and Phi-nodes.
 * Expected Result: this test should report an error.
 * 
 * @author Eran Yahav (eyahav)
 *********************************************************************/
public class PiNodes2 {

  static Random r = new Random();

  static int aNumber = r.nextInt();

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent(); // FileComponent Site #1
    FileComponentContainer c1 = new FileComponentContainer(); // FileComponentContainer
                                                              // Site #1
    FileComponent f2;
    FileComponent f3;

    c1.theComponent = f1; // { (I,open,A:{},U:{f1,c1.theComponent}) }

    f2 = c1.theComponent; // { (I,open,A:{},U:{f1,c1.theComponent,f2}) }

    f2.close(); // { (I,closed,A:{},U:{f1,c1.theComponent,f2}) }

    if (aNumber > 42) { // { (I,closed,A:{},U:{f1,c1.theComponent,f2}) }
      f3 = f2; // { (I,closed,A:{},U:{f1,c1.theComponent,f2,f3}) }
    } else {
      f3 = f2; // { (I,closed,A:{},U:{f1,c1.theComponent,f2,f3}) }
    }
    // phi // { (I,closed,A:{f3},U:{f1,c1.theComponent,f2}) }

    f3.read(); // { (I,err,A:{},U:{f1,c1.theComponent,f2,f3}) }
  }
}
