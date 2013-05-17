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

public final class OutputStreamTest extends SafeTCase {

  public void testFileOutputStreamExample1Base() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.output_stream.FileOutputStreamExample1", 1);
    test.selectTypestateRule("OutputStreamCloseThenWrite");
    test.selectBaseTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   *  - Warning: Always connect a PipedOutputStream when using default
   * constructor
   * (j2se.typestate.output_stream.PipedOutputStreamExample1.main(java.lang.String[]):24) -
   * Warning: Always connect a PipedOutputStream when using default constructor
   * (j2se.typestate.output_stream.PipedOutputStreamExample1.main(java.lang.String[]):30) -
   * Warning: Always connect a PipedOutputStream when using default constructor
   * (j2se.typestate.output_stream.PipedOutputStreamExample1.main(java.lang.String[]):30) -
   * Warning: Always connect a PipedOutputStream when using default constructor
   * (j2se.typestate.output_stream.PipedOutputStreamExample1.main(java.lang.String[]):30)
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testPipedOutputStreamExample1Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.output_stream.PipedOutputStreamExample1", 4);
    test.selectTypestateRule("PipedOutputStream");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * - Warning: Always connect a PipedOutputStream when using default
   * constructor
   * (j2se.typestate.output_stream.PipedOutputStreamExample1.main(java.lang.String[]):24) -
   * Warning: Always connect a PipedOutputStream when using default constructor
   * (j2se.typestate.output_stream.PipedOutputStreamExample1.main(java.lang.String[]):30) -
   * Warning: Always connect a PipedOutputStream when using default constructor
   * (j2se.typestate.output_stream.PipedOutputStreamExample1.main(java.lang.String[]):30) -
   * Warning: Always connect a PipedOutputStream when using default constructor
   * (j2se.typestate.output_stream.PipedOutputStreamExample1.main(java.lang.String[]):30)
   * 
   * @throws SafeException
   * @throws Exception
   */

  public void testPipedOutputStreamExample1APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.output_stream.PipedOutputStreamExample1", 4);
    test.selectTypestateRule("PipedOutputStream");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  /**
   * - Warning: Always connect a PipedOutputStream when using default
   * constructor
   * (j2se.typestate.output_stream.PipedOutputStreamExample2.main(java.lang.String[]):31) -
   * Warning: Always connect a PipedOutputStream when using default constructor
   * (j2se.typestate.output_stream.PipedOutputStreamExample2.main(java.lang.String[]):25) -
   * Warning: Always connect a PipedOutputStream when using default constructor
   * (j2se.typestate.output_stream.PipedOutputStreamExample2.main(java.lang.String[]):31) -
   * Warning: Always connect a PipedOutputStream when using default constructor
   * (j2se.typestate.output_stream.PipedOutputStreamExample2.main(java.lang.String[]):31)
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testPipedOutputStreamExample2Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.output_stream.PipedOutputStreamExample2", 4);
    test.selectTypestateRule("PipedOutputStream");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testPipedOutputStreamExample2APMust() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.output_stream.PipedOutputStreamExample2", 0);
    test.selectTypestateRule("PipedOutputStream");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFileOutputStreamExample2Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.output_stream.FileOutputStreamExample2", 2);
    test.selectTypestateRule("OutputStreamCloseThenWrite");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFileOutputStreamExample2MMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.output_stream.FileOutputStreamExample2", 2);
    test.selectTypestateRule("OutputStreamCloseThenWrite");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFileOutputStreamExample2Must() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.output_stream.FileOutputStreamExample2", 2);
    test.selectTypestateRule("OutputStreamCloseThenWrite");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFileOutputStreamExample3MMN() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.output_stream.FileOutputStreamExample3", 1);
    test.selectTypestateRule("OutputStreamCloseThenWrite");
    test.selectAPMustMustNotTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFileOutputStreamExample3Must() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.output_stream.FileOutputStreamExample3", 1);
    test.selectTypestateRule("OutputStreamCloseThenWrite");
    test.selectAPMustTypestateSolver();
    SafeRegressionDriver.run(test);
  }

  public void testFileOutputStreamExample3Separating() throws SafeException, Exception {
    TypestateRegressionUnit test = new TypestateRegressionUnit("j2se.typestate.output_stream.FileOutputStreamExample3", 1);
    test.selectTypestateRule("OutputStreamCloseThenWrite");
    test.selectSeparatingTypestateSolver();
    SafeRegressionDriver.run(test);
  }

}
