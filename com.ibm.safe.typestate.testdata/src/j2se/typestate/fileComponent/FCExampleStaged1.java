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
 * @author Eran Yahav (eyahav)
 *
 * Reference variables f1, f2, f4 will have the same value number
 * in DOMO IR. So this kind of intraprocedural copying-based must-aliases is
 * apparent (and immediate) in the DOMO SSA IR.
 *
 * Expected Result: This case should report a true alarm.
 *********************************************************************/
public class FCExampleStaged1 {

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent();
    f1.read(); // benign stmt by localMNN

    FileComponent f2 = new FileComponent(); // benign instance by localMNN
    f2.read();
    f2.close();

    foo(f1);
    bar(f1);
  }

  public static void foo(FileComponent x) {
    x.read(); // should not be benign by localMNN
  }

  public static void bar(FileComponent x) {
    x.close(); // should not be benign by localMNN
  }
}
