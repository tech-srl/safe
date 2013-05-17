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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This example should demonstrate: 1. an instance verified by FI solver (a.k.a.
 * DFA slicing) 2. an instance veriifed by localMMN 3. an instance verified by
 * some interproc engine with strong-updates 4. a statement verified as valid by
 * localMMN, saving work for a following engine (e.g., unique) that will get
 * another statement.
 * 
 * We should be able to compare the running times of the various solvers on this
 * example and report them in the overview section.
 * 
 * @author yahave
 * 
 */
class Sender {

  public static Socket createSocket() {
    return new Socket();
  }

  public static Collection createSockets() {
    Collection result = new ArrayList();
    for (int i = 0; i < 5; i++) {
      result.add(new Socket());
    }
    return result;
  }

  /**
   * InputStream in this method will be sliced away, since it has not close
   * operation invoked on it, thus it will be killed by DFA slicing.
   */
  public static Collection readMessages() {
    Collection result = new ArrayList();
    try {
      FileInputStream f1 = new FileInputStream("C:/temp/foo.txt");
      // ...
      f1.read();
      // ...
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  public static void talk(Socket s) throws IOException {
    Collection messages = readMessages();
    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
    for (Iterator it = messages.iterator(); it.hasNext();) {
      Object message = it.next();
      out.print(message);
    }
    out.close();
  }

  public static void example() throws IOException {
    InetAddress addr = InetAddress.getByName("java.sun.com");

    Socket handShake = createSocket(); // should be shown correct by unique
                                        // solver
    handShake.connect(new InetSocketAddress(addr, 80));
    InputStream inp = handShake.getInputStream();

    Collection sockets = createSockets();
    for (Iterator it = sockets.iterator(); it.hasNext();) { // localMMN should
      // get iterator
      Socket s = (Socket) it.next();
      s.connect(new InetSocketAddress(addr, 80));
      talk(s);
    }

    talk(handShake);
  }

  public static void main(String[] args) {
    try {
      example();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  // ...

}
