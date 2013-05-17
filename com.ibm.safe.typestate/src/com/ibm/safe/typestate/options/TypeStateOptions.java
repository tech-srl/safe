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
package com.ibm.safe.typestate.options;

import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.options.WholeProgramOptions;
import com.ibm.safe.options.WholeProgramProperties;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.rules.TypestateRule;
import com.ibm.safe.typestate.controller.TypeStateSolverKind;

/**
 * @author egeay
 * @author yahave
 */
public class TypeStateOptions extends WholeProgramOptions {

  private IRule[] rules;

  /**
   * @param typeStateRules
   * @param callGraphKind
   * @param entryPointDefinitions
   * @param pointsToDotFileName
   * @param propertyDotFileName
   * @param typeStateSolverKind
   * @param shouldDumpCallGraph
   */
  public TypeStateOptions(WholeProgramOptions wpOptions) {
    super(wpOptions);
  }

  public TypeStateOptions(PropertiesManager propertiesManager) {
    super(propertiesManager);
  }

  public TypeStateOptions(PropertiesManager propertiesManager, IRule[] rules) {
    super(propertiesManager);
    this.rules = rules;
  }

  public String getPointsToDotFile() throws PropertiesException {
    return getStringValue(WholeProgramProperties.Props.POINTS_TO_GRAPH);
  }

  public String getPropertyDotFile() throws PropertiesException {
    return getStringValue(TypestateProperties.Props.PROPERTY_GRAPH);
  }

  public void setRules(TypestateRule[] rules) {
    this.rules = rules;
  }

  public TypestateRule[] getRules() {
    return (TypestateRule[]) this.rules;
  }

  public boolean shouldDumpCallGraph() throws PropertiesException {
    return getBooleanValue(WholeProgramProperties.Props.REPORT_CALL_GRAPH);
  }

  public boolean shouldCollectStatistics() throws PropertiesException {
    return getBooleanValue(CommonProperties.Props.COLLECT_STATISTICS);
  }

  /**
   * Generate an abstract trace of events, instead of normal typestate
   * verification?
   * 
   * @throws PropertiesException
   */
  public boolean shouldMineDFA() throws PropertiesException {
    return getBooleanValue(TypestateProperties.Props.MINE_DFA);
  }

  public boolean shouldCreatePointsToDotFile() throws PropertiesException {
    return getPointsToDotFile() != null;
  }

  public boolean shouldCreatePropertyDotFile() throws PropertiesException {
    return getPropertyDotFile() != null;
  }

  public boolean shouldSliceDFA() throws PropertiesException {
    return getBooleanValue(TypestateProperties.Props.SLICE_DFA);
  }

  public boolean shouldGenerateWitness() throws PropertiesException {
    return getBooleanValue(TypestateProperties.Props.GENERATE_WITNESS);
  }

  /**
   * @return Returns the accessPathKLimit.
   * @throws PropertiesException
   */
  public int getAccessPathKLimit() throws PropertiesException {
    return getIntValue(TypestateProperties.Props.MUST_AP_KLIMIT);
  }

  public String getMineType() throws PropertiesException {
    return getStringValue(TypestateProperties.Props.MINE_TYPE);
  }

  public String getTypeStateSolverKindString() throws PropertiesException {
    return getStringValue(TypestateProperties.Props.TYPESTATE_SOLVER_KIND);
  }

  public String getMineMergeKindString() throws PropertiesException {
    return getStringValue(TypestateProperties.Props.MINE_MERGE);
  }

  public MineMergeKind getMineMergeKind() throws PropertiesException {
    final MineMergeKind kind = MineMergeKind.getMergeKindFromString(getMineMergeKindString());
    if (kind == null) {
      throw new PropertiesException("Bad merge property kind " + getMineMergeKindString());
    }
    return kind;
  }

  public TypeStateSolverKind getTypeStateSolverKind() throws PropertiesException {
    final TypeStateSolverKind kind = TypeStateSolverKind.getSolverKindFromString(getTypeStateSolverKindString());
    if (kind == null) {
      throw new PropertiesException("Bad solver property kind " + getTypeStateSolverKindString());
    }
    return kind;
  }

  public String getPointsToDotFileName() throws PropertiesException {
    return getStringValue(WholeProgramProperties.Props.POINTS_TO_GRAPH);
  }

  public String getOutputDirectory() throws PropertiesException {
    return getPathValue(CommonProperties.Props.OUTPUT_DIR);
  }

  public String getAbstractTraceFileName() throws PropertiesException {
    String result = getStringValue(TypestateProperties.Props.ABSTRACT_TRACE_FILE_NAME);
    if (result == null) {
      result = "traces.txt";
    }
    return result;
  }

  public String getPropertyDotFileName() throws PropertiesException {
    return getStringValue(TypestateProperties.Props.PROPERTY_GRAPH);
  }

}
