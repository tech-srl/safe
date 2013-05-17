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
package com.ibm.safe.core.tests;

import com.ibm.safe.controller.GenericSafeController;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.exceptions.SetUpException;

public final class BadSetupTest extends SafeTCase {

  public void testNoEngine() throws Exception {
    try {
      SafeRegressionUnit test = new SafeRegressionUnit("j2se.typestate.iterator.IteratorExample6", 1);
      SafeRegressionDriver.run(test);
    } catch (SafeException e) {
      assertTrue(e instanceof SetUpException);
      assertTrue(e.getMessage().equals(GenericSafeController.NO_SOLVER_OPTIONS));
      return;
    }
    fail("should have thrown a SafeException");
  }

}
