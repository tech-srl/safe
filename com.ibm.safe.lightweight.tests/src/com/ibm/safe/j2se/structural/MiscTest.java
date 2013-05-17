package com.ibm.safe.j2se.structural;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.lightweight.tests.LightweightRegressionUnit;

public final class MiscTest extends SafeTCase {

  public void testObjectFinalize() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.ObjectFinalizeExample", 2);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  public void testObjectNotify() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.ObjectNotifyExample", 1);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  public void testSuspiciousOverrieds() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.SuspiciousOverriddingExamples", 4);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  public void testSystemGC() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.SystemGcExample", 1);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  public void testThreadCalls() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.ThreadCalls", 2);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }
}
