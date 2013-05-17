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

import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.util.collections.HashSetFactory;

/**
 * @author yahave
 */
public class DFAUtil {

  /**
   * returns the positive projection of a DFA - remove accepting state - remove
   * all edges to accepting state
   */
  public static IDFA positiveProjection(IDFA dfa) {
    IDFA result = (IDFA) dfa.clone();
    Set<Object> accepting = HashSetFactory.make(result.acceptingStates());
    for (Iterator<Object> it = accepting.iterator(); it.hasNext();) {
      Object acceptNode = it.next();
      Set<Object> removeSources = HashSetFactory.make();
      for (Iterator<? extends Object> sit = result.getPredNodes(acceptNode); sit.hasNext();) {
        Object src = sit.next();
        removeSources.add(src);
      }
      for (Iterator<Object> rit = removeSources.iterator(); rit.hasNext();) {
        result.removeAllEdges(rit.next(), acceptNode);
      }
      result.removeNode(acceptNode);
    }
    return result;
  }

}
