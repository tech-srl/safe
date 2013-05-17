package com.ibm.safe.j2se.structural;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.lightweight.tests.LightweightRegressionUnit;

public final class CloneableTest extends SafeTCase {

  public void testCloneable1() throws Exception, SafeException {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.CloneableExample1", 1);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  public void testCloneable2() throws Exception, SafeException {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.CloneableExample2", 0);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

}
