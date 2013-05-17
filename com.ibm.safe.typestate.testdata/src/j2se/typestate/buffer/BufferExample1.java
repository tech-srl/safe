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
 * Name: BufferExample1.java
 * Description: An incorrect usage of nio.Buffer.
 * Expected Result: this case should report a true alarm.
 * Author: Eran Yahav (eyahav)
 *********************************************************************/

package j2se.typestate.buffer;

import java.nio.ByteBuffer;

public class BufferExample1 {

  public static void main(String[] args) {
    byte[] bytes = new byte[42];
    ByteBuffer buf = ByteBuffer.wrap(bytes);
    buf.reset();
  }
}
