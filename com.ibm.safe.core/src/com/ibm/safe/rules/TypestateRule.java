/*******************************************************************************
 * Copyright (c) 2002-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.rules;

import java.util.ArrayList;
import java.util.List;

import com.ibm.safe.dfa.DFASpec;

public class TypestateRule extends IRule {

  protected List<String> types = new ArrayList<String>();

  protected DFASpec typeStateAutomaton;

  public TypestateRule() {
    super();
  }

  public List<String> getTypes() {
    return types;
  }

  public DFASpec getTypeStateAutomaton() {
    return typeStateAutomaton;
  }

  public DFASpec basicGetTypeStateAutomaton() {
    return typeStateAutomaton;
  }

  public void setTypeStateAutomaton(DFASpec newTypeStateAutomaton) {
    typeStateAutomaton = newTypeStateAutomaton;

  }

  public void addType(String t) {
    types.add(t);
  }

}
