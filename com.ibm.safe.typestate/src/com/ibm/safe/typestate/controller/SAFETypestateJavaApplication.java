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
package com.ibm.safe.typestate.controller;

import java.util.ArrayList;
import java.util.Arrays;

import com.ibm.safe.controller.AbstractSafeController;
import com.ibm.safe.controller.AbstractSafeJavaApplication;
import com.ibm.safe.options.WholeProgramProperties;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.properties.PropertiesManager.IPropertyDescriptor;
import com.ibm.safe.typestate.options.TypestateProperties;

/**
 * Starts SAFE via Java command line mode.
 * 
 * @author egeay
 */
public class SAFETypestateJavaApplication extends AbstractSafeJavaApplication {

  public static void main(final String[] args) {
    SAFETypestateJavaApplication sja = new SAFETypestateJavaApplication();
    TypestateProperties.register();
    sja.run(args);
  }

  protected AbstractSafeController createController(final PropertiesManager propertiesManager) {
    return new TypestateController(propertiesManager);
  }

  protected void displaySpecificLauncherOptions() {
    final ArrayList<IPropertyDescriptor> properties = new ArrayList<IPropertyDescriptor>();
    properties.addAll(Arrays.asList(WholeProgramProperties.Props.values()));
    properties.addAll(Arrays.asList(TypestateProperties.Props.values()));
    displayUsage("Typestate Options", properties.toArray(new IPropertyDescriptor[properties.size()]));
  }

}
