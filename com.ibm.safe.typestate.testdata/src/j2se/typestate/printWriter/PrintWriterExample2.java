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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Stack;

/**
 * 
 * 
 * @author egeay
 */
public final class PrintWriterExample2 {

  private static Stack classWorkList = new Stack();

  public static void main(String[] args) {
    classWorkList.push(args[0]);

    while (!classWorkList.isEmpty()) {
      final String name = (String) classWorkList.pop();
      PrintWriter fWriter = oopen(name, 1000);
      fWriter.close();
    }
  }

  static PrintWriter oopen(final String fileName, int bufsize) {
    File f = new File(fileName);

    if (f.exists() && !f.canWrite()) {
      abort(fileName + ": cannot write");
    }

    try {
      PrintWriter d = new PrintWriter(new BufferedOutputStream(new FileOutputStream(f), bufsize));
      d.println("/*  " + f.getName() + " -- from Java class " + fileName + "  */");
      return d;
    } catch (Exception e) {
      abort(fileName + ": cannot open for output");
    }
    return null;
  }

  static void abort(String s) {
    System.err.print("   ");
    System.err.println(s);
    System.exit(1);
  }

  static void write(final PrintWriter writer) {
    writer.println();
    writer.println("blah blah");
  }

}
