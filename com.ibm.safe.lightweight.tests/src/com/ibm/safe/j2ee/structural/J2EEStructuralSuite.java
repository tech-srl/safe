package com.ibm.safe.j2ee.structural;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Eran Yahav (yahave)
 * 
 */

public class J2EEStructuralSuite {

  public static Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTestSuite(PetStoreTest.class);

    return suite;
  }

  /**
   * Runs the test suite using the textual runner.
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
}