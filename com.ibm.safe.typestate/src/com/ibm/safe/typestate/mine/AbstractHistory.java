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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.safe.dfa.DFA;
import com.ibm.safe.dfa.IDFA;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.IDFAStateFactory;
import com.ibm.safe.typestate.io.DFADebug;
import com.ibm.safe.typestate.io.IDFADotWriter;
import com.ibm.safe.typestate.merge.AbstractUnification;
import com.ibm.safe.typestate.merge.FutureMerge;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Pair;

/**
 * An abstract trace of events (method calls) on an abstract object.
 * 
 * This implementation models an abstract history with a graph. The graph has
 * one state per event.
 * 
 * The AbstractHistory is allowed to have multiple current states.
 * 
 * 
 * TODO: handle the case in which graph may have more than one state per event.
 * For example, when the merging strategy allows for the same event to lead to a
 * number of states.
 * 
 * TODO: this class is a kitchen sink of so many things, it is really terrible
 * in any possible way. Must refactor it. [EY]
 * 
 * @author Sharon Shoham
 * @author sfink
 * @author Eran Yahav
 * 
 */
public class AbstractHistory implements IDFAState, Cloneable {

  private static final int DEBUG_LEVEL = 0;

  /**
   * the initial state for all objects (short name is better for dot-ing later)
   */
  public static final String ABSTRACT_HISTORY_INIT = "AH.init";

  /**
   * should we use one-level of extra context TODO: a ugly hack, should be
   * refactored out of this class and made into a cmd-line option
   */
  private static final boolean EXTRA_CONTEXT = false;

  /**
   * should we only use extra context for a specific event name? If null and
   * EXTRA_CONTEXT is enabled - use extra context for all events If given a
   * specific string value (e.g., "update"), and EXTRA_CONEXT is enabled - will
   * only use extra context for events containing the string as a substring.
   */
  private static final String EXTRA_CONTEXT_EVENT = null;

  /**
   * Automaton. Nodes are Strings (state names). Edge labels are also Strings.
   */
  private IDFA dfa;

  /**
   * The "current" state of the abstract trace (the last selector invoked)
   */
  private Collection<Object> currentStates;

  /**
   * Does this event trace represent a finished program trace?
   */
  private boolean exit = false;

  /**
   * cached for efficiency. be extremely careful not to mutate one of these when
   * it's in a hashed collection.
   */
  private int hashCode;

  private AbstractUnification merger;

  /**
   * shortcut to merger's state factory
   */
  private IDFAStateFactory stateFactory;

  public AbstractHistory(AbstractUnification merger) {
    this.merger = merger;
    if (merger != null) {
      this.stateFactory = merger.getStateFactory();
    } else {
      this.stateFactory = EventNameStateFactory.getInstance();
    }
    Object initState = this.stateFactory.createState(ABSTRACT_HISTORY_INIT);
    initialize(new DFA(initState), Collections.singleton(initState), merger);
  }

  public AbstractHistory(IDFA dfa, Collection<Object> currentStates, AbstractUnification merger) {
    this.merger = merger;
    if (merger != null) {
      this.stateFactory = merger.getStateFactory();
    } else {
      this.stateFactory = EventNameStateFactory.getInstance();
    }
    initialize(dfa, currentStates, merger);
  }

  private void initialize(IDFA dfa, Collection<Object> currentStates, AbstractUnification merger) {
    this.dfa = dfa;
    this.currentStates = HashSetFactory.make();
    this.currentStates.addAll(currentStates);
    updateHashCode();
  }

  public String getName() {
    return "AbstractHistory";
  }

  public boolean isAccepting() {
    // we consider the program exit state "accepting"
    return exit;
  }

  /**
   * "Extend" this abstract trace to account for an observed event.
   * 
   * @param event
   *          an observed event to append to the trace
   */
  public AbstractHistory extend(String event) {

    if (shouldIgnore(event)) {
      return this;
    }

    // sharon: special treatment for FutureMerge
    if (merger != null && merger instanceof FutureMerge) {
      IDFA resultDFA = (DFA) dfa.clone();
      Collection<Object> resultCurrStates = HashSetFactory.make();

      if (DEBUG_LEVEL > 1) {
        String fileName1 = "c:/temp/master/" + debugCounter + "extend_pre";
        System.out.println("extend " + debugCounter + " input: " + this.toString());
        File f1 = IDFADotWriter.instance().writeDFA(fileName1, resultDFA);
        DFADebug.dotifyImages(f1, fileName1 + ".gif");
      }

      assert currentStates.size() == 1 : "extend: expects exactly one current state in future merge";

      Object currObj = currentStates.iterator().next();
      // HistoryState curr = (HistoryState) currObj;

      assert resultDFA.successor(currObj, event) == null : "extend: expects current state to be a sink state in future merge";

      Object nextState = stateFactory.createState(event);
      resultDFA.addNode(nextState);
      resultDFA.addLabeledEdge(currObj, nextState, event);
      resultCurrStates.add(nextState);

      AbstractHistory result = new AbstractHistory(resultDFA, resultCurrStates, merger);
      // apply beta: is this really what we want?
      result = ((FutureMerge) merger).beta(result);

      if (DEBUG_LEVEL > 1) {
        String fileName2 = "c:/temp/master/" + debugCounter + "extend_post";
        System.out.println("extend " + debugCounter + " output: " + result.toString());
        File f2 = IDFADotWriter.instance().writeDFA(fileName2, resultDFA);
        DFADebug.dotifyImages(f2, fileName2 + ".gif");
        debugCounter++;
      }

      return result;
    }

    if (extendWouldBeNoOp(event)) {
      return this;
    } else {
      AbstractHistory result = new AbstractHistory(merger);
      result.dfa = (DFA) dfa.clone();

      result.currentStates.clear();
      for (Iterator<Object> it = currentStates.iterator(); it.hasNext();) {
        Object currObj = it.next();
        HistoryState curr = (HistoryState) currObj;

        Object extended_event = event;

        if (EXTRA_CONTEXT) {
          extended_event = Pair.make(null, event);
          // TODO: need to make label aware of its length, change
          // type from string to something else
          Collection incomingEvents = getIncomingEvents(curr);
          if (EXTRA_CONTEXT_EVENT == null) {
            extended_event = Pair.make(incomingEvents, event);
          } else if (event.toString().contains(EXTRA_CONTEXT_EVENT)) {
            extended_event = Pair.make(incomingEvents, event);
          }
        }

        Object nextState = result.dfa.successor(curr, extended_event);
        if (nextState == null) {
          // sharon: fixed BUG: if successor already exists, do not create a new
          // one
          nextState = stateFactory.createState(extended_event);
          result.dfa.addNode(nextState);
          result.dfa.addLabeledEdge(curr, nextState, extended_event);
        }
        result.currentStates.add(nextState);
      }

      result.updateHashCode();

      if (DEBUG_LEVEL > 1) {
        String fileName4 = "c:/temp/master/" + debugCounter + "extend";
        File f4 = IDFADotWriter.instance().writeDFA(fileName4, result.getDfa());
        DFADebug.dotifyImages(f4, fileName4 + ".gif");
        debugCounter++;
      }

      // TODO: apply beta before returning
      return result;
    }
  }

  private boolean shouldIgnore(String event) {
    return EventFilter.shouldIgnore(event);
  }

  private Collection getIncomingEvents(HistoryState curr) {
    Set result = HashSetFactory.make();
    for (Iterator predIt = dfa.getPredNodes(curr); predIt.hasNext();) {
      Object pred = predIt.next();
      for (Iterator labIt = dfa.getLabels(pred, curr).iterator(); labIt.hasNext();) {
        Pair currLabel = (Pair) labIt.next();
        result.add(currLabel.snd);
      }
    }
    return result;
  }

  private static int debugCounter = 0;

  /**
   * determine when extend should be avoided
   * 
   * @param event
   * @return
   */
  private boolean extendWouldBeNoOp(String event) {
    if (currentStates.size() != 1) {
      // if we have more than one state, we always want to extend,
      // so that after extend, we'll have exactly one state.
      return false;
    }
    Object state = currentStates.iterator().next();
    // System.out.println()
    Object next = dfa.successor(state, event);
    if (next == null) {
      return false;
    }
    return state.equals(next);
  }

  /*
   * TODO: modify identification of current states
   */
  public boolean equals(Object arg0) {
    if (arg0 == null)
      return false;
    if (this == arg0) {
      return true;
    }
    if (!(getClass().equals(arg0.getClass()))) {
      return false;
    }
    AbstractHistory other = (AbstractHistory) arg0;
    if (hashCode != other.hashCode) {
      return false;
    }
    if (stateFactory instanceof UniqueNameStateFactory) {
      return isIsomorphic(other);
    } else {
      return currentStates.equals(other.currentStates) && dfa.equals(other.dfa);
    }
  }

  private static int counter = 0;

  public boolean isIsomorphic(AbstractHistory t_i) {

    if (DEBUG_LEVEL > 1) {
      System.out.println("ISISO CALLS: " + counter++);
    }

    IDFA dfa_j = this.getDfa();
    IDFA dfa_i = t_i.getDfa();
    Collection<Object> currentStates_j = this.getCurrentStates();
    Collection<Object> currentStates_i = t_i.getCurrentStates();

    // can the initial states be current???
    if (currentStates_j.contains(dfa_j.getInitialState()) != currentStates_i.contains(dfa_i.getInitialState())) {
      return false;
    }

    if (!dfa_j.alphabet().equals(dfa_i.alphabet())) {
      return false;
    }

    List<Pair<Object, Object>> workList = new LinkedList<Pair<Object, Object>>();
    Collection<Pair<Object, Object>> explored = HashSetFactory.make();

    Pair<Object, Object> origStatesPair = Pair.make(dfa_j.getInitialState(), dfa_i.getInitialState());
    workList.add(origStatesPair);

    while (!workList.isEmpty()) {
      origStatesPair = workList.remove(0);

      for (Iterator<Object> labelIt = dfa_i.alphabet().iterator(); labelIt.hasNext();) {
        Object label = labelIt.next();
        Object nextDest_j = dfa_j.successor(origStatesPair.fst, label);
        Object nextDest_i = dfa_i.successor(origStatesPair.snd, label);

        if ((nextDest_j == null) != (nextDest_i == null)) {
          return false;
        }

        if (nextDest_j == null && nextDest_i == null) {
          continue;
        }

        if (currentStates_j.contains(nextDest_j) != currentStates_i.contains(nextDest_i)) {
          return false;
        }

        Pair<Object, Object> nextPair = Pair.make(nextDest_j, nextDest_i);
        if (!explored.contains(nextPair)) {
          workList.add(nextPair);
          explored.add(nextPair);
        }

      }

    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  private void updateHashCode() {
    hashCode = dfa.getNumberOfNodes() + 31 * dfa.getNumberOfLabels();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return hashCode;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "AbstractHistory\ncurrent states:" + currentStates + "\nDFA:\n" + dfa.toString();
  }

  /**
   * return a clone of this trace, but marked as having exited.
   */
  public AbstractHistory exit() {
    AbstractHistory result = new AbstractHistory(merger);
    result.dfa = (DFA) dfa.clone();
    result.currentStates = new HashSet<Object>(currentStates);
    result.exit = true;
    return result;
  }

  /**
   * @return Returns the dfa.
   */
  public IDFA getDfa() {
    return dfa;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#clone()
   */
  public Object clone() {
    AbstractHistory result = new AbstractHistory(merger);
    result.dfa = (DFA) dfa.clone();
    result.currentStates = new HashSet<Object>(currentStates);
    result.updateHashCode();
    return result;
  }

  /**
   * Abstract history may have multiple current states
   * 
   * @return collection of current states
   */
  public Collection<Object> getCurrentStates() {
    return Collections.unmodifiableCollection(currentStates);
  }

  public void addCurrentStates(Collection<Object> newStates) {
    currentStates.addAll(newStates);
    updateHashCode();
  }

  /**
   * Merge all state from t into this. TODO: we may want to change the name of
   * this method, it adds "breadth" and does not extend the length of the
   * history.
   */
  public void extend(AbstractHistory t) {
    for (Iterator it = t.dfa.iterator(); it.hasNext();) {
      dfa.addNode(it.next());
    }
    for (Iterator it = t.dfa.iterator(); it.hasNext();) {
      Object x = it.next();
      for (Iterator it2 = t.dfa.getSuccNodes(x); it2.hasNext();) {
        Object y = it2.next();
        Collection<Object> labels = t.dfa.getLabels(x, y);
        dfa.addLabeledEdges(x, y, labels);
      }
    }
    currentStates.addAll(t.currentStates);
    updateHashCode();
  }

  /**
   * take a cartesian product of two abstract histories, and take the union of
   * the current states
   * 
   * @param t_i
   * @return
   */
  public AbstractHistory historyCartesianProduct(AbstractHistory t_i) {

    Map<Pair, Object> statesMap = HashMapFactory.make();
    Set<Object> currentStates = HashSetFactory.make();

    IDFA dfa_j = this.getDfa();
    Collection<Object> currentStates_j = this.getCurrentStates();
    IDFA dfa_i = t_i.getDfa();
    Collection<Object> currentStates_i = t_i.getCurrentStates();

    Pair<Object, Object> origStatesPair = Pair.make(dfa_j.getInitialState(), dfa_i.getInitialState());
    Object newState = stateFactory.createState(origStatesPair.toString());

    statesMap.put(origStatesPair, newState);
    IDFA mergedDFA = new DFA(newState);

    // can the initial states be current???
    if (currentStates_j.contains(dfa_j.getInitialState()) || currentStates_i.contains(dfa_i.getInitialState())) {
      currentStates.add(newState);
    }

    List toExplore = new LinkedList<Pair<Object, Object>>();
    toExplore.add(Pair.make(origStatesPair, newState));

    Object nextState;

    while (!toExplore.isEmpty()) {
      Pair current = (Pair) toExplore.remove(0);
      origStatesPair = (Pair<Object, Object>) current.fst;
      newState = current.snd;

      Collection<Object> unionAlphabet = HashSetFactory.make();
      unionAlphabet.addAll(dfa_i.alphabet());
      unionAlphabet.addAll(dfa_j.alphabet());

      for (Iterator<Object> labelIt = unionAlphabet.iterator(); labelIt.hasNext();) {
        Object label = labelIt.next();
        Object nextDest_j = null;
        Object nextDest_i = null;
        if (origStatesPair.fst != null) {
          nextDest_j = dfa_j.successor(origStatesPair.fst, label);
        }
        if (origStatesPair.snd != null) {
          nextDest_i = dfa_i.successor(origStatesPair.snd, label);
        }
        if (nextDest_j != null || nextDest_i != null) {
          Pair<Object, Object> nextPair = Pair.make(nextDest_j, nextDest_i);

          nextState = statesMap.get(nextPair);
          if (nextState == null) {
            nextState = stateFactory.createState(nextPair.toString());
            mergedDFA.addNode(nextState);
            statesMap.put(nextPair, nextState);
            toExplore.add(Pair.make(nextPair, nextState));
            if (currentStates_j.contains(nextDest_j) || currentStates_i.contains(nextDest_i)) {
              currentStates.add(nextState);
            }
          }
          mergedDFA.addLabeledEdge(newState, nextState, label);
        }
      }
    }
    return new AbstractHistory(mergedDFA, currentStates, merger);
  }

  public AbstractUnification getMerger() {
    return merger;
  }

  public void setAccepting(boolean value) {
    // TODO Auto-generated method stub

  }

  public void setName(String value) {
    // TODO Auto-generated method stub

  }

}