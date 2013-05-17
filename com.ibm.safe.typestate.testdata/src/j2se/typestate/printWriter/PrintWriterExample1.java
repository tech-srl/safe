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
package j2se.typestate.printWriter;

import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * Should produce 2 true warnings with any typestate engine.
 * 
 * @author Eran Yahav
 */
public final class PrintWriterExample1 {

  public static void main(String[] args) {
    try {
      FileOutputStream out = new FileOutputStream("foo.txt");
      PrintWriter p = new PrintWriter(out);
      p.close();
      p.println("foo!");
      p.write(42);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
