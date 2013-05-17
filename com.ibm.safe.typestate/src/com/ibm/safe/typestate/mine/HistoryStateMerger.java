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
package com.ibm.safe.typestate.mine;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.safe.dfa.DFA;
import com.ibm.safe.dfa.IDFA;
import com.ibm.safe.dfa.IDFAStateFactory;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.IndiscriminateFilter;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.graph.traverse.DFS;
import com.ibm.wala.util.intset.IntegerUnionFind;
import com.ibm.wala.util.intset.MutableMapping;

/**
 * @author sharonsh
 * @author yahave
 *
 */
public class HistoryStateMerger {

  private static HistoryStateMerger theInstance;

  public static HistoryStateMerger getInstance() {
    if (theInstance == null) {
      theInstance = new HistoryStateMerger();
    }
    return theInstance;
  }

  public AbstractHistory flatten(AbstractHistory history, IDFAStateFactory stateFactory) {
    return mergeStates(history, stateFactory);
  }

  protected AbstractHistory mergeStates(AbstractHistory history, IDFAStateFactory stateFactory) {

    Map /* state -> set of states */<Object, Set> partitionMap = HashMapFactory.make();
    Set /* set of states */<Set> partition = HashSetFactory.make();

    IDFA dfa = history.getDfa();
    Collection cut = history.getCurrentStates();
    // TODO: cut and currentStates are identical

    Collection<Object> reachable = DFS.getReachableNodes(dfa, Collections.singleton(dfa.getInitialState()), IndiscriminateFilter
        .singleton());

    Set /* <Pair> */<Pair> equivalence = computeEquivalence(dfa, reachable, cut);

    Set<Object> ignoreSet = HashSetFactory.make();

    Collection currStates = history.getCurrentStates();

    Set<Set> current = HashSetFactory.make();

    for (Iterator<Object> srcIt = reachable.iterator(); srcIt.hasNext();) {
      Object src = srcIt.next();
      if (ignoreSet.contains(src)) {
        continue;
      }
      // sharon: use currFlag in order to avoid adding a still changing set to
      // the HashSet current
      boolean currFlag = false;
      Set<Object> nodeSet = HashSetFactory.make();
      nodeSet.add(src);
      // sharon: hope that it is OK to update the map while nodeSet is still
      // changing
      partitionMap.put(src, nodeSet);
      if (currStates.contains(src)) {
        currFlag = true;
      }

      for (Iterator<Object> destIt = reachable.iterator(); destIt.hasNext();) {
        Object dest = destIt.next();
        if ((dest != src) && equivalence.contains(Pair.make(src, dest))) {
          // sharon: added check src != dest
          nodeSet.add(dest);
          ignoreSet.add(dest);
          // sharon: hope that it is OK to update the map while nodeSet is still
          // changing
          partitionMap.put(dest, nodeSet);
          if (currStates.contains(dest)) {
            currFlag = true;
            current.add(nodeSet);
          }
        }
      }
      partition.add(nodeSet);
      if (currFlag) {
        current.add(nodeSet);
      }
    }

    return constructNewHistory(history, partitionMap, partition, current, stateFactory);
  }

  /**
   * Construct the minimized automaton, given the equivalence classes (as
   * partition of original DFA states). Note that we are using the equivalence
   * classes (sets of states) as states for the new DFA. While this is not as
   * efficient as it could be, it greatly simplifies debugging and maintains a
   * visible relationship between the original and the minimized DFAs.
   * 
   * @param dfa -
   *            original DFA
   * @param partitionMap -
   *            partition map, mapping original states to their current
   *            equivalence class (set of states)
   * @param partition -
   *            a set of equivalnce clsses (sets of states)
   * @param current -
   *            a set of current states for the history
   * @return the new minimized automaton over equivalence classes, instead of
   *         over states.
   */
  protected AbstractHistory constructNewHistory(AbstractHistory history, Map<Object, Set> partitionMap, Set<Set> partition,
      Set<Set> current, IDFAStateFactory stateFactory) {

    // map from stateClass to new automaton states
    Map<Object, Object> classes2states = HashMapFactory.make();

    IDFA dfa = history.getDfa();

    Collection<Object> resultCurrentStates = HashSetFactory.make();

    // create new DFA
    Object initialClass = partitionMap.get(dfa.getInitialState());
    Object newInitState = stateFactory.createState(initialClass.toString());
    classes2states.put(initialClass, newInitState);

    // sharon: fixed BUG: initial state was not checked
    if (current.contains(initialClass)) {
      resultCurrentStates.add(newInitState);
    }

    IDFA resultDFA = new DFA(newInitState);

    // first add all states
    for (Iterator<Set> partitionIt = partition.iterator(); partitionIt.hasNext();) {
      Set stateClass = partitionIt.next();
      if (!(stateClass == initialClass)) {
        Object newState = stateFactory.createState(stateClass.toString());
        resultDFA.addNode(newState, false);
        classes2states.put(stateClass, newState);
        if (current.contains(stateClass)) {
          resultCurrentStates.add(newState);
        }
      }
    }

    // then add edges
    for (Iterator<Set> partitionIt = partition.iterator(); partitionIt.hasNext();) {
      Set stateClass = partitionIt.next();
      Object newState = classes2states.get(stateClass);
      for (Iterator stateIt = stateClass.iterator(); stateIt.hasNext();) {
        Object state = stateIt.next();
        for (Iterator labelIt = dfa.alphabet().iterator(); labelIt.hasNext();) {
          Object label = labelIt.next();
          Object successor = dfa.successor(state, label);
          if (successor != null) {
            Object succStateClass = partitionMap.get(successor);
            Object succNewState = classes2states.get(succStateClass);
            resultDFA.addLabeledEdge(newState, succNewState, label);
          }
        }
      }
    }
    return new AbstractHistory(resultDFA, resultCurrentStates, history.getMerger());
  }

  /**
   * compute the state-equivalence relation
   * 
   * @param dfa
   * @param reachable
   * @param toMerge --
   *            initial set of states to merge
   * @return a set of pairs (p,q) where states p and q are equivalent states are
   *         equivalent if they are in toMerge or if they are matching
   *         successors of equivalent states.
   */
  public Set/* <Pair> */<Pair> computeEquivalence(IDFA dfa, Collection<Object> reachable, Collection toMerge) {

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
            // different states but same equivalence class
            for (Iterator labelIt = dfa.alphabet().iterator(); labelIt.hasNext();) {
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
    Set<Pair> equivPairs = HashSetFactory.make();
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
