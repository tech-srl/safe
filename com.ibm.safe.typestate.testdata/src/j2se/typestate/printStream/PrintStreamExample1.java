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
package j2se.typestate.printStream;

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * "No action after close" : should produce 2 true warnings with any typestate
 * engine. "Must close before exit" : > Base: 1 > Unique: 1 (due to exceptional
 * path) > UniqueEscape: 0 (determine that PrintStream escapes on exit?)
 * 
 * @author Eran Yahav
 */
public final class PrintStreamExample1 {

  public static void main(String[] args) {
    try {
      FileOutputStream out = new FileOutputStream("foo.txt");
      PrintStream p = new PrintStream(out);
      p.close();
      p.println("foo!");
      p.write(42);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
