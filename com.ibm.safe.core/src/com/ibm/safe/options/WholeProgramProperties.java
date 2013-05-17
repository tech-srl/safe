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
package com.ibm.safe.options;

import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.properties.PropertiesManager.IPropertyDescriptor;
import com.ibm.safe.properties.PropertiesManager.Type;
import com.ibm.wala.util.debug.Assertions;

/**
 * Properties for typestate SAFE
 * 
 * @author sjfink
 * @author yahave
 * 
 */
public class WholeProgramProperties {

  public static enum Props implements IPropertyDescriptor {

    ALL_MAIN_CLASSES_ENTRY_POINT("all_main_classes_entry_point", Type.BOOLEAN, "Takes all main classes as entry points."), CG_KIND(
        "cg_kind", Type.STRING, "ZERO_ONE_CUSTOM", "Specifies kind of call graph to build for typestate analysis."), ENTRY_POINTS(
        "entry_points", Type.STRING, "Specifies a list of entry points for the analysis."), ENTRY_POINTS_FILE("entry_points_file",
        Type.STRING, "Specifies an XML file where some end-user entry points are defined."), GENERATE_WITNESS("generate_witness",
        Type.BOOLEAN, ""), LIVE_ANALYSIS("live_analysis", Type.BOOLEAN, true, ""), CONTRADICTION_ANALYSIS("contradiction_analysis",
        Type.BOOLEAN, false, "Perform contradiction analysis."), POINTS_TO_GRAPH("points_to_graph", Type.STRING,
        "Outputs points-to graph"), REPORT_CALL_GRAPH("report_call_graph", Type.BOOLEAN, "Outputs or not the call graph."), SELECT_MAIN_CLASSES(
        "select_main_classes", Type.STRING, "Filters some main classes among the ones automatically detected."), SLICE_SUPERGRAPH(
        "slice_supergraph", Type.BOOLEAN, "Enable supergraph slicing.");

    private final String name;

    private final boolean cmdLine;

    private final Type type;

    private final String defaultString;

    private final String description;

    Props(final String name, final Type type, final String defaultString, final String description) {
      this.name = name;
      this.type = type;
      this.cmdLine = true;
      this.defaultString = defaultString;
      this.description = description;
    }

    Props(final String name, final Type type, final boolean defaultBoolean, final String description) {
      this(name, type, defaultBoolean ? "true" : "false", description);
    }

    Props(final String name, final Type type, final String description) {
      this.name = name;
      this.type = type;
      this.cmdLine = true;
      this.description = description;
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

  public static void register() {
    PropertiesManager.registerProperties(WholeProgramProperties.Props.values());
    CommonProperties.register();
  }
}
