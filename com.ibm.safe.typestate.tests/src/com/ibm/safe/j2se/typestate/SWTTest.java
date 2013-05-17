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

/**
 * Unit tests for SWT-related properties.
 * 
 * @author yahave
 * 
 */
public final class SWTTest extends SafeTCase {

  public void addLibrariesToScope(TypestateRegressionUnit test) throws SafeException {
    test.addLibraryToScope("org.eclipse.swt_3.2.1.v3235e.jar");
    test.addLibraryToScope("org.eclipse.swt.win32.win32.x86_3.2.1.v3235.jar");
  }

  /**
   * Use ZERO_CFA in this testcase if you ever hope to swallow SWT in reasonable
   * time.
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSWTExample1Staged() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.swt.SWTExample1", 1);
    addLibrariesToScope(test);
    test.selectTypestateRule("XResMustDispose");
    test.selectStagedTypestateSolver();
    test.setOption(WholeProgramProperties.Props.CG_KIND.getName(), "ZERO_CFA");
    test.setOption(CommonProperties.Props.TIMEOUT_SECS.getName(), "3600");
    SafeRegressionDriver.run(test);
  }

  /**
   * Use ZERO_CFA in this testcase if you ever hope to swallow SWT in reasonable
   * time.
   * 
   * EY-Jun23-07: This is currently working incorrectly, as the local solver
   * cannot observe the program exit event, and therefore concludes that the
   * event never occurs (?)
   * 
   * TODO: change property to use object death events
   * 
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSWTExample1LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.swt.SWTExample1", 1);
    addLibrariesToScope(test);
    test.selectTypestateRule("XResMustDispose");
    test.selectLocalMMNSolver();
    test.setOption(WholeProgramProperties.Props.CG_KIND.getName(), "ZERO_CFA");
    test.setOption(CommonProperties.Props.TIMEOUT_SECS.getName(), "3600");
    SafeRegressionDriver.run(test);
  }

  /**
   * unique solver reports the error as expected.
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSWTExample1Unique() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.swt.SWTExample1", 1);
    addLibrariesToScope(test);
    test.selectTypestateRule("XResMustDispose");
    test.selectUniqueTypestateSolver();
    test.setOption(WholeProgramProperties.Props.CG_KIND.getName(), "ZERO_CFA");
    test.setOption(CommonProperties.Props.TIMEOUT_SECS.getName(), "3600");
    SafeRegressionDriver.run(test);
  }

  /**
   * Use ZERO_CFA in this testcase if you ever hope to swallow SWT in reasonable
   * time.
   * 
   * EY-Jun23-07: This is currently working incorrectly, as the local solver
   * cannot observe the program exit event, and therefore concludes that the
   * event never occurs (?)
   * 
   * TODO: change property to use object death events ??
   * 
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSWTObjDeathExample1LocalMMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.swt.SWTExample1", 1);
    addLibrariesToScope(test);
    test.selectTypestateRule("XODResMustDispose");
    test.selectLocalMMNSolver();
    test.setOption(WholeProgramProperties.Props.CG_KIND.getName(), "ZERO_CFA");
    test.setOption(CommonProperties.Props.TIMEOUT_SECS.getName(), "3600");
    SafeRegressionDriver.run(test);
  }

  /**
   * Use ZERO_CFA in this testcase if you ever hope to swallow SWT in reasonable
   * time.
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSWTExample2Staged() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.swt.SWTExample2", 0);
    addLibrariesToScope(test);
    test.selectTypestateRule("XResMustDispose");
    test.selectStagedTypestateSolver();
    test.setOption(WholeProgramProperties.Props.CG_KIND.getName(), "ZERO_CFA");
    test.setOption(CommonProperties.Props.TIMEOUT_SECS.getName(), "3600");
    SafeRegressionDriver.run(test);
  }

}
