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
package com.ibm.safe.lightweight.options;

import com.ibm.safe.internal.filtering.AlwaysTrueClassFilter;
import com.ibm.safe.internal.filtering.OrFilter;
import com.ibm.safe.internal.filtering.QualifiedNameFilter;
import com.ibm.safe.internal.filtering.RegularExpressionFilter;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.rules.StructuralRule;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.util.Predicate;

public final class StructuralOptions implements IStructuralOptions {

  private final Predicate<IClass> classFilter;

  private final IRule[] rules;

  private final String dumpXMLDir;

  private final boolean collectStatistics;

  private boolean pessimisticEval;

  private static final char[] REGULAR_EXP_CHARS = { '*', '[', '(', '{', '\\', '|', '?', '+' };

  public StructuralOptions(final IRule[] structuralRules, final String[] classFilterList, final String dumpXMLDirectory,
      final boolean shouldCollectStatistics, final boolean pessimisticEval) {
    this.classFilter = createClassFilter(classFilterList);
    this.rules = structuralRules;
    this.dumpXMLDir = dumpXMLDirectory;
    this.collectStatistics = shouldCollectStatistics;
    this.pessimisticEval = pessimisticEval;
  }

  // --- Interface methods implementation

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.options.IStructuralOptions#getClassFilter()
   */
  public Predicate<IClass> getClassFilter() {
    return this.classFilter;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.options.IStructuralOptions#getRules()
   */
  public StructuralRule[] getRules() {
    return (StructuralRule[])this.rules;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.options.IStructuralOptions#getXMLDumpingDir()
   */
  public String getXMLDumpingDir() {
    return this.dumpXMLDir;
  }

  private Predicate<IClass> createClassFilter(final String[] classFilterList) {
    if (classFilterList.length == 0) {
      return new AlwaysTrueClassFilter<IClass>();
    }
    final OrFilter<IClass> composedFilter = new OrFilter<IClass>();
    for (int i = 0; i < classFilterList.length; i++) {
      if (hasCharacter(classFilterList[i], REGULAR_EXP_CHARS)) {
        composedFilter.addFilter(new RegularExpressionFilter<IClass>(classFilterList[i]));
      } else {
        composedFilter.addFilter(new QualifiedNameFilter<IClass>(classFilterList[i]));
      }
    }
    return composedFilter;
  }

  private boolean hasCharacter(final String theClass, final char[] theChars) {
    for (int i = 0; i < theChars.length; i++) {
      if (theClass.indexOf(theChars[i]) != -1) {
        return true;
      }
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.options.IStructuralOptions#shouldCollectStatistics()
   */
  public boolean shouldCollectStatistics() {
    return this.collectStatistics;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.options.IStructuralOptions#shouldDumpXML()
   */
  public boolean shouldDumpXML() {
    return getXMLDumpingDir() != null;
  }

  public boolean pessimisticEval() {
    return pessimisticEval;
  }

}
