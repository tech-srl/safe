package com.ibm.safe.j2se.structural;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.lightweight.tests.LightweightRegressionUnit;

public final class AccessibilityRulesTest extends SafeTCase {

  public void testCaseAccessibility1() throws SafeException, Exception {
    final LightweightRegressionUnit test = new LightweightRegressionUnit("AccessibilityExamples", 37);
    test.selectStructuralAccessibilityAnalysis();
    SafeRegressionDriver.run(test);
  }

}
