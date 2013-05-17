/*******************************************************************************
 * Copyright (c) 2004-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.core.tests;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.ibm.safe.controller.AbstractSafeJavaApplication;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.properties.CommonProperties;

/**
 * Common class for all SAFE test cases
 * 
 * @author egeay
 * @author sfink
 */
public class SafeRegressionUnit {
  /**
   * expected number of findings (use -1 if you want no comparison of expected
   * findings)
   */
  private final int expectedNumberOfFindings;

  /**
   * miscellaneous analysis options
   */
  private final Map<String, String> options = new HashMap<String, String>(20);

  /**
   * @param expectedNumberOfFindings
   * @throws SafeException
   */
  public SafeRegressionUnit(int expectedNumberOfFindings) throws SafeException {
    this(null, expectedNumberOfFindings);
  }

  /**
   * @param mainClassName
   *            name of main class, including package. can be null.
   * @param expectedNumberOfFindings
   * @throws SafeException
   */
  public SafeRegressionUnit(String mainClassName, int expectedNumberOfFindings) throws SafeException {
    this.expectedNumberOfFindings = expectedNumberOfFindings;

    if (mainClassName != null) {
      String unqualifiedName = mainClassName.substring(mainClassName.lastIndexOf('.') + 1);
      setOption(CommonProperties.Props.MODULES.getName(), unqualifiedName + ".jar");
      setOption(CommonProperties.Props.MAIN_CLASSES.getName(), mainClassName);
    }
    setOption(CommonProperties.Props.RULES_DIRS.getName(), createRulesDirsOption());

    setOption(CommonProperties.Props.MODULES_DIRS.getName(), "../com.ibm.safe.testdata/jars"); //$NON-NLS-1$
    setBooleanOption(CommonProperties.Props.VERBOSE.getName());
    // enableWitnessGeneration();
  }

  public final void setOption(final String optionName, final String optionArgument) {
    this.options.put(optionName, optionArgument);
  }

  public final void setBooleanOption(final String optionName) {
    setBooleanOption(optionName, true);
  }

  public final void setBooleanOption(final String optionName, boolean value) {
    this.options.put(optionName, value ? "true" : "false");
  }

  public final void setIntegerOption(final String optionName, int value) {
    this.options.put(optionName, String.valueOf(value));
  }

  private String createRulesDirsOption() throws SafeException {
    return createRulesDirsOption("");
  }

  protected String createRulesDirsOption(String subdir) throws SafeException {
    final String rootPath = getRootPath();
    final StringBuffer buf = new StringBuffer(rootPath);
    buf.append("com.ibm.safe.core/nl/en" + subdir + ";").append(rootPath) //$NON-NLS-1$
        .append("com.ibm.safe.tests/nl/en" + subdir); //$NON-NLS-1$

    return buf.toString();
  }

  protected String getRootPath() throws SafeException {
    URL url = AbstractSafeJavaApplication.class.getClassLoader().getResource("safe.properties"); //$NON-NLS-1$
    if (url == null) {
      throw new SafeException("null url for safe.properties");
    }
    return url.getFile().substring(0, url.getFile().lastIndexOf("com.ibm.safe")); //$NON-NLS-1$
  }

  protected String getSafeHome() throws SafeException {
    return getRootPath() + "com.ibm.safe";
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public int getExpectedNumberOfFindings() {
    return expectedNumberOfFindings;
  }

}
