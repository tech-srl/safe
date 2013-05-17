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
package com.ibm.safe.dfa;

import java.util.Collection;
import java.util.Set;

import com.ibm.wala.util.collections.Pair;

public interface IDFAStateMerger {
  public IDFA mergeStates(IDFA dfa, Set<Object> cut);
  public Set<Pair<Object, Object>> computeEquivalence(IDFA dfa, Collection<Object> reachable, Set<Object> cut);
}
