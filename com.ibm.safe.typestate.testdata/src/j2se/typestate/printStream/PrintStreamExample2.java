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
import java.util.Random;

/**
 * Should produce 1 true warnings with any typestate engine.
 * 
 * @author Eran Yahav
 */
public final class PrintStreamExample2 {

  public static void main(String[] args) {
    try {
      FileOutputStream out = new FileOutputStream("foo.txt");
      PrintStream p = new PrintStream(out);
      p.println("foo!");
      p.write(42);

      if ((new Random()).nextBoolean()) {
        // otherwise, currently slicing kills me
        p.close();
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
