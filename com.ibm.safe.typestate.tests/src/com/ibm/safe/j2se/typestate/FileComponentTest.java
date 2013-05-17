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

public final class FileComponentTest extends SafeTCase {

  public void testFocusExample1APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FocusExample1", 0);
    test.selectTypestateRule("FileOpenAndReadFromOpFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testPiNodes1APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.PiNodes1", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testPiNodes1APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.PiNodes1", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testPiNodes2APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.PiNodes2", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testPiNodes2APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.PiNodes2", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testFCExample1APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample1", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample1APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample1", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testFCExample2APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample2", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample2APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample2", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testFCExample2APMustNoSlicing() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample2", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.disableSupergraphSlicing();
    test.disableDFASlicing();
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample3APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample3", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample3APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample3", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testFCExample3APMustNoSlicing() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample3", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.disableSupergraphSlicing();
    test.disableDFASlicing();
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample4APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample4", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample4APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample4", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testFCExample4APMustNoSlicing() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample4", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.disableDFASlicing();
    test.disableSupergraphSlicing();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample5APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample5", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample5APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample5", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testFCExample5APMustNoSlicing() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample5", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.disableDFASlicing();
    test.disableSupergraphSlicing();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample6Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample6", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample6APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample6", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample6APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample6", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testFCExample7APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample7", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample7APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample7", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testFCExample8APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample8", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample8APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample8", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testFCExample9APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample9", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample9APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample9", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testFCExample10APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample10", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample10APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample10", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testFCExample11APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample11", 3);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExample11APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExample11", 3);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testIPExample1APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.IPExample1", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIPExample1APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.IPExample1", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testIPExample2APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.IPExample2", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIPExample2APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.IPExample2", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testIPExample3APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.IPExample3", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIPExample3APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.IPExample3", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testIPExample4APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.IPExample4", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIPExample4APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.IPExample4", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testIPExample5APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.IPExample5", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIPExample5APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.IPExample5", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testSimpleExample1APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.SimpleExample1", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSimpleExample1APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.SimpleExample1", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testSimpleExample2APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.SimpleExample2", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSimpleExample2APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.SimpleExample2", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testSimpleExample3APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.SimpleExample3", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSimpleExample3APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.SimpleExample3", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void TestSimpleExample4APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.SimpleExample4", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSingleStepsAPMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.SingleSteps", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSingleStepsAPMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.SingleSteps", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testStaticFiesta1Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.StaticFiesta1", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testStaticFiesta1APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.StaticFiesta1", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testStaticFiesta1APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.StaticFiesta1", 1);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testStaticFiesta2APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.StaticFiesta2", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testStaticFiesta2APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.StaticFiesta2", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testMayPhiExample1Base() throws SafeException, Exception {
    // the base solver is defeated by a weak update, and reports a false
    // positive
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.MayPhiExample1", 1);
    test.selectTypestateRule("FileOpenAndReadFromOpFileComponent");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testMayPhiExample1APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.MayPhiExample1", 0);
    test.selectTypestateRule("FileOpenAndReadFromOpFileComponent");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testMayPhiExample1APMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.MayPhiExample1", 0);
    test.selectTypestateRule("FileOpenAndReadFromOpFileComponent");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testFCExampleStaged1Staged() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExampleStaged1", 0);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectStagedTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFCExampleStaged1LocalMNN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.FCExampleStaged1", 2);
    test.selectTypestateRule("FileReadAndCloseFromFileComponent");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

}
