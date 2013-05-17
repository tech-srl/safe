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

package com.ibm.safe.accesspath;


/**
 * @author sfink
 * @author Eran Yahav
 * 
 * An interface that describes a fall-back for asking alias questions that the
 * access-path solver for some reason fails to answer.
 */
public interface AliasOracle {

  /**
   * Return the set of all possible access paths that might be aliased with x
   * 
   * @param x
   * @return Set<AccessPath>
   */
  public AccessPathSet getGlobalAliases(PointerPathElement x);
}
