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

public final class RunningExampleTest extends SafeTCase {

  public void testRunningExampleSeparating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.RunningExample", 4);
    test.selectTypestateRule("EmptyVector");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testRunningExampleUnique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.RunningExample", 2);
    test.selectTypestateRule("EmptyVector");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testRunningExampleAPMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.RunningExample", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testRunningExampleAPMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.RunningExample", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testRunningExampleAPMustK1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.RunningExample", 1);
    test.selectTypestateRule("EmptyVector");
    test.selectAPMustTypestateSolver();
    test.setAPMustKLimit(1);
    SafeRegressionDriver.run(test);
  }

  public void testRunningExampleIntraProcedural() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.RunningExample", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  public void testRunningExampleStaged() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.vector.RunningExample", 0);
    test.selectTypestateRule("EmptyVector");
    test.selectStagedTypestateSolver();
    SafeRegressionDriver.run(test);
  }

}
