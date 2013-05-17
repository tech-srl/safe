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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import com.ibm.safe.controller.RulesManager;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.rules.TypestateRule;
import com.ibm.safe.typestate.options.TypestateProperties;

public class TypestateRulesManager extends RulesManager {

  protected final Collection<IRule> typeStateRules = new LinkedList<IRule>();

  public TypestateRulesManager(final IRule[] theRules) {
    super(theRules);
  }

  public void applyFilters(final PropertiesManager propertiesManager) throws SetUpException, PropertiesException {
    if (rules == null) {
      return;
    }
    final Collection<IRule> allTypeStates = new ArrayList<IRule>(this.rules.length);
    for (int i = 0; i < this.rules.length; i++) {
      IRule curr = this.rules[i];
      if (curr instanceof TypestateRule) {
        allTypeStates.add(this.rules[i]);
      }
    }

    applyFilters(this.typeStateRules, allTypeStates,
        propertiesManager.getStringValue(TypestateProperties.Props.SELECT_TYPESTATE_RULES));
    if (propertiesManager.getBooleanValue(TypestateProperties.Props.MINE_DFA)) {
      // a hack. when mining, ignore rules.
      this.typeStateRules.clear();
    }
  }

  public TypestateRule[] getTypeStateRules() {
    return this.typeStateRules.toArray(new TypestateRule[this.typeStateRules.size()]);
  }

}
