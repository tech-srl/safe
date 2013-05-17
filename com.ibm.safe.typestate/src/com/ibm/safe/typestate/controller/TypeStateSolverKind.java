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

/**
 * Enumeration that distinguishes all different kinds of typestate solver
 * possible for analysis.
 * 
 * @author egeay
 */
public final class TypeStateSolverKind {

  public static final TypeStateSolverKind BASE = new TypeStateSolverKind("Base"); //$NON-NLS-1$

  public static final TypeStateSolverKind SEPARATING = new TypeStateSolverKind("Separating"); //$NON-NLS-1$

  public static final TypeStateSolverKind LOCAL_MUST_MUSTNOT = new TypeStateSolverKind("LocalMustMustNot"); //$NON-NLS-1$

  public static final TypeStateSolverKind STRONG_UPDATE = new TypeStateSolverKind("StrongUpdate"); //$NON-NLS-1$

  public static final TypeStateSolverKind UNIQUE = new TypeStateSolverKind("Unique"); //$NON-NLS-1$

  public static final TypeStateSolverKind AP_MUST = new TypeStateSolverKind("APMust"); //$NON-NLS-1$

  public static final TypeStateSolverKind AP_MUST_MUSTNOT = new TypeStateSolverKind("APMustMustNot"); //$NON-NLS-1$

  public static final TypeStateSolverKind NULL_DEREF = new TypeStateSolverKind("NullDeRef"); //$NON-NLS-1$

  public static final TypeStateSolverKind MULTIPLE_OBJECTS = new TypeStateSolverKind("MultipleObjects"); //$NON-NLS-1$

  public static final TypeStateSolverKind MULTIPLE_OBJECTS_FLOW_INSENSITIVE = new TypeStateSolverKind(
      "MultipleObjectsFlowInsensitive");;

  public static final TypeStateSolverKind STAGED = new TypeStateSolverKind("Staged");

  public static final TypeStateSolverKind TVLA = new TypeStateSolverKind("TVLA"); //$NON-NLS-1$

  public static final TypeStateSolverKind MODULAR = new TypeStateSolverKind("MODULAR"); //$NON-NLS-1$;

  public static final TypeStateSolverKind[] allKinds = { BASE, SEPARATING, LOCAL_MUST_MUSTNOT, STRONG_UPDATE, UNIQUE, AP_MUST,
      AP_MUST_MUSTNOT, NULL_DEREF, MULTIPLE_OBJECTS, MULTIPLE_OBJECTS_FLOW_INSENSITIVE, STAGED, TVLA, MODULAR };

  /**
   * Returns the TypeStateSolverKind instance related to a string representation
   * of a solver kind.
   * 
   * @post may[ getSolverKindFromString() == null ]
   */
  public static TypeStateSolverKind getSolverKindFromString(final String solverKind) {
    for (int i = 0; i < allKinds.length; i++) {
      if (allKinds[i].toString().compareToIgnoreCase(solverKind) == 0) {
        return allKinds[i];
      }
    }
    return null;
  }

  /**
   * Returns a textual representation of the typestate solver kind encapsulated.
   */
  public String toString() {
    return this.solverKind;
  }

  private TypeStateSolverKind(final String typeStateSolverKind) {
    this.solverKind = typeStateSolverKind;
  }

  private final String solverKind;

}
