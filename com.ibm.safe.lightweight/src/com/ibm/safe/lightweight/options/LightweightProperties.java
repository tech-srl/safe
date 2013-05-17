/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.lightweight.options;

import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.properties.PropertiesManager.IPropertyDescriptor;
import com.ibm.safe.properties.PropertiesManager.Type;
import com.ibm.wala.util.debug.Assertions;

/**
 * Properties for lightweight SAFE
 * 
 * @author sjfink
 * 
 */
public class LightweightProperties {

  public static void register() {
    PropertiesManager.registerProperties(LightweightProperties.Props.values());
    CommonProperties.register();
  }

  public static enum Props implements IPropertyDescriptor {

    DUMP_XML_DIRECTORY("dump_xml", Type.STRING, "Specifies that we should dump created XMLs for Java code inspected."), SELECT_CLASSES(
        "select_classes", Type.STRING, "Selects a set of classes to analyze among the modules identified."), SELECT_STRUCTURAL_RULES(
        "select_structural_rules", Type.STRING, "Selects a list of structural rules to apply (file name without extension XMI)."), PESSIMISTIC_EVAL(
        "pessimistic_eval", Type.BOOLEAN, "Use pessimistic evaluation.");

    private final String name;

    private final boolean cmdLine;

    private final Type type;

    private final String defaultString;

    private final String description;

    Props(final String name, final Type type, final String description) {
      this.name = name;
      this.type = type;
      this.description = description;
      this.cmdLine = true;
      switch (type) {
      case BOOLEAN:
        this.defaultString = "false";
        break;
      case INT:
        this.defaultString = null;
        break;
      case STRING:
        this.defaultString = null;
        break;
      default:
        Assertions.UNREACHABLE();
        this.defaultString = null;
      }
    }

    public String getName() {
      return name;
    }

    public boolean isCommandLineOption() {
      return cmdLine;
    }

    public Type getType() {
      return type;
    }

    public String getDefaultAsString() {
      return defaultString;
    }

    public String getDescription() {
      return this.description;
    }

  }
}
