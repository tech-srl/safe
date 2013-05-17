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
package com.ibm.safe.lightweight.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import com.ibm.safe.controller.RulesManager;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.lightweight.options.LightweightProperties.Props;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.rules.StructuralRule;

public class LightweightRulesManager extends RulesManager {

  protected final Collection<IRule> structuralRules = new LinkedList<IRule>();

  public LightweightRulesManager(final IRule[] theRules) {
    super(theRules);
  }

  public void applyFilters(final PropertiesManager propertiesManager) throws PropertiesException {
    final Collection<IRule> allStructurals = new ArrayList<IRule>(this.rules.length);

    for (int i = 0; i < this.rules.length; i++) {
      IRule curr = this.rules[i];
      if (curr instanceof StructuralRule) {
        allStructurals.add(this.rules[i]);
      }
    }
    applyFilters(this.structuralRules, allStructurals, propertiesManager.getStringValue(Props.SELECT_STRUCTURAL_RULES));
  }

  public StructuralRule[] getStructuralRules() {
    return this.structuralRules.toArray(new StructuralRule[this.structuralRules.size()]);
  }

}
