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
 * Name: URLConnectionExample1.java
 * Description: An incorrect usage of URLConnection.
 * Expected Result: this case should report a true alarm.
 * Example from: http://javaalmanac.com/egs/java.net/GetHeaders.html
 *********************************************************************/

package j2se.typestate.URLConnection;

import java.net.URL;
import java.net.URLConnection;

public class URLConnectionExample2 {

  public static void main(String[] args) {
    try {
      // Create a URLConnection object for a URL
      URL url = new URL("http://hostname:80");
      URLConnection conn = url.openConnection();

      conn.connect();

      conn.addRequestProperty("key", "value"); // illegal when connected #1
      conn.setDoInput(true); // illegal when connected #2
      conn.setDoOutput(true); // illegal when connected #3
      conn.setAllowUserInteraction(true); // illegal when connected #4
      conn.setIfModifiedSince(1000); // illegal when connected #5
      conn.setAllowUserInteraction(true); // illegal when connected #6
      conn.setUseCaches(true); // illegal when connected #7

    } catch (Exception e) {
    }
  }
}
