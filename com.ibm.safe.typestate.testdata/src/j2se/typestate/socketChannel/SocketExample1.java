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
 * Name: SocketExample1.java
 * Description: correct usage of socket channels
 * Expected Result: this case should return a true alarm, as when "finishConnect()" returns
 * exceptionally, it closes the channel.  
 * Author: Eran Yahav (eyahav)
 * Example taken from: http://javaalmanac.com/egs/java.nio/ReadSocket.html
 *********************************************************************/

package j2se.typestate.socketChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class SocketExample1 {

  // Creates a non-blocking socket channel for the specified host name and port.
  // connect() is called on the new channel before it is returned.
  public static SocketChannel createSocketChannel(String hostName, int port) throws IOException {
    // Create a non-blocking socket channel
    SocketChannel sChannel = SocketChannel.open();
    sChannel.configureBlocking(false);

    // Send a connection request to the server; this method is non-blocking
    sChannel.connect(new InetSocketAddress(hostName, port));
    return sChannel;
  }

  public static void main(String[] args) {

    SocketChannel socketChannel = null;

    // Create a non-blocking socket and check for connections
    try {
      // Create a non-blocking socket channel on port 80
      socketChannel = createSocketChannel("hostname.com", 80);

      // Before the socket is usable, the connection must be completed
      // by calling finishConnect(), which is non-blocking
      while (!socketChannel.finishConnect()) {
        // Do something else
      }
      // Socket channel is now ready to use
    } catch (IOException e) {
    }

    // Create a direct buffer to get bytes from socket.
    // Direct buffers should be long-lived and be reused as much as
    // possible.
    ByteBuffer buf = ByteBuffer.allocateDirect(1024);

    try {
      // Clear the buffer and read bytes from socket
      buf.clear();
      int numBytesRead = socketChannel.read(buf);

      if (numBytesRead == -1) {
        // No more bytes can be read from the channel
        socketChannel.close();
      } else {
        // To read the bytes, flip the buffer
        buf.flip();

        // Read the bytes from the buffer ...;
        // see e159 Getting Bytes from a ByteBuffer
      }
    } catch (IOException e) {
      // Connection may have been closed
    }
  }
}
