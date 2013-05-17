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
/*
 * $Id: AbstractTrace.java,v 1.3 2010/10/17 01:20:03 eyahav Exp $
 */
package com.ibm.safe.typestate.mine;

import java.util.HashSet;
import java.util.Set;

import com.ibm.safe.dfa.DFASpec;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

/**
 * Represents a history (trace) and the instances, solver, and merger used for obtaining it. 
 * @author yahave
 */
public class AbstractTrace {
  protected DFASpec dfa = null;

  protected String program;

  protected String type;

  protected Set<InstanceKey> instances = new HashSet<InstanceKey>();

  protected String solver;

  protected String merger;

  public AbstractTrace() {
    super();
  }

  public DFASpec getDfa() {
    return dfa;
  }

  public void setDfa(DFASpec newDfa) {
    dfa = newDfa;
  }

  public String getProgram() {
    return program;
  }

  public void setProgram(String newProgram) {
    program = newProgram;
  }

  public String getType() {
    return type;
  }

  public void setType(String newType) {
    type = newType;
  }

  public Set<InstanceKey> getInstances() {
    return instances;
  }

  public String getSolver() {
    return solver;
  }

  public void setSolver(String newSolver) {
    solver = newSolver;
  }

  public String getMerger() {
    return merger;
  }

  public void setMerger(String newMerger) {
    merger = newMerger;
  }

  public String toString() {
    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (program: ");
    result.append(program);
    result.append(", type: ");
    result.append(type);
    result.append(", solver: ");
    result.append(solver);
    result.append(", merger: ");
    result.append(merger);
    result.append(')');
    return result.toString();
  }

  public void addInstances(Set<InstanceKey> inst) {
    instances.addAll(inst);
  }

}
