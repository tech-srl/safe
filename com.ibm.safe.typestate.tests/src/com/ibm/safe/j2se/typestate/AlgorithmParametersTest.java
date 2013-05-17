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

public final class AlgorithmParametersTest extends SafeTCase {

  public void testAlgorithmParametersExample1Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.AlgorithmParametersExample1", 1);
    test.selectTypestateRule("AlgorithmParameters");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testAlgorithmParametersExample1APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.AlgorithmParametersExample1", 1);
    test.selectTypestateRule("AlgorithmParameters");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testAlgorithmParametersExample2Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.AlgorithmParametersExample2", 0);
    test.selectTypestateRule("AlgorithmParameters");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testAlgorithmParametersExample2APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.security.AlgorithmParametersExample2", 0);
    test.selectTypestateRule("AlgorithmParameters");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

}
