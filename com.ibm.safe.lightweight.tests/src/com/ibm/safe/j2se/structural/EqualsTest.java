package com.ibm.safe.j2se.structural;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.lightweight.tests.LightweightRegressionUnit;

public final class EqualsTest extends SafeTCase {

  public void testEqualsHashCode1() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.EqualsHashCodeExample1", 1);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  public void testEqualsHashCode2() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.EqualsHashCodeExample2", 1);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  public void testEqualsStringComparison() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.EqualsStringComparison", 1);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

}
