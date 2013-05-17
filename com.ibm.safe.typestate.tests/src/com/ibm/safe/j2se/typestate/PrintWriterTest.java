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

public final class PrintWriterTest extends SafeTCase {

  public void testPrintWriterExample1Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.printWriter.PrintWriterExample1", 2);
    test.selectTypestateRule("PrintWriter");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testPrintWriterExample1NoSlicing() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.printWriter.PrintWriterExample1", 2);
    test.selectTypestateRule("PrintWriter");
    test.selectBaseTypestateSolver();
    test.disableDFASlicing();
    test.disableSupergraphSlicing();
    SafeRegressionDriver.run(test);
  }

  public void testPrintWriterExample2Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.printWriter.PrintWriterExample2", 0);
    test.selectTypestateRule("PrintWriter");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testPrintWriterExample2APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.printWriter.PrintWriterExample2", 0);
    test.selectTypestateRule("PrintWriter");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

}
