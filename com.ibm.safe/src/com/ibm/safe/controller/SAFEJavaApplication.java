/*******************************************************************************
 * Copyright (c) 2004, 2009-2010 IBM Corporation.
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
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.NullProgressMonitor;

import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.reporting.ReporterFactory;
import com.ibm.safe.lightweight.options.LightweightProperties;
import com.ibm.safe.options.WholeProgramProperties;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.properties.CommonProperties.Props;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.properties.PropertiesManager.IPropertyDescriptor;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.typestate.options.TypestateProperties;
import com.ibm.safe.utils.SafeLogger;
import com.ibm.wala.util.WalaException;

/**
 * Starts SAFE via Java command line mode.
 * 
 * @author egeay
 */
public class SAFEJavaApplication extends AbstractSafeJavaApplication {

  public static void main(final String[] args) throws PropertiesException, WalaException {
    LightweightProperties.register();
    TypestateProperties.register();
    SAFEJavaApplication sja = new SAFEJavaApplication();
    sja.run(args, PropertiesManager.initFromCommandLine(args));
  }

  protected void run(final String[] args, final PropertiesManager properties) {
    if (requiresHelp(args)) {
      displayUsage();
    }
    initLoggerProperty();
    try {
      final AbstractSafeController runner = createController(properties);
      final IRule[] rules = runner.getRules();
      final String analysisResultFile = properties.getPathValue(Props.OUTPUT_DIR) + File.separator
          + properties.getStringValue(Props.RESULT_FILENAME);
      assert analysisResultFile != null;
      runner.execute(rules, ReporterFactory.createDefaultReporter(analysisResultFile), new NullProgressMonitor());
    } catch (SafeException except) {
      except.printStackTrace();
      SafeLogger.severe(except.getMessage());
      if (except.getCause() != null) {
        SafeLogger.severe(except.getCause().getMessage());
      }
    } catch (Exception except) {
      except.printStackTrace();
      SafeLogger.severe(getStackStrace(except));
      if (except.getCause() != null) {
        SafeLogger.severe(getStackStrace(except.getCause()));
      }
    }
  }

  protected AbstractSafeController createController(final PropertiesManager propertiesManager) {
    return new GenericSafeController(propertiesManager);
  }

  protected void displaySpecificLauncherOptions() {
    displayUsage("Structural Options", LightweightProperties.Props.values());

    ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
    properties.addAll(Arrays.asList(WholeProgramProperties.Props.values()));
    properties.addAll(Arrays.asList(TypestateProperties.Props.values()));
    displayUsage("Typestate Options", properties.toArray(new IPropertyDescriptor[properties.size()]));

    properties = new ArrayList<IPropertyDescriptor>();
    properties.addAll(Arrays.asList(CommonProperties.Props.values()));
    properties.addAll(Arrays.asList(WholeProgramProperties.Props.values()));
    displayUsage("Null-dereference Options", properties.toArray(new IPropertyDescriptor[properties.size()]));
  }

}
