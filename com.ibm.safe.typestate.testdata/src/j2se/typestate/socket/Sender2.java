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
package j2se.typestate.socket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;

class Sender2 {

  public Sender2() {
    System.out.println("created");
  }

  public static Socket createSocket() {
    if ((new Random()).nextBoolean()) {
      return new Socket();
    } else {
      return new Socket();
    }
  }

  public static void main(String[] args) throws IOException {
    InetAddress addr = InetAddress.getByName("java.sun.com");
    Socket s = createSocket();
    s.connect(new InetSocketAddress(addr, 80));
    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
  }
}
