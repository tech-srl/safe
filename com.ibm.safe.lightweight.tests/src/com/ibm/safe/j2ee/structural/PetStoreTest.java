package com.ibm.safe.j2ee.structural;

import com.ibm.safe.core.tests.SafeRegressionDriver;
import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.lightweight.tests.LightweightRegressionUnit;
import com.ibm.safe.properties.CommonProperties;

public final class PetStoreTest extends SafeTCase {

  public void testPetStoreStructural() throws SafeException, Exception {
    LightweightRegressionUnit test = new LightweightRegressionUnit(162);
    test.selectStructuralAnalysis();
    test.setOption(CommonProperties.Props.MODULES_DIRS.getName(), "../com.ibm.safe.testdata/libraries/j2ee");
    test.setBooleanOption(CommonProperties.Props.ANALYZE_DEPENDENT_JARS.getName());
    test.setOption(CommonProperties.Props.MODULES.getName(), "petstore.war");
    SafeRegressionDriver.run(test);
  }

}
