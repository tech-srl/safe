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

public final class SocketExample extends SafeTCase {

  public void testSocketExampleLocalMMN() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.socket.Sender", 2);
    // test.selectTypestateRule("Socket");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * expected results: Due to weak updates, any use of these methods should
   * raise a false alarm under separating engine - Warning: Never
   * getOutputStream/getInputStream from a Socket when not connected or closed
   * (j2se.typestate.socket.Sender.example():76) - Warning: Never
   * getOutputStream/getInputStream from a Socket when not connected or closed
   * (j2se.typestate.socket.Sender.talk(java.net.Socket):63)
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSocketExampleSeparating() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.socket.Sender", 2);
    test.selectTypestateRule("Socket");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSocketExampleUnique() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.socket.Sender", 1);
    test.selectTypestateRule("Socket");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * expected result: - Warning: Never getOutputStream/getInputStream from a
   * Socket when not connected or closed
   * (j2se.typestate.socket.Sender.talk(java.net.Socket):63) the APMust engine
   * is unable to perform strong update on socket taken out of collection.
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSocketExampleAPMust() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.socket.Sender", 1);
    test.selectTypestateRule("Socket");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * expected result: no alarms reported. APMMN should eliminate the false alarm
   * reported by APMust.
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSocketExampleAPMustMustNot() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.socket.Sender", 0);
    test.selectTypestateRule("Socket");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSocketExampleStaged() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.socket.Sender", 0);
    test.selectTypestateRule("Socket");
    test.selectStagedTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSocketExample2Staged() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.socket.Sender2", 0);
    test.selectTypestateRule("Socket");
    test.selectStagedTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSocketExample2APMustMustNot() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.socket.Sender2", 0);
    test.selectTypestateRule("Socket");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testSocketExample2LocalMMN() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.socket.Sender2", 0);
    test.selectTypestateRule("Socket");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

}
