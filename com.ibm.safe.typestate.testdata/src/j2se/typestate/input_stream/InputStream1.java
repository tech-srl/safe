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
 * Name: InputStream1.java
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.typestate.input_stream;

import java.io.FileInputStream;

public class InputStream1 {

  public static void main(String[] args) {
    String fileName = "E:/tmp/test.file";
    try {
      FileInputStream f = new FileInputStream(fileName);
      f.close();
      f.read();
    } catch (Exception e) {
      System.out.println("Error !");
    }
  }
}
