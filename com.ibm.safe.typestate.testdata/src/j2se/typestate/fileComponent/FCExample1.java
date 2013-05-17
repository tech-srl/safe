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
 * Reference variables f1, f2, f4 will have the same value number
 * in DOMO IR. So this kind of intraprocedural copying-based must-aliases is
 * apparent (and immediate) in the DOMO SSA IR.
 *
 * Expected Result: This case should report a true alarm.
 *********************************************************************/
public class FCExample1 {

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent();
    FileComponent f2;
    FileComponent f3;
    FileComponent f4;

    f2 = f1;
    f3 = null;
    f4 = f1;

    f1.close();
    f4.read();
  }
}
