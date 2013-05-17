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
import com.ibm.safe.options.WholeProgramProperties;
import com.ibm.safe.typestate.tests.TypestateRegressionUnit;

public final class IteratorTest extends SafeTCase {

  public void testIteratorExample1Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample1", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample1LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample1", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample2Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample2", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample2LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample2", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample3Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample3", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample3UniqueRTA() throws SafeException, Exception {
    // exclude this testcase from precommit until RTA crash is fixed
    if (SafeTCase.ONLY_PRECOMMIT) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample3", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.selectUniqueTypestateSolver();
    test.selectRTA();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample3LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample3", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample4Unique() throws SafeException, Exception {
    // for now, using cheap live analysis, which gives one false alarm.
    // expensive live analysis should be ok. need to optimize the expensive live
    // analysis.
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample4", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample4LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample4", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample5Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample5", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample5LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample5", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample6Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample6", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.setOption(WholeProgramProperties.Props.ENTRY_POINTS.getName(), "j2se.typestate.iterator.IteratorExample6.dummy([I)V");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample6LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample6", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.setOption(WholeProgramProperties.Props.ENTRY_POINTS.getName(), "j2se.typestate.iterator.IteratorExample6.dummy([I)V");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample9Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample9", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample9LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample9", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample9Staged() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample9", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectStagedTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * this produces a false alarm with unique engine since weak-updates have to
   * be made!
   */
  public void testIteratorExample9UniqueNoLive() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample9", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.selectUniqueTypestateSolver();
    test.disableLiveAnalysis();
    test.disableSupergraphSlicing();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample4APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample4", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * this produces NO false alarm with MMN engine the engine gets the required
   * strong update from unique logic
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample4APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample4", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample10Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample10", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample10LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample10", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample10APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample10", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample10APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample10", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample12Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample12", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample12LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample12", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample12Staged() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample12", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectStagedTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample13Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample13", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample13LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample13", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample14LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample14", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * @throws SafeException
   * @throws Exception
   */
  public void testIteratorExample14APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample14", 1);
    test.selectTypestateRule("IteratorHasNext");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample14APMustMustNot() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample14", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testIteratorExample16Unique() throws SafeException, Exception {
    // for now, using cheap live analysis, which gives one false alarm.
    // expensive live analysis should be ok. need to optimize the expensive live
    // analysis.
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample16", 0);
    test.selectTypestateRule("IteratorHasNext");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

}
