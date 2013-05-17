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
package com.ibm.safe.internal.reporting;

import javax.xml.parsers.ParserConfigurationException;

import com.ibm.safe.reporting.CompositeReporter;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.utils.SafeLogger;

/**
 * Factory methods to create instance of IReporter interface.
 * 
 * @author egeay
 */
public final class ReporterFactory {

  /**
   * Creates the default reporter for collecting few analysis progress and
   * results. This default reporter is composed of three ones: one on the
   * standard output, another on a log file, and finally one in an XML file (XML
   * analysis result file identified in main SAFE properties file).
   * 
   * @return A reporter that works on standard output, a log file and
   *         <i>eventually</i> an XML file (if we were able to create it).
   */
  public static IReporter createDefaultReporter(final String analysisResultFileName) {
    final CompositeReporter reporter = new CompositeReporter();
    reporter.addReporter(new StandardOutputReporter());
    reporter.addReporter(new LogReporter());
    addXMLReporter(reporter, analysisResultFileName);
    return reporter;
  }

  /**
   * Creates a log reporter to report SAFE analysis results, and XML reporter if
   * we are able to create it (ie no I/O errors during its creation).
   * 
   */
  public static IReporter createLogAndXMLReporter(final String analysisResultFileName) {
    final CompositeReporter reporter = new CompositeReporter();
    reporter.addReporter(new LogReporter());
    addXMLReporter(reporter, analysisResultFileName);
    return reporter;
  }

  /**
   * Creates a standard output and XML reporter to report SAFE analysis results.
   */
  public static IReporter createXMLAndConsoleReporter(final String analysisResultFileName) {
    final CompositeReporter reporter = new CompositeReporter();
    reporter.addReporter(new StandardOutputReporter());
    addXMLReporter(reporter, analysisResultFileName);
    return reporter;
  }

  // --- Private code

  private ReporterFactory() {
  }

  private static void addXMLReporter(final CompositeReporter reporter, final String analysisResultFilename) {
    try {
      reporter.addReporter(new XMLReporter(analysisResultFilename));
    } catch (ParserConfigurationException except) {
      // We simply don't add this reporter.
      SafeLogger.warning("Unable to create XML reporter.");
    }
  }

}
