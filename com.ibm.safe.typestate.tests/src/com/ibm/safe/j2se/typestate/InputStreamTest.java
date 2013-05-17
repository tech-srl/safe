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

public final class InputStreamTest extends SafeTCase {

  public void testFileInputStreamExample1Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.input_stream.FileInputStreamExample1", 1);
    test.selectTypestateRule("InputStreamCloseThenRead");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * - Warning: Always connect a PipedInputStream when using default constructor
   * (j2se.typestate.input_stream.PipedInputStreamExample1.main(java.lang.String[]):32) -
   * Warning: Always connect a PipedInputStream when using default constructor
   * (j2se.typestate.input_stream.PipedInputStreamExample1.main(java.lang.String[]):24) -
   * Warning: Always connect a PipedInputStream when using default constructor
   * (j2se.typestate.input_stream.PipedInputStreamExample1.main(java.lang.String[]):32) -
   * Warning: Always connect a PipedInputStream when using default constructor
   * (j2se.typestate.input_stream.PipedInputStreamExample1.main(java.lang.String[]):32)
   * 
   * @throws SafeException
   * @throws Exception
   */

  public void testPipedInputStreamExample1Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.input_stream.PipedInputStreamExample1", 4);
    test.selectTypestateRule("PipedInputStream");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * - Warning: Always connect a PipedInputStream when using default constructor
   * (j2se.typestate.input_stream.PipedInputStreamExample1.main(java.lang.String[]):32) -
   * Warning: Always connect a PipedInputStream when using default constructor
   * (j2se.typestate.input_stream.PipedInputStreamExample1.main(java.lang.String[]):24) -
   * Warning: Always connect a PipedInputStream when using default constructor
   * (j2se.typestate.input_stream.PipedInputStreamExample1.main(java.lang.String[]):32) -
   * Warning: Always connect a PipedInputStream when using default constructor
   * (j2se.typestate.input_stream.PipedInputStreamExample1.main(java.lang.String[]):32)
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testPipedInputStreamExample1APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.input_stream.PipedInputStreamExample1", 4);
    test.selectTypestateRule("PipedInputStream");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * expected results - Warning: Always connect a PipedInputStream when using
   * default constructor
   * (j2se.typestate.input_stream.PipedInputStreamExample2.main(java.lang.String[]):33) -
   * Warning: Always connect a PipedInputStream when using default constructor
   * (j2se.typestate.input_stream.PipedInputStreamExample2.main(java.lang.String[]):33) -
   * Warning: Always connect a PipedInputStream when using default constructor
   * (j2se.typestate.input_stream.PipedInputStreamExample2.main(java.lang.String[]):33) -
   * Warning: Always connect a PipedInputStream when using default constructor
   * (j2se.typestate.input_stream.PipedInputStreamExample2.main(java.lang.String[]):25)
   * 
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testPipedInputStreamExample2Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.input_stream.PipedInputStreamExample2", 4);
    test.selectTypestateRule("PipedInputStream");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testPipedInputStreamExample2APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.input_stream.PipedInputStreamExample2", 0);
    test.selectTypestateRule("PipedInputStream");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFileInputStreamExample1BaseTracing() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.input_stream.FileInputStreamExample1", 1);
    test.disableDFASlicing();
    test.selectTypestateRule("InputStreamCloseThenRead");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

}
