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
package j2se.typestate.input_stream;

import java.io.FileInputStream;

/**
 * Should produce 1 true warning even with Base TypeState engine.
 * 
 * @author egeay
 */
public final class FileInputStreamExample2 {

  public static void main(String[] args) {
    try {
      FileInputStream f1 = new FileInputStream("C:\\temp\\foo.txt");
      f1.close();
      f1.read();
    } catch (Exception e) {
      // do nothing
    }
  }
}
