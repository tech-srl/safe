package com.ibm.safe.j2se.structural;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.lightweight.tests.LightweightRegressionUnit;

public final class NullDerefTest extends SafeTCase {

  public void testNullDeref1() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref1", 1);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref2() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref2", 0);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref3() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref3", 6);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  /**
   * expected results - Warning: Potential null dereference
   * (j2se.structural.NullDeref4.foo2(java.lang.Integer):15) - Warning:
   * Potential null dereference
   * (j2se.structural.NullDeref4.foo3(java.lang.Integer):22) - Warning:
   * Potential null dereference
   * (j2se.structural.NullDeref4.foo6(java.lang.Integer, boolean):44) - Warning:
   * Potential null dereference
   * (j2se.structural.NullDeref4.foo7(java.lang.Integer):49) - Warning:
   * Potential null dereference
   * (j2se.structural.NullDeref4.foo8(java.lang.Integer):57) - Warning:
   * Potential null dereference
   * (j2se.structural.NullDeref4.foo8(java.lang.Integer):58) - Warning:
   * Potential null dereference
   * (j2se.structural.NullDeref4.foo10(java.lang.Integer, boolean, boolean):79) -
   * Warning: Potential null dereference
   * (j2se.structural.NullDeref4.foo12(java.lang.Integer):96)
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testNullDeref4() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref4", 8);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref5() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref5", 0);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref6() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref6", 0);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref7() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref7", 1);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref8() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref8", 1);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref9() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref9", 0);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref10() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref10", 0);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref11() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref11", 2);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref12() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref12", 2);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref13() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref13", 1);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref14() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref14", 1);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref15() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref15", 0);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

  public void testNullDeref16() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.NullDeref16", 0);
    test.selectStructuralAnalysis();
    test.selectStructuralRule("NullDereferencement");
    SafeRegressionDriver.run(test);
  }

}
