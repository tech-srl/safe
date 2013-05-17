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
package j2se.typestate.accesspath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * example taken from:
 * http://www.javaworld.com/javaworld/jw-12-1996/jw-12-sockets.html with minor
 * modifications
 */
public class EchoServer {
  public static void main(String args[]) {

    // declaration section:
    // declare a server socket and a client socket for the server
    // declare an input and an output stream

    ServerSocket echoServer = null;
    String line;
    BufferedReader d;
    PrintStream os;
    Socket clientSocket = null;

    // Try to open a server socket on port 9999
    // Note that we can't choose a port less than 1023 if we are not
    // privileged users (root)

    try {
      echoServer = new ServerSocket(9999);
    } catch (IOException e) {
      System.out.println(e);
    }

    // Create a socket object from the ServerSocket to listen and accept
    // connections.
    // Open input and output streams

    try {
      clientSocket = echoServer.accept();
      d = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      os = new PrintStream(clientSocket.getOutputStream());

      // As long as we receive data, echo that data back to the client.

      while (true) {
        line = d.readLine();
        os.println(line);
      }
    } catch (IOException e) {
      System.out.println(e);
    }
  }
}
