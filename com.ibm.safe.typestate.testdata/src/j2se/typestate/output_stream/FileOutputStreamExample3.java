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
package j2se.typestate.output_stream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class FileOutputStreamExample3 {

  public static void main(String[] args) {
    try {
      FileOutputStream f1 = new FileOutputStream("C:\\temp\\foo.txt");
      FileOutputStream f2 = new FileOutputStream("C:\\temp\\foo2.txt");
      FileInputStream i1 = new FileInputStream("C:\\temp\\fooinput.txt");
      balance(i1, f1, f2);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * without path sensitivity, seems like o1 may be written to after closed and
   * a finding should be reported.
   * 
   * @param i
   * @param o1
   * @param o2
   */
  public static void balance(InputStream i, FileOutputStream o1, FileOutputStream o2) {
    final int threshold = 100;
    int c = 0;
    try {
      int r = i.read();
      while (r != -1) {
        if (c++ < threshold) {
          o1.write(r);
          if (c == threshold) {
            o1.close();
          }
        } else {
          o2.write(r);
        }
        r = i.read();
      }
    } catch (IOException e) {
      throw new RuntimeException("Bang!");
    }
  }

}
