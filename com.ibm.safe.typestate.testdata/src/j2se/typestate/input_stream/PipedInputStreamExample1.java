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

import java.io.IOException;
import java.io.PipedInputStream;

/**
 * Should produce two warnings.
 * 
 * @author egeay
 */
public final class PipedInputStreamExample1 {

  public static void main(final String[] args) {
    PipedInputStream pStream = null;
    try {
      pStream = new PipedInputStream();
      int data = pStream.read(); // Hit !
      // ...
      // Do something with data.
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (pStream != null) {
        try {
          pStream.close(); // Hit !
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
