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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.lightweight.options.LightweightProperties;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.properties.PropertiesManager.IPropertyDescriptor;
import com.ibm.safe.properties.PropertiesManager.Type;
import com.ibm.safe.typestate.options.TypestateProperties;

public final class EclipseHeadlessApplication implements IApplication {

  public Object start(final IApplicationContext context) throws Exception {
    Job.getJobManager().join(ResourcesPlugin.FAMILY_AUTO_BUILD, null);

    LightweightProperties.register();
    TypestateProperties.register();
    PropertiesManager.registerProperties(Props.values());

    final SAFEJavaApplication javaApp = new SAFEJavaApplication();
    final String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
    try {
      javaApp.run(args, PropertiesManager.initFromMap(getArgsMap(args)));
    } catch (PropertiesException except) {
      throw new IllegalArgumentException(except);
    }
    return IApplication.EXIT_OK;
  }

  public void stop() {
    // Nothing to do for us to my knowledge.
  }

  // --- Private code

  private Map<String, String> getArgsMap(final String[] arguments) {
    final Map<String, String> argumentMap = new HashMap<String, String>(50);

    for (final String argument : arguments) {
      if (argument.charAt(0) == '-') {
        final int separatorIndex = argument.indexOf('=');
        if (separatorIndex != -1) {
          argumentMap.put(argument.substring(1, separatorIndex), argument.substring(separatorIndex + 1));
        } else {
          argumentMap.put(argument.substring(1), "true"); //$NON-NLS-1$
        }
      }
    }

    return argumentMap;
  }

  public static enum Props implements IPropertyDescriptor {

    PDELAUNCH("pdelaunch");

    Props(final String theName) {
      this.name = theName;
    }

    // --- Interface methods implementation

    public String getDefaultAsString() {
      return null;
    }

    public String getDescription() {
      return null;
    }

    public String getName() {
      return this.name;
    }

    public Type getType() {
      return Type.STRING;
    }

    public boolean isCommandLineOption() {
      return false;
    }

    // --- Fields

    private final String name;

  }

}
