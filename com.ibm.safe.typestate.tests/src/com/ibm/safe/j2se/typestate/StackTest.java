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

public final class StackTest extends SafeTCase {

  public void testStackExample1Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.stack.StackExample1", 1);
    test.selectTypestateRule("EmptyStack");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testStackExample2Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.stack.StackExample2", 1);
    test.selectTypestateRule("EmptyStack");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testStackExample3Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.stack.StackExample3", 1);
    test.selectTypestateRule("EmptyStack");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

}
