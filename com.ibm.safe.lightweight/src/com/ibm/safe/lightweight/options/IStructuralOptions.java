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

import com.ibm.safe.rules.StructuralRule;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.util.Predicate;

/**
 * Base for all options related to structural analysis.
 * 
 * @author egeay
 */
public interface IStructuralOptions {

  /**
   * Returns a filter that allows to select classes to analyze among the modules
   * identified.
   * 
   * @post sure[ getClassesFilter() != null ]
   */
  public Predicate<IClass> getClassFilter();

  /**
   * Returns a directory name where to dump XML files related to ASTs of code
   * analyzed. <b>Always call {@link #shouldDumpXML()} before calling this
   * method</b>.
   * 
   * @post may[ getXMLDumpingDir() == null ]
   */
  public String getXMLDumpingDir();

  /**
   * Returns set of structural rules loaded in memory.
   * 
   * @post See postcondition of getStructuralRules()
   */
  public StructuralRule[] getRules();

  /**
   * Returns true if end-user selected to collect statistics for structural
   * analysis, false otherwise.
   */
  public boolean shouldCollectStatistics();

  /**
   * Returns true if end-user selected to dump XML files related to ASTs of code
   * analyzed, false otherwise.
   */
  public boolean shouldDumpXML();

  /**
   * propagate nulls through joins as long as other value is not bottom (prefer
   * nulls over top)
   */
  public boolean pessimisticEval();

}
