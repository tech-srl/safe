/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.lightweight.tests;

import com.ibm.safe.core.tests.SafeRegressionUnit;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.lightweight.options.LightweightProperties;
import com.ibm.safe.properties.CommonProperties;

public class LightweightRegressionUnit extends SafeRegressionUnit {

  static {
    LightweightProperties.register();
  }

  private static final String STRUCTURAL_SUBDIR = "/rules/structural";

  private static final String ACCESSIBILITY_SUBDIR = "/rules/accessibility";

  public LightweightRegressionUnit(String mainClassName, int expectedNumberOfFindings) throws SafeException {
    super(mainClassName, expectedNumberOfFindings);
    setOption(CommonProperties.Props.MODULES_DIRS.getName(), "../com.ibm.safe.lightweight.testdata/jars"); //$NON-NLS-1$
  }

  public LightweightRegressionUnit(int expectedNumberOfFindings) throws SafeException {
    super(expectedNumberOfFindings);
  }

  public void selectStructuralAnalysis() throws SafeException {
    setBooleanOption(CommonProperties.Props.STRUCTURAL.getName());
    setOption(CommonProperties.Props.RULES_DIRS.getName(), getStructuralRulesDirs());
  }

  public void selectStructuralAccessibilityAnalysis() throws SafeException {
    setBooleanOption(CommonProperties.Props.STRUCTURAL.getName());
    setOption(CommonProperties.Props.RULES_DIRS.getName(), getAccessibilityRulesDirs());
  }

  public void selectStructuralRule(String ruleName) throws SafeException {
    setOption(LightweightProperties.Props.SELECT_STRUCTURAL_RULES.getName(), ruleName);
  }

  private String getStructuralRulesDirs() throws SafeException {
    return createRulesDirsOption(STRUCTURAL_SUBDIR);
  }

  private String getAccessibilityRulesDirs() throws SafeException {
    return createRulesDirsOption(ACCESSIBILITY_SUBDIR);
  }
}
