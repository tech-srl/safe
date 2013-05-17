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
 * Name: APExample1.java
 * Author: Eran Yahav (eyahav)
 *
 * Reference variables f1, f2, f4 will have the same value number
 * in DOMO IR. So this kind of intraprocedural copying-based must-aliases is
 * apparent (and immediate) in the DOMO SSA IR.
 *
 * Expected Result: This case should report a true alarm.
 *********************************************************************/

package j2se.typestate.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * A variant of SWTExample1 in which font is disposed. No errors should be
 * reported.
 * 
 * @author yahave
 * 
 */
public class SWTExample2 {

  public static void main(String[] args) {
    Display display = new Display();

    Font font = new Font(display, "Courier", 10, SWT.NORMAL);
    Shell shell = new Shell(display);
    shell.open();
    GC gc = new GC(shell);

    gc.setFont(font);

    gc.drawText("Hello SWT World!", 0, 0);

    gc.dispose();

    font.dispose();
  }
}
