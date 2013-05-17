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

public final class PrintStreamTest extends SafeTCase {

  public void testPrintStreamExample1Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.printStream.PrintStreamExample1", 2);
    test.selectTypestateRule("PrintStream");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testPrintStreamExample1NoSlicing() throws SafeException, Exception {
    if (SafeTCase.ONLY_PRECOMMIT) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.printStream.PrintStreamExample1", 2);
    test.selectTypestateRule("PrintStream");
    test.selectBaseTypestateSolver();
    test.disableDFASlicing();
    test.disableSupergraphSlicing();
    SafeRegressionDriver.run(test);
  }

  // TODO: disable for now, revisit later. [EY]
  // public void testPrintStreamExample1MustCloseBase() throws SafeException,
  // Exception {
  // if (SafeTCase.ONLY_PRECOMMIT) {
  // return;
  // }
  // TypestateRegressionUnit test = new
  // TypestateRegressionUnit("j2se.typestate.printStream.PrintStreamExample1",
  // 1);
  // test.selectTypestateRule("MustClosePrintStream");
  // test.selectBaseTypestateSolver();
  // SafeRegressionDriver.run(test);
  // }

  /**
   * Will report an error due to exceptional exit path on which the PrintStream
   * is not closed
   * 
   * @throws SafeException
   * @throws Exception
   */
  // TODO: disable for now, revisit later. [EY]
  // public void testPrintStreamExample1MustCloseUnique() throws SafeException,
  // Exception {
  // if (SafeTCase.ONLY_PRECOMMIT) {
  // return;
  // }
  // TypestateRegressionUnit test = new
  // TypestateRegressionUnit("j2se.typestate.printStream.PrintStreamExample1",
  // 1);
  // test.selectTypestateRule("MustClosePrintStream");
  // test.selectUniqueTypestateSolver();
  // SafeRegressionDriver.run(test);
  // }
  // TODO: disable for now, revisit later. [EY]
  // public void testPrintStreamExample1MustCloseUnique() throws SafeException,
  // Exception {
  // if (SafeTCase.ONLY_PRECOMMIT) {
  // return;
  // }
  // TypestateRegressionUnit test = new
  // TypestateRegressionUnit("j2se.typestate.printStream.PrintStreamExample1",
  // 0);
  // test.selectTypestateRule("MustClosePrintStream");
  // test.selectUniqueTypestateSolver();
  // SafeRegressionDriver.run(test);
  // }
  // TODO: disable for now, revisit later. [EY]
  // public void testPrintStreamExample2MustCloseUnique() throws SafeException,
  // Exception {
  // if (SafeTCase.ONLY_PRECOMMIT) {
  // return;
  // }
  // TypestateRegressionUnit test = new
  // TypestateRegressionUnit("j2se.typestate.printStream.PrintStreamExample2",
  // 1);
  // test.selectTypestateRule("MustClosePrintStream");
  // test.selectUniqueTypestateSolver();
  // SafeRegressionDriver.run(test);
  // }
}
