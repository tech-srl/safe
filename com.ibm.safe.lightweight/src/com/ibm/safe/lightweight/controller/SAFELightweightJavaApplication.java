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
package com.ibm.safe.lightweight.controller;

import com.ibm.safe.controller.AbstractSafeController;
import com.ibm.safe.controller.AbstractSafeJavaApplication;
import com.ibm.safe.lightweight.options.LightweightProperties;
import com.ibm.safe.properties.PropertiesManager;

/**
 * Starts SAFE via Java command line mode.
 * 
 * @author egeay
 * @author eyahav
 */
public class SAFELightweightJavaApplication extends AbstractSafeJavaApplication {

  public static void main(final String[] args) {
    LightweightProperties.register();
    SAFELightweightJavaApplication sja = new SAFELightweightJavaApplication();
    sja.run(args);
  }

  protected AbstractSafeController createController(final PropertiesManager propertiesManager) {
    return new LightweightController(propertiesManager);
  }

  protected void displaySpecificLauncherOptions() {
    displayUsage("Structural Options", LightweightProperties.Props.values());
  }

}
