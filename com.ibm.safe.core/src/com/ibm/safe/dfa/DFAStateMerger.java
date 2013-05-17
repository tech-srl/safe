/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.IndiscriminateFilter;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.graph.traverse.DFS;

/**
 * @author yahave
 */
public abstract class DFAStateMerger implements IDFAStateMerger {

  private final static String ERR_STATE = "err";

  /**
   * compute the state-equivalence relation
   * 
   * @param dfa
   * @param reachable
   * @param cut
   *            (initial partition of the states)
   * @return a set of pairs (p,q) where states p and q are equivalent
   */
  public abstract Set<Pair<Object, Object>> computeEquivalence(IDFA dfa, Collection<Object> reachable, Set<Object> cut);

  /**
   * merge states of DFA
   * 
   * @param dfa
   * @return
   */
  protected IDFA mergeStates(IDFA dfa) {
    return mergeStates(dfa, dfa.acceptingStates());
  }

  /**
   * merge states in a given DFA according to equivalence function and an
   * initial partition
   * 
   * @param dfa
   * @param cut -
   *            initial partition of states
   * @return a minimized DFA
   */

  public IDFA mergeStates(IDFA dfa, Set<Object> cut) {
    return mergeStates(dfa, cut, IdentityStateFactory.getInstance());
  }

  protected IDFA mergeStates(IDFA dfa, Set<Object> cut, IDFAStateFactory stateFactory) {

    Map<Object, Set<Object>> partitionMap = HashMapFactory.make();
    Set<Set<Object>> partition = HashSetFactory.make();
    Set<Set<Object>> accepting = HashSetFactory.make();

    Collection<Object> reachable = DFS.getReachableNodes(dfa, Collections.singleton(dfa.getInitialState()), IndiscriminateFilter
        .singleton());

    Set<Pair<Object, Object>> equivalence = computeEquivalence(dfa, reachable, cut);

    Set<Object> ignoreSet = HashSetFactory.make();

    for (Iterator<Object> srcIt = reachable.iterator(); srcIt.hasNext();) {
      Object src = srcIt.next();
      if (ignoreSet.contains(src)) {
        continue;
      }
      Set<Object> nodeSet = HashSetFactory.make();
      nodeSet.add(src);
      partitionMap.put(src, nodeSet);
      // sharon: use acceptFlag in order to avoid adding a still
      // changing set to the HashSet accepting
      boolean acceptFlag = false;
      if (dfa.isAccepting(src)) {
        // accepting.add(nodeSet);
        acceptFlag = true;
      }
      for (Iterator<Object> destIt = reachable.iterator(); destIt.hasNext();) {
        Object dest = destIt.next();
        if (equivalence.contains(Pair.make(src, dest))) {
          // TODO: avoid duplicates here! (BUG)
          nodeSet.add(dest);
          ignoreSet.add(dest);
          partitionMap.put(dest, nodeSet);
          if (dfa.isAccepting(dest)) {
            // accepting.add(nodeSet);
            acceptFlag = true;
          }
        }
      }
      partition.add(nodeSet);
      if (acceptFlag) {
        accepting.add(nodeSet);
      }
    }

    return constructNewDfA(dfa, partitionMap, partition, accepting, stateFactory);
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
   * @param accepting -
   *            a set of accepting equilvanece classes (sets of states)
   * @return the new minimized automaton over equivalence classes, instead of
   *         over states.
   */
  protected IDFA constructNewDfA(IDFA dfa, Map<Object, Set<Object>> partitionMap, Set<Set<Object>> partition,
      Set<Set<Object>> accepting, IDFAStateFactory stateFactory) {

    // map from stateClass to new automaton states
    Map<Object, Object> classes2states = HashMapFactory.make();

    // create new DFA
    Object initialClass = partitionMap.get(dfa.getInitialState());
    Object newInitState = stateFactory.createState(initialClass);
    classes2states.put(initialClass, newInitState);

    IDFA result = new DFA(newInitState);

    // first add all states
    for (Iterator<Set<Object>> partitionIt = partition.iterator(); partitionIt.hasNext();) {
      Set<Object> stateClass = partitionIt.next();
      if (!(stateClass == initialClass)) {
        Object newState = stateFactory.createState(stateClass);
        result.addNode(newState, accepting.contains(stateClass));
        classes2states.put(stateClass, newState);
      }
    }

    // then add edges
    for (Iterator<Set<Object>> partitionIt = partition.iterator(); partitionIt.hasNext();) {
      Set<Object> stateClass = partitionIt.next();
      Object newState = classes2states.get(stateClass);
      for (Iterator<Object> stateIt = stateClass.iterator(); stateIt.hasNext();) {
        Object state = stateIt.next();
        for (Iterator<Object> labelIt = dfa.alphabet().iterator(); labelIt.hasNext();) {
          Object label = labelIt.next();
          Object successor = dfa.successor(state, label);
          if (successor != null) {
            Object succStateClass = partitionMap.get(successor);
            Object succNewState = classes2states.get(succStateClass);
            result.addLabeledEdge(newState, succNewState, label);
          }
        }
      }
    }
    return result;
  }

  /**
   * updates DFA to contain accepting state and have add missing transitions to
   * the accepting state.
   * 
   * @param dfa
   */
  public IDFA completeDFA(IDFA dfa) {
    IDFA result = (IDFA) dfa.clone();

    if (result.acceptingStates().isEmpty()) {
      result.addNode(ERR_STATE, true);
    }
    for (Iterator<Object> nodeIt = result.iterator(); nodeIt.hasNext();) {
      Object src = nodeIt.next();
      for (Iterator<Object> labelIt = result.alphabet().iterator(); labelIt.hasNext();) {
        Object label = labelIt.next();
        if (result.successor(src, label) == null) {
          result.addLabeledEdge(src, ERR_STATE, label);
        }
      }
    }
    return result;
  }

  /**
   * Completes a DFA with a single "accepting" error state. This takes a DFA
   * with no explicit error (i.e., a DFA that implicitly rejects all words that
   * it does not identify) and modifies it to a DFA with an explicit error state
   * and transitions to the error state. Note that completion is done in-place.
   * 
   * @param dfa
   */
  public void inPlaceCompleteDFA(IDFA dfa) {
    if (dfa.acceptingStates().isEmpty()) {
      dfa.addNode(ERR_STATE, true);
    }
    for (Iterator<Object> nodeIt = dfa.iterator(); nodeIt.hasNext();) {
      Object src = nodeIt.next();
      for (Iterator<Object> labelIt = dfa.alphabet().iterator(); labelIt.hasNext();) {
        Object label = labelIt.next();
        if (dfa.successor(src, label) == null) {
          dfa.addLabeledEdge(src, ERR_STATE, label);
        }
      }
    }
  }

}
