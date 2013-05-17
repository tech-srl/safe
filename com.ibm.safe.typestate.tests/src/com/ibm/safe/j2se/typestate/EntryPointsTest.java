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
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.typestate.tests.TypestateRegressionUnit;

public final class EntryPointsTest extends SafeTCase {

  public void testEntrypoints1() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.iterator.IteratorExample6", //$NON-NLS-1$ 
        1);
    test.selectBaseTypestateSolver();
    test.selectTypestateRule("IteratorHasNext");
    test.setOption(WholeProgramProperties.Props.ENTRY_POINTS.toString(), "j2se.typestate.iterator.IteratorExample6.dummy([I)V"); //$NON-NLS-1$
    SafeRegressionDriver.run(test);
  }

  public void testEntrypoints4() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit(null, 1);
    test.setOption(CommonProperties.Props.MODULES.toString(), "IteratorExample6.jar"); //$NON-NLS-1$
    test.selectBaseTypestateSolver();
    test.selectTypestateRule("IteratorHasNext");
    test.setOption(WholeProgramProperties.Props.ENTRY_POINTS_FILE.toString(), ENTRY_POINTS_XML_FILE_2);
    SafeRegressionDriver.run(test);
  }

  private static final String ENTRY_POINTS_XML_FILE_2 = EntryPointsTest.class.getClassLoader()
      .getResource("entry_points_2.xml").getFile(); //$NON-NLS-1$

}
