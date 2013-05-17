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
package com.ibm.safe.controller;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.reporting.ReporterFactory;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.properties.CommonProperties.Props;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.properties.PropertiesManager.IPropertyDescriptor;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.utils.SafeLogger;

public abstract class AbstractSafeJavaApplication {

  protected abstract void displaySpecificLauncherOptions();

  // --- Private code

  /**
   * the actual entry point of the application
   */
  protected void run(final String[] args) {
    if (requiresHelp(args)) {
      displayUsage();
    }
    initLoggerProperty();
    try {
      PropertiesManager propertiesManager = PropertiesManager.initFromCommandLine(args);
      final AbstractSafeController runner = createController(propertiesManager);
      final IRule[] rules = runner.getRules();
      final String analysisResultFile = propertiesManager.getPathValue(Props.OUTPUT_DIR) + File.separator
          + propertiesManager.getStringValue(Props.RESULT_FILENAME);
      runner.execute(rules, ReporterFactory.createDefaultReporter(analysisResultFile), new NullProgressMonitor());
    } catch (SafeException except) {
      SafeLogger.severe(except.getMessage());
      System.err.println(except.getMessage());
      if (except.getCause() != null) {
        SafeLogger.severe(except.getCause().getMessage());
      }
      System.exit(-1);
    } catch (Exception except) {
      SafeLogger.severe(getStackStrace(except));
      except.printStackTrace();
      if (except.getCause() != null) {
        SafeLogger.severe(getStackStrace(except.getCause()));
      }
      System.exit(-1);
    }
  }

  protected String getStackStrace(final Throwable exception) {
    final StringWriter strWriter = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(strWriter);
    exception.printStackTrace(printWriter);
    printWriter.close();
    return strWriter.toString();
  }

  protected void initLoggerProperty() {
    final String loggingConfigFile = System.getProperty(LOGGING_CONFIG_FILE);
    if (loggingConfigFile == null) {

      URL resource = AbstractSafeJavaApplication.class.getClassLoader().getResource(SAFE_LOG_FILE);
      assert resource != null;
      System.setProperty(LOGGING_CONFIG_FILE, resource.getFile());
    }
  }

  protected boolean requiresHelp(final String[] javaAppArguments) {
    return ((javaAppArguments.length == 0) || ((javaAppArguments.length == 1) && ((javaAppArguments[0].equals("-h") || //$NON-NLS-1$
        (javaAppArguments[0].equals("--help")) || //$NON-NLS-1$
    (javaAppArguments[0].equals("/?")))))); //$NON-NLS-1$
  }

  protected final void displayUsage(final String optionsGroupingName, final IPropertyDescriptor[] propertyDescriptors) {
    System.out.println(optionsGroupingName);
    for (IPropertyDescriptor descriptor : propertyDescriptors) {
      if (!descriptor.isCommandLineOption())
        continue;

      final StringBuilder lineBuilder = new StringBuilder();

      lineBuilder.append("  - ").append(descriptor.getName()).append('=');
      switch (descriptor.getType()) {
      case BOOLEAN:
        lineBuilder.append("[true,false]");
        break;
      case INT:
        lineBuilder.append("[integer]");
        break;
      case STRING:
        lineBuilder.append("[string]");
        break;
      case PATH:
        lineBuilder.append("[path]");
        break;
      }
      lineBuilder.append('\t').append(descriptor.getDescription());

      System.out.println(lineBuilder.toString());
    }
  }

  protected final void displayUsage() {
    System.out.println("Usage: safe [options]\n");
    displayUsage("Common Options:", CommonProperties.Props.values());

    displaySpecificLauncherOptions();

    System.exit(0);
  }

  protected abstract AbstractSafeController createController(final PropertiesManager properties);

  protected static final String LOGGING_CONFIG_FILE = "java.util.logging.config.file"; //$NON-NLS-1$

  protected static final String SAFE_LOG_FILE = "safelog.properties"; //$NON-NLS-1$

}
