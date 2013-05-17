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
package com.ibm.safe.typestate.tests;

import com.ibm.safe.core.tests.SafeRegressionUnit;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.options.WholeProgramProperties;
import com.ibm.safe.properties.CommonProperties;
import com.ibm.safe.typestate.controller.TypeStateSolverKind;
import com.ibm.safe.typestate.options.TypestateProperties;

public class TypestateRegressionUnit extends SafeRegressionUnit {

  private static final String TYPESTATE_SUBDIR = "/rules/typestate";

  static {
    TypestateProperties.register();
  }

  public TypestateRegressionUnit(String mainClassName, int expectedNumberOfFindings) throws SafeException {
    super(mainClassName, expectedNumberOfFindings);
    setOption(CommonProperties.Props.MODULES_DIRS.getName(), "../com.ibm.safe.typestate.testdata/jars"); //$NON-NLS-1$
  }

  public TypestateRegressionUnit(int expectedNumberOfFindings) throws SafeException {
    super(expectedNumberOfFindings);
  }

  private String getTypestateRulesDirs() throws SafeException {
    return createRulesDirsOption(TYPESTATE_SUBDIR);
  }

  public void selectBaseTypestateSolver() {
    setOption(TypestateProperties.Props.TYPESTATE_SOLVER_KIND.getName(), TypeStateSolverKind.BASE.toString());
    setBooleanOption(CommonProperties.Props.TYPESTATE.getName());
  }

  public void selectSeparatingTypestateSolver() {
    setOption(TypestateProperties.Props.TYPESTATE_SOLVER_KIND.getName(), TypeStateSolverKind.SEPARATING.toString());
    setBooleanOption(CommonProperties.Props.TYPESTATE.getName());
  }

  public void selectLocalMMNSolver() {
    setOption(TypestateProperties.Props.TYPESTATE_SOLVER_KIND.getName(), TypeStateSolverKind.LOCAL_MUST_MUSTNOT.toString());
    setBooleanOption(CommonProperties.Props.TYPESTATE.getName());
  }

  public void selectStrongUpdateTypestateSolver() {
    setOption(TypestateProperties.Props.TYPESTATE_SOLVER_KIND.getName(), TypeStateSolverKind.STRONG_UPDATE.toString());
    setBooleanOption(CommonProperties.Props.TYPESTATE.getName());
  }

  public void selectUniqueTypestateSolver() {
    setOption(TypestateProperties.Props.TYPESTATE_SOLVER_KIND.getName(), TypeStateSolverKind.UNIQUE.toString());
    setBooleanOption(CommonProperties.Props.TYPESTATE.getName());
  }

  public void selectAPMustTypestateSolver() {
    setOption(TypestateProperties.Props.TYPESTATE_SOLVER_KIND.getName(), TypeStateSolverKind.AP_MUST.toString());
    setBooleanOption(CommonProperties.Props.TYPESTATE.getName());
  }

  public void selectAPMustMustNotTypestateSolver() {
    setOption(TypestateProperties.Props.TYPESTATE_SOLVER_KIND.getName(), TypeStateSolverKind.AP_MUST_MUSTNOT.toString());
    setBooleanOption(CommonProperties.Props.TYPESTATE.getName());
  }

  public void selectMultipleObjectsSolver() {
    setOption(TypestateProperties.Props.TYPESTATE_SOLVER_KIND.getName(), TypeStateSolverKind.MULTIPLE_OBJECTS.toString());
    setBooleanOption(CommonProperties.Props.TYPESTATE.getName());
  }

  /**
   * 
   */
  public void selectStagedTypestateSolver() {
    setOption(TypestateProperties.Props.TYPESTATE_SOLVER_KIND.getName(), TypeStateSolverKind.STAGED.toString());
    setBooleanOption(CommonProperties.Props.TYPESTATE.getName());
  }

  /**
   * 
   */
  public void selectNullDerefSolver() {
    setOption(TypestateProperties.Props.TYPESTATE_SOLVER_KIND.getName(), TypeStateSolverKind.NULL_DEREF.toString());
    setBooleanOption(CommonProperties.Props.TYPESTATE.getName());
  }

  public void selectTVLASolver() {
    setOption(TypestateProperties.Props.TYPESTATE_SOLVER_KIND.getName(), TypeStateSolverKind.TVLA.toString());
    setBooleanOption(CommonProperties.Props.TYPESTATE.getName());
  }

  /**
   * @param rule
   */
  public void selectTypestateRule(String rule) {
    setOption(TypestateProperties.Props.SELECT_TYPESTATE_RULES.getName(), rule);
  }

  public void selectTypestateAnalysis() throws SafeException {
    setBooleanOption(CommonProperties.Props.TYPESTATE.getName());
    setOption(CommonProperties.Props.RULES_DIRS.getName(), getTypestateRulesDirs());
  }

  public void disableSupergraphSlicing() {
    setBooleanOption(WholeProgramProperties.Props.SLICE_SUPERGRAPH.getName(), false);
  }

  public void disableDFASlicing() {
    setBooleanOption(TypestateProperties.Props.SLICE_DFA.getName(), false);
  }

  // public void enableWitnessGeneration() {
  // setBooleanOption(SafeProperties.GENERATE_WITNESS, true);
  // }
  //
  // public void disableWitnessGeneration() {
  // setBooleanOption(SafeProperties.GENERATE_WITNESS, false);
  // }

  public void enableLiveAnalysis() {
    setBooleanOption(WholeProgramProperties.Props.LIVE_ANALYSIS.getName(), true);
  }

  public void disableLiveAnalysis() {
    setBooleanOption(WholeProgramProperties.Props.LIVE_ANALYSIS.getName(), false);
  }

  public void setAPMustKLimit(int k) {
    setIntegerOption(TypestateProperties.Props.MUST_AP_KLIMIT.getName(), k);
  }

  public void selectRTA() {
    setOption(WholeProgramProperties.Props.CG_KIND.getName(), "RTA");
  }

  public void addLibraryToScope(String str) throws SafeException {
    String modulesDir = getSafeHome() + "/" + getOptions().get(CommonProperties.Props.MODULES_DIRS.getName());
    String currentModules = getOptions().get(CommonProperties.Props.MODULES.getName());
    assert (currentModules != null);
    String libString = modulesDir + "/../lib/" + str;
    String newString = currentModules + "," + libString;
    setOption(CommonProperties.Props.MODULES.getName(), newString);

  }
}
