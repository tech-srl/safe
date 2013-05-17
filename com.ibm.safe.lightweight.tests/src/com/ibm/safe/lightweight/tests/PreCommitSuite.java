package com.ibm.safe.lightweight.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.j2se.structural.AccessibilityRulesTest;
import com.ibm.safe.j2se.structural.CloneableTest;
import com.ibm.safe.j2se.structural.RecurseTest;
import com.ibm.safe.j2se.structural.SCCPTest;

public final class PreCommitSuite {

  public static Test suite() {
    final TestSuite suite = new TestSuite();

    SafeTCase.ONLY_PRECOMMIT = true;

    suite.addTestSuite(CloneableTest.class);
    suite.addTestSuite(AccessibilityRulesTest.class);
    suite.addTestSuite(SCCPTest.class);
    suite.addTestSuite(RecurseTest.class);

    return suite;
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}
