package com.ibm.safe.j2se.structural;

import java.util.Collection;

import com.ibm.safe.core.tests.SafeMessageCheck;
import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.lightweight.tests.LightweightRegressionUnit;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.reporting.message.MethodLocation;
import com.ibm.safe.reporting.message.SignatureUtils;

/**
 * @author yahave
 */
public final class SCCPTest extends SafeTCase {

  /**
   * expected result - Warning: Suspicious condition over a constant value
   * (j2se.structural.SCCPExample1.main(java.lang.String[]):19)
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSCCP1() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.SCCPExample1", 1);
    test.selectStructuralAnalysis();

    SafeRegressionDriver.run(test);
  }

  /**
   * expected result - Warning: Potential null dereference
   * (j2se.structural.SCCPExample2.main(java.lang.String[]):19) - Warning:
   * Suspicious condition over a constant value
   * (j2se.structural.SCCPExample2.main(java.lang.String[]):18)
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSCCP2() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.SCCPExample2", 2);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  /**
   * expected result
   * 
   * <ul>
   * <li> Warning: Suspicious condition over a constant value
   * (j2se.structural.SCCPExample3:28)
   * <li> Warning: Suspicious condition over a constant value
   * (j2se.structural.SCCPExample3:34)
   * <li> Warning: Suspicious condition over a constant value
   * (j2se.structural.SCCPExample3:24)
   * <li> Warning: Suspicious condition over a constant value
   * (j2se.structural.SCCPExample3:20)
   * </ul>
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSCCP3() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.SCCPExample3", 4);
    test.selectStructuralRule("ConstantCondition");
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  /**
   * expected result - Warning: Potential null dereference
   * (j2se.structural.SCCPExample4.SCCPExample4(int, boolean,
   * java.lang.String):20) - Warning: Suspicious condition over a constant value
   * (j2se.structural.SCCPExample4.SCCPExample4(int, boolean,
   * java.lang.String):19)
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSCCP4() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.SCCPExample4", 2);
    test.selectStructuralAnalysis();
    SafeMessageCheck check = new SafeMessageCheck() {
      public void check(Collection<? extends Message> messages) {
        if (messages.size() > 0) {
          final Message message = (Message) messages.iterator().next();
          final MethodLocation methodLoc = (MethodLocation) message.getLocation();
          final String signature = SignatureUtils.getMethodSignature(methodLoc, true);
          assertTrue(!signature.startsWith("<init>")); //$NON-NLS-1$
        }
      }
    };
    SafeRegressionDriver.run(test, check);
  }

  /**
   * expected result - Warning: Suspicious condition over a constant value
   * (j2se.structural.SCCPExample5.main(java.lang.String[]):12) - Warning:
   * Suspicious condition over a constant value
   * (j2se.structural.SCCPExample5.main(java.lang.String[]):9) - Warning:
   * Suspicious condition over a constant value
   * (j2se.structural.SCCPExample5.main(java.lang.String[]):26)
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSCCP5() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.SCCPExample5", 3);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  /**
   * should not report any alarms
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSCCP6() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.SCCPExample6", 0);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

  /**
   * 
   * demonstrates the false-alarm when conditions checked in finally block
   * 
   * @throws SafeException
   * @throws Exception
   */
  public void testSCCP7() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit("j2se.structural.SCCPExample7", 2);
    test.selectStructuralAnalysis();
    SafeRegressionDriver.run(test);
  }

}
