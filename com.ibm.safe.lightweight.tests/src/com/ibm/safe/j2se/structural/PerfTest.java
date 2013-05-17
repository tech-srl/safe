package com.ibm.safe.j2se.structural;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.lightweight.tests.LightweightRegressionUnit;

public final class PerfTest extends SafeTCase {

  public void testPerfExamples() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.PerformanceRelatedExamples", 3);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

}
