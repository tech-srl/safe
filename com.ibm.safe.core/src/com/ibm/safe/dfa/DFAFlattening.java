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
import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.intset.IntegerUnionFind;
import com.ibm.wala.util.intset.MutableMapping;

/**
 * @author yahave
 * @author sharonsh 
 */
public class DFAFlattening extends DFAStateMerger {

  private static DFAFlattening theInstance;

  public static DFAFlattening getInstance() {
    if (theInstance == null) {
      theInstance = new DFAFlattening();
    }
    return theInstance;
  }

  /**
   * singleton
   */
  private DFAFlattening() {
  }

  public IDFA flatten(IDFA dfa, Set<Object> cut) {
    return mergeStates(dfa, cut);
  }

  public IDFA flatten(IDFA dfa, Set<Object> cut, IDFAStateFactory stateFactory) {
    return mergeStates(dfa, cut, stateFactory);
  }

  /**
   * compute the state-equivalence relation
   * 
   * @param dfa
   * @param reachable
   * @return a set of pairs (p,q) where states p and q are equivalent
   */
  /**
   * compute the state-equivalence relation
   * 
   * @param dfa
   * @param reachable
   * @param toMerge --
   *            initial set of states to merge
   * @return a set of pairs (p,q) where states p and q are equivalent
   */
  public Set<Pair<Object, Object>> computeEquivalence(IDFA dfa, Collection<Object> reachable, Set<Object> toMerge) {

    IntegerUnionFind equivUF = new IntegerUnionFind();

    MutableMapping<Object> states = MutableMapping.make();

    boolean first = true;
    int mergeRep = 0;

    for (Iterator<Object> stateIt = reachable.iterator(); stateIt.hasNext();) {
      Object state = stateIt.next();
      // stateToInt.put(state, new Integer(i));
      int i = states.add(state);
      if (toMerge.contains(state)) {
        if (first) {
          first = false;
          mergeRep = i;
        } else {
          equivUF.union(mergeRep, i);
        }
      }
    }

    // maybe remember "changed" bit for each equivalence class?
    // avoid rechecking states in equivalence classes that were not changed
    boolean changedUF = false;
    do {
      changedUF = false;
      for (Iterator<Object> stateIt1 = reachable.iterator(); stateIt1.hasNext();) {
        Object state1 = stateIt1.next();
        for (Iterator<Object> stateIt2 = reachable.iterator(); stateIt2.hasNext();) {
          Object state2 = stateIt2.next();
          if (state1 != state2 && equivUF.find(states.getMappedIndex(state1)) == equivUF.find(states.getMappedIndex(state2))) {
            for (Iterator<Object> labelIt = dfa.alphabet().iterator(); labelIt.hasNext();) {
              Object label = labelIt.next();
              Object nextDest1 = dfa.successor(state1, label);
              Object nextDest2 = dfa.successor(state2, label);
              if (nextDest1 != null && nextDest2 != null) {
                int class1 = equivUF.find(states.getMappedIndex(nextDest1));
                int class2 = equivUF.find(states.getMappedIndex(nextDest2));
                if (class1 != class2) {
                  equivUF.union(class1, class2);
                  changedUF = true;
                }
              }
            }
          }
        }
      }
    } while (changedUF);

    // equivalence is already the partition. Convert it into a set of
    // equivalent pairs.
    // (redundant -- later transformed into a partition..)
    Set<Pair<Object, Object>> equivPairs = HashSetFactory.make();
    for (Iterator<Object> stateIt1 = reachable.iterator(); stateIt1.hasNext();) {
      Object state1 = stateIt1.next();
      for (Iterator<Object> stateIt2 = reachable.iterator(); stateIt2.hasNext();) {
        Object state2 = stateIt2.next();
        /*
         * if (state1 != state2 && equivalence.find(state1) ==
         * equivalence.find(state2)){ equivPairs.add(new Pair(state1, state2)); }
         */
        if (state1 != state2 && equivUF.find(states.getMappedIndex(state1)) == equivUF.find(states.getMappedIndex(state2))) {
          equivPairs.add(Pair.make(state1, state2));
        }
      }
    }

    return equivPairs;
  }
}
