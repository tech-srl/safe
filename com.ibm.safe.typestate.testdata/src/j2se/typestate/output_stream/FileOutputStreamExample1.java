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

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Should produce 1 true warning with Base TypeState engine.
 * 
 * @author egeay
 */
public final class FileOutputStreamExample1 {

  public static void main(String[] args) {
    try {
      FileOutputStream f1 = new FileOutputStream("C:\\temp\\foo.txt");
      foo(f1);
      bar(f1);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void foo(FileOutputStream f) {
    try {
      f.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void bar(FileOutputStream f) {
    try {
      f.write(12);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
