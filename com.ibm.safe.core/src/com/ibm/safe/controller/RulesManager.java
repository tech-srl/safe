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
package com.ibm.safe.controller;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import com.ibm.safe.rules.IRule;

public class RulesManager {

  public static final String LIST_REGEX_SEPARATOR = "[,;]\\s*"; //$NON-NLS-1$

  protected final IRule[] rules;

  protected RulesManager(final IRule[] theRules) {
    this.rules = theRules;
  }

  protected void applyFilters(final Collection<IRule> finalRules, final Collection<IRule> allRules, final String propertyValue) {
    if (propertyValue != null) {
      final String[] selectedXMI = propertyValue.split(RulesManager.LIST_REGEX_SEPARATOR);

      if (selectedXMI.length > 0) {
        for (Iterator<IRule> iter = allRules.iterator(); iter.hasNext();) {
          final IRule rule = iter.next();
          final String resource = rule.getFileName();
          if (resource != null) {
            final String xmiFileName = resource.substring(resource.lastIndexOf(File.separatorChar) + 1);
            final String fileName = xmiFileName.substring(0, xmiFileName.lastIndexOf('.'));
            for (int i = 0; i < selectedXMI.length; i++) {
              if (fileName.equals(selectedXMI[i])) {
                finalRules.add(rule);
              }
            }
          } else {
            finalRules.add(rule);
          }

        }
      }
    } else {
      finalRules.addAll(allRules);
    }
  }
}
