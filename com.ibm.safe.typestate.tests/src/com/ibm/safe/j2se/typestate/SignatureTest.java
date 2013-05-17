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
package com.ibm.safe.j2se.typestate;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.typestate.tests.TypestateRegressionUnit;

public final class SignatureTest extends SafeTCase {

  public void testSignatureExample1Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample1", 1);
    test.selectTypestateRule("Signature");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSignatureExample1APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample1", 1);
    test.selectTypestateRule("Signature");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSignatureExample2Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample2", 0);
    test.selectTypestateRule("Signature");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSignatureExample2APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample2", 0);
    test.selectTypestateRule("Signature");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSignatureExample3Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample3", 2);
    test.selectTypestateRule("Signature");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSignatureExample3APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample3", 1);
    test.selectTypestateRule("Signature");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSignatureExample4Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample4", 0);
    test.selectTypestateRule("Signature");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSignatureExample4APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample4", 0);
    test.selectTypestateRule("Signature");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSignatureExample5Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample5", 1);
    test.selectTypestateRule("Signature");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSignatureExample5APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample5", 1);
    test.selectTypestateRule("Signature");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSignatureExample6Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample6", 2);
    test.selectTypestateRule("Signature");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSignatureExample6APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.SignatureExample6", 2);
    test.selectTypestateRule("Signature");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

}
