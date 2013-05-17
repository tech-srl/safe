package com.ibm.safe.j2se.structural;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.lightweight.tests.LightweightRegressionUnit;

public final class RecurseTest extends SafeTCase {

  public void testRecurse1() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.Recurse1", 1);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  public void testRecurse2() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.Recurse2", 1);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  public void testRecurse3() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.Recurse3", 1);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  public void testRecurse4() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.Recurse4", 0);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  public void testRecurse5() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.Recurse5", 1);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  public void testRecurse9() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.Recurse9", 2);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

}
