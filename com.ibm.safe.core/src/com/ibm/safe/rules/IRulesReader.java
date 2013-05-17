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
package com.ibm.safe.rules;

import java.io.IOException;


/**
 * Implementation of this interface should be able to read all SAFE EMF rules.
 * It provides the different rules in the environment separated by main
 * categories. At this time, Structural and TypeState. To access the location of
 * the rules, it reads the property value of
 * {@link com.ibm.safe.properties.SafeProperties#RULES_DIRS}.
 * 
 * @author egeay
 * @author eyahav
 */
public interface IRulesReader {

  /**
   * Returns the list of rules present in the given environment.
   * 
   * @post sure[ getRules() != null ] && may[ getRules().length == 0 ]
   */
  public IRule[] getRules();

  /**
   * Loads the rules at the location specified by end-user. Must be called
   * before getStructuralRules() and getTypeStateRules().
   * 
   * @param classLoader
   *            The current class loader.
   */
  public void load(final ClassLoader classLoader) throws IOException;

}
