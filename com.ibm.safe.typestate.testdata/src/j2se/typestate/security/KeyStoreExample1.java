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
package j2se.typestate.security;

import java.security.KeyStore;
import java.security.KeyStoreException;

/**
 * Should produce a true warning.
 * 
 * @author egeay
 */
public final class KeyStoreExample1 {

  public static void main(final String[] args) {
    try {
      final KeyStore keyStore = KeyStore.getInstance("JKS");
      // ... Some code
      int size = keyStore.size(); // Hit !
    } catch (KeyStoreException e) {
      e.printStackTrace();
    }
  }

  private static char[] getPassword() {
    return new char[] { 'm', 'y', 'p', 'a', 's', 's' };
  }

}
