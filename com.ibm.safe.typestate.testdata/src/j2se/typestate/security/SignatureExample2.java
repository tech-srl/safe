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

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.spec.DSAParameterSpec;

/**
 * Should not produce a warning.
 * 
 * @author egeay
 */
public class SignatureExample2 {

  public static void main(final String[] args) {
    try {
      KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA");
      DSAParameterSpec dsaSpec = new DSAParameterSpec(new BigInteger("3423434"), new BigInteger("3453465345"), new BigInteger(
          "23453425"));
      SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
      random.setSeed(1000);
      keyGen.initialize(dsaSpec, random);

      final Signature dsa = Signature.getInstance("SHA1withDSA");
      final KeyPair pair = keyGen.generateKeyPair();
      dsa.initSign(pair.getPrivate());

      dsa.update(getData());
      byte[] sig = dsa.sign();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static byte[] getData() {
    return new byte[] { 124, 123, 53, 66, 45 };
  }

}
