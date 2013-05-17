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

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;

/**
 * Should produce a true warning.
 * 
 * @author egeay
 */
public final class AlgorithmParametersExample1 {

  public static void main(final String[] args) {
    try {
      final AlgorithmParameters algoParam = AlgorithmParameters.getInstance("JKS");
      // ... Some code
      byte[] parameters = algoParam.getEncoded(); // Hit !
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
