/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.typestate.options;

import com.ibm.safe.options.WholeProgramProperties;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.properties.PropertiesManager.IPropertyDescriptor;
import com.ibm.safe.properties.PropertiesManager.Type;
import com.ibm.wala.util.debug.Assertions;

/**
 * Properties for typestate SAFE
 * 
 * @author sjfink
 * 
 */
public class TypestateProperties {

  public static void register() {
    PropertiesManager.registerProperties(TypestateProperties.Props.values());
    WholeProgramProperties.register();
  }

  public static enum Props implements IPropertyDescriptor {

    ABSTRACT_TRACE_FILE_NAME("abstract_trace_file_name", Type.STRING, "Name of output file used in spec mining."), GENERATE_WITNESS(
        "generate_witness", Type.BOOLEAN, ""), MINE_DFA("mine_dfa", Type.BOOLEAN, "Enable specification mining."), MINE_MERGE(
        "mine_merge", Type.STRING, "Simulation", "Specifies kind of merge operator used in spec mining."), MINE_TYPE("mine_type",
        Type.STRING, ""), MINE_CONTEXT("mine_context", Type.INT, 1, ""), MINE_CONTEXT_EVENT("mine_context_event", Type.STRING, ""), MODULAR_SUMMARY(
        "modular_summary", Type.STRING, "Base", ""), MUST_AP_KLIMIT("must_ap_klmit", Type.INT, 2,
        "Access path limit length for Must pointer analysis"), PROPERTY_GRAPH("property_graph", Type.STRING,
        "Outputs the property graph."), SELECT_TYPESTATE_RULES("select_typestate_rules", Type.STRING,
        "Selects a list of typestate rules to apply (file name without extension XMI)."), SLICE_DFA("slice_dfa", Type.BOOLEAN,
        "Use DFA-slicing."), TYPESTATE_SOLVER_KIND("typestate_solver_kind", Type.STRING, "Staged",
        "Specifies kind of solver to use for TypeState analysis.");

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

    Props(final String name, final Type type, final int defaultInt, final String description) {
      this(name, type, String.valueOf(defaultInt), description);
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

}
