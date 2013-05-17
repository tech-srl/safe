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

public final class VectorTest extends SafeTCase {

  public void testVectorExample1Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample1", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample2Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample2", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample3Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample3", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample12Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample12", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample12BaseNoSlicing() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample12", 1);
    test.selectTypestateRule("EmptyVector");
    test.disableSupergraphSlicing();
    test.disableDFASlicing();
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample11Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample11", 3);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample11BaseNoSlicing() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample11", 3);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    test.disableDFASlicing();
    test.disableSupergraphSlicing();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample13Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample13", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample13BaseUnsound() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample13", 1);
    test.selectTypestateRule("UnsoundEmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample14Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample14", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample1SU() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample1", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectStrongUpdateTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample2SU() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample2", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectStrongUpdateTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample3SU() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample3", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectStrongUpdateTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample12SU() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample12", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectStrongUpdateTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample11SU() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample11", 3);
    test.selectTypestateRule("EmptyVector");
    test.selectStrongUpdateTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample13SU() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample13", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectStrongUpdateTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample13SUUnsound() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample13", 0);
    test.selectTypestateRule("UnsoundEmptyVector");
    test.selectStrongUpdateTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample1APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample1", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample1APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample1", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample1APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample1", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample2APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample2", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample2APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample2", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample2APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample2", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample3APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample3", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample3APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample3", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample3APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample3", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample4APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample4", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample4APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample4", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample4APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample4", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample5APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample5", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample5APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample5", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample5APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample5", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample6APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample6", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample6APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample6", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample6APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample6", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample7APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample7", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample7APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample7", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample7APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample7", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample8APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample8", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample8APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample8", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample8APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample8", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample9APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample9", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample9APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample9", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample9APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample9", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample10APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample10", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample10APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample10", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample10APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample10", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample15APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample15", 2);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample15APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample15", 2);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample16Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample16", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample16APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample16", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample16APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample16", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample17Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample17", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample17APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample17", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample17APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample17", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample17APMustK2() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample17", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(2);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample18Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample18", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample18Unique() throws SafeException, Exception {
    // base engine is defeated by lack of strong update
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample18", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample18APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample18", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample18APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample18", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample19APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample19", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample19APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample19", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample20APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample20", 2);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample20APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample20", 2);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample21Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample21", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample21APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample21", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample21APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample21", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample22Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample22", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample22APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample22", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample22APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample22", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample23APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample23", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample23APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample23", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample24APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample24", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample24APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample24", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample25APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample25", 2);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample26Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample26", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample27APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample27", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample27APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample27", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample28APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample28", 1);
    test.selectTypestateRule("UnsoundEmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample29Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample29", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample29APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample29", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample29APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample29", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testVectorExample30APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.VectorExample30", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

}
