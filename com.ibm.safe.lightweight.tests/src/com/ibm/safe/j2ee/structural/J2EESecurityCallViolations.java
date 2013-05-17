package com.ibm.safe.j2ee.structural;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.lightweight.options.LightweightProperties;
import com.ibm.safe.lightweight.tests.LightweightRegressionUnit;
import com.ibm.safe.properties.CommonProperties;

public final class J2EESecurityCallViolations extends SafeTCase {

  public void testSecurityViolationHelloBean() throws SafeException, Exception {
    final LightweightRegressionUnit test = new LightweightRegressionUnit("j2ee.structural.security.HelloBean", 17);
    test.selectStructuralAnalysis();
    test.setOption(CommonProperties.Props.MODULES.toString(), "HelloBean.jar,../com.ibm.safe.testdata/libraries/j2ee/j2ee.jar");
    test.setOption(LightweightProperties.Props.SELECT_STRUCTURAL_RULES.toString(), "J2EEDontCallSecurityRules");
    test.setOption(LightweightProperties.Props.SELECT_CLASSES.toString(), ".*Hello.*");
    SafeRegressionDriver.run(test);
  }

  public void testSecurityViolationHelloServlet() throws SafeException, Exception {
    final LightweightRegressionUnit test = new LightweightRegressionUnit("j2ee.structural.security.HelloServlet", 17);
    test.selectStructuralAnalysis();
    test.setOption(CommonProperties.Props.MODULES.toString(), "HelloServlet.jar,../com.ibm.safe.testdata/libraries/j2ee/j2ee.jar");
    test.setOption(LightweightProperties.Props.SELECT_STRUCTURAL_RULES.toString(), "J2EEDontCallSecurityRules");
    test.setOption(LightweightProperties.Props.SELECT_CLASSES.toString(), ".*Hello.*");
    SafeRegressionDriver.run(test);
  }

}
