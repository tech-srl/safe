package com.ibm.safe.j2se.structural;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Eran Yahav (yahave)
 * 
 */

public class J2SEStructuralSuite {

  public static Test suite() {

    TestSuite suite = new TestSuite();

    suite.addTestSuite(EqualsTest.class);
    suite.addTestSuite(MiscTest.class);
    suite.addTestSuite(NullDerefTest.class);
    suite.addTestSuite(PerfTest.class);
    suite.addTestSuite(RecurseTest.class);
    suite.addTestSuite(SCCPTest.class);
    suite.addTestSuite(AccessibilityRulesTest.class);
    suite.addTestSuite(CloneableTest.class);

    return suite;
  }

  /**
   * Runs the test suite using the textual runner.
   */
  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }
}