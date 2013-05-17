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

public final class StagingExample extends SafeTCase {

  public void testStagingExample1Staged() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.StagingExample1", 0);
    test.selectTypestateRule("FileOpenAndReadFromOpFileComponent");
    test.selectStagedTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testStagingExample1Unique() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.StagingExample1", 1);
    test.selectTypestateRule("FileOpenAndReadFromOpFileComponent");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testStagingExample1LocalMMN() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.StagingExample1", 1);
    test.selectTypestateRule("FileOpenAndReadFromOpFileComponent");
    test.selectLocalMMNSolver();
    SafeRegressionDriver.run(test);
  }

  public void testStagingExample2Unique() throws SafeException, Exception {
    if (isPreCommit()) {
      return;
    }
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.fileComponent.StagingExample2", 0);
    test.selectTypestateRule("FileOpenAndReadFromOpFileComponent");
    test.selectUniqueTypestateSolver();
    SafeRegressionDriver.run(test);
  }

}
