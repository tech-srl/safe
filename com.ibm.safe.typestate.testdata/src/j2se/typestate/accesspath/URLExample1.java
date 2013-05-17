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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class URLExample1 {
  public static void main(String[] args) throws IOException {

    try {
      // Create a URLConnection object for a URL
      URL url = new URL("http://haven.watson.ibm.com:8080");
      URLConnection conn = url.openConnection();

      // List all the response headers from the server.
      // Note: The first call to getHeaderFieldKey() will implicit send
      // the HTTP request to the server.
      for (int i = 0;; i++) {
        String headerName = conn.getHeaderFieldKey(i);
        String headerValue = conn.getHeaderField(i);
        System.out.println(headerName);
        System.out.println(headerValue);
        if (headerName == null && headerValue == null) {
          // No more headers
          break;
        }
        if (headerName == null) {
          // The header value contains the server's HTTP version
        }
      }
    } catch (Exception e) {
    }
  }
}
