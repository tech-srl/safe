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
package com.ibm.safe.typestate.merge;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.ibm.safe.dfa.DFA;
import com.ibm.safe.dfa.IDFA;
import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.io.DFADebug;
import com.ibm.safe.typestate.io.IDFADotWriter;
import com.ibm.safe.typestate.mine.AbstractHistory;
import com.ibm.safe.typestate.mine.UniqueNameStateFactory;
import com.ibm.wala.dataflow.IFDS.IMergeFunction;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.intset.BitVectorIntSet;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableMapping;

/**
 * States are considered equivalent iff they share some outgoing behavior. The
 * current state is always a single sink state. Equivalent states are merged,
 * however matching successors of equivalent states are not merged, which
 * results in nondeterminism.
 * 
 * @author sharonsh
 * @author yahave
 */
public class FutureMerge extends AbstractUnification {

  /**
   * DEBUG LEVEL 0 - no debug 1 - messages only 2 - dump dot files
   */
  private final static int DEBUG_LEVEL = 0;

  /**
   * counter for debugging
   */
  private static int combineCount = 0;

  /**
   * output directory for debugging dot files
   */
  private static String DEBUG_OUTDIR = "C:/temp/master/";

  private FutureMerge(TypeStateDomain domain) {
    super(domain);
    stateFactory = UniqueNameStateFactory.getInstance();
  }

  public int merge(IntSet x, int j) {

    assert j != 0 : "don't merge 0 please";

    int jrep = uf.find(j);
    BaseFactoid f_j = getRepresentativeFactoid(jrep);
    AbstractHistory t_j = (AbstractHistory) f_j.state;

    // sharon: do not merge if the automaton consists of a single state which
    // is both the initial and the current state.
    if (t_j.getCurrentStates().contains(t_j.getDfa().getInitialState())) {
      return getRealRepresentative(jrep);
    }

    // sharon: in order to add a join criterion that merges only
    // histories with matching initial states
    IDFA dfa_j = t_j.getDfa();
    Collection initLabels_j = HashSetFactory.make();
    for (Iterator lIt = dfa_j.getSuccNodes(dfa_j.getInitialState()); lIt.hasNext();) {
      initLabels_j.addAll(dfa_j.getLabels(dfa_j.getInitialState(), lIt.next()));
    }

    // reps := set of factoids to merge.
    BitVectorIntSet reps = new BitVectorIntSet();
    reps.add(jrep);

    for (IntIterator it = x.intIterator(); it.hasNext();) {
      int i = it.next();
      if (i != 0) {
        int r = uf.find(i);
        if (!reps.contains(r)) {
          AbstractHistory t_r = (AbstractHistory) getRepresentativeFactoid(r).state;
          // sharon: do not merge if the automaton consists of a single state
          // which
          // is both the initial and the current one.
          if (t_r.getCurrentStates().contains(t_r.getDfa().getInitialState())) {
            continue;
          }
          // check: are the 2 traces equal, modulo the dfa?
          // TODO: find a way to do this without creating a new index.
          int test = getDomain().getIndexForStateDelta(f_j, t_r);
          if (test == getRealRepresentative(r)) {
            // yes, the traces are equal excluding the dfa
            // we need to merge with r.
            IDFA dfa_r = t_r.getDfa();
            for (Iterator lIt = dfa_r.alphabet().iterator(); lIt.hasNext();) {
              Object label = lIt.next();
              if (dfa_r.successor(dfa_r.getInitialState(), label) != null) {
                if (initLabels_j.contains(label)) {
                  // a shared outgoing transition was found.
                  reps.add(r);
                  continue;
                }
              }
            }
            // reps.add(r);
          }
        }
      }
    }
    return unify(reps, jrep);
  }

  /**
   * @param reps
   *          set of representative factoid numbers to unify
   * @param jrep
   *          number of the representative of the "new" (non-preexisting)
   *          factoid
   * @return the number of the new, unified, factoid.
   * 
   */
  protected int unify(IntSet reps, int jrep) {

    if (reps.size() == 1) {
      // no merge is necessary
      return getRealRepresentative(jrep);
    }

    // TODO: use better names for the states
    Object resultInitState = stateFactory.createState("init");
    IDFA resultDFA = new DFA(resultInitState);
    Object resultCurrState = stateFactory.createState("curr");
    resultDFA.addNode(resultCurrState);

    Collection<Object> resultCurrentStates = HashSetFactory.make();
    resultCurrentStates.add(resultCurrState);

    // construct the automaton
    // for each label introduce a state such that all the transitions with that
    // label will be outgoing transitions of that state.
    // Two different labels can be mapped to the same state only if the state is
    // init.
    Map /* label -> state */<String, Object> label2state = HashMapFactory.make();

    // first find all the labels that should be mapped to the initial state
    for (IntIterator it = reps.intIterator(); it.hasNext();) {
      int i = it.next();
      AbstractHistory t_i = (AbstractHistory) getRepresentativeFactoid(i).state;
      if (DEBUG_LEVEL > 1) {
        String fileName1;
        if (i == jrep) {
          fileName1 = DEBUG_OUTDIR + combineCount + "t_j" + i;
          System.out.println("unify" + combineCount + " t_j: " + t_i.toString());
        } else {
          fileName1 = DEBUG_OUTDIR + combineCount + "t_i" + i;
          System.out.println("unify" + combineCount + " t_i: " + t_i.toString());
        }
        File f1 = IDFADotWriter.instance().writeDFA(fileName1, t_i.getDfa());
        DFADebug.dotifyImages(f1, fileName1 + ".gif");
      }
      mapLabelsOfInitialState(t_i.getDfa(), label2state, resultInitState);
    }

    // now construct the rest of the states and edges of the result DFA
    for (IntIterator it = reps.intIterator(); it.hasNext();) {
      int i = it.next();
      AbstractHistory t_i = (AbstractHistory) getRepresentativeFactoid(i).state;
      updateFlattenedHistory(t_i, resultDFA, resultCurrState, label2state);
    }

    // sharon: important NOT to update the union-find
    // for (IntIterator it = reps.intIterator(); it.hasNext();) {
    // int i = it.next();
    // uf.union(i, jrep);
    // }

    BaseFactoid f_j = getRepresentativeFactoid(jrep);
    AbstractHistory resultAbstractHistory = new AbstractHistory(resultDFA, resultCurrentStates, ((AbstractHistory) f_j.state)
        .getMerger());

    int newJ = getDomain().getIndexForStateDelta(f_j, resultAbstractHistory);
    // sharon: important NOT to update the union-find
    // uf.union(jrep, newJ);
    int newRep = uf.find(newJ);
    rep2Last.set(newRep, newJ);

    if (DEBUG_LEVEL > 1) {
      String fileName2 = DEBUG_OUTDIR + combineCount + "unify";
      System.out.println("unify" + combineCount + " result: " + resultAbstractHistory.toString());
      File f2 = IDFADotWriter.instance().writeDFA(fileName2, resultAbstractHistory.getDfa());
      DFADebug.dotifyImages(f2, fileName2 + ".gif");
      combineCount++;
    }

    return newJ;
  }

  private void mapLabelsOfInitialState(IDFA dfa, Map<String, Object> label2state, Object resultInitState) {
    Object init_i = dfa.getInitialState();
    for (Iterator succIt = dfa.getSuccNodes(init_i); succIt.hasNext();) {
      Object succ = succIt.next();
      Collection<Object> labels = dfa.getLabels(init_i, succ);
      for (Iterator<Object> labelIt = labels.iterator(); labelIt.hasNext();) {
        String label = (String) labelIt.next();
        // we assume that an outgoing transition with a$ exists only if there
        // is also a transition with a. Therefore, a$ can be ignored.
        if (!label.endsWith("$")) {
          label2state.put(label, resultInitState);
        }
      }
    }
  }

  private void updateFlattenedHistory(AbstractHistory history, IDFA resultDFA, Object resultCurrState,
      Map<String, Object> label2state) {

    // Object resultInitState = resultDFA.getInitialState();
    IDFA dfa = history.getDfa();

    Map /* old state -> result state */<Object, Object> stateMap = HashMapFactory.make();

    // first add states and create states map
    for (Iterator stateIt = dfa.iterator(); stateIt.hasNext();) {
      Object state = stateIt.next();
      if (history.getCurrentStates().contains(state)) {
        stateMap.put(state, resultCurrState);
      } else {
        // assume that every other state has at least one outgoing transition.
        // for the initial state, all the lables are mapped to the same result
        // state
        String label = (String) dfa.getLabels(state, dfa.getSuccNodes(state).next()).iterator().next();
        if (label.endsWith("$")) {
          label = label.substring(0, label.indexOf("$"));
        }
        Object newState = label2state.get(label);
        if (newState == null) {
          newState = stateFactory.createState(label);
          resultDFA.addNode(newState);
          label2state.put(label, newState);
        }
        stateMap.put(state, newState);
      }
    }

    // now add the edges
    constructFlattenedDFAEdges(dfa, resultDFA, stateMap);
  }

  // flatten the DFA into a new DFA over a subset of the states
  public AbstractHistory beta(AbstractHistory history) {

    IDFA dfa = history.getDfa();
    Object initState = dfa.getInitialState();
    // assuming that there is exactly one current state in FutureMerge
    Object currState = history.getCurrentStates().iterator().next();

    // sharon: do not merge if the automaton consists of a single state which
    // is both the initial and the current state.
    if (currState == initState) {
      return history;
    }

    IDFA resultDFA = new DFA(initState);
    resultDFA.addNode(currState);
    Collection<Object> resultCurrentStates = HashSetFactory.make();
    resultCurrentStates.add(currState);

    Map /* old state -> result state */<Object, Object> stateMap = HashMapFactory.make();
    stateMap.put(initState, initState);
    stateMap.put(currState, currState);

    // for each label introduce a state such that all the transitions with that
    // label will be outgoing transitions of that state.
    // Two different labels can be mapped to the same state only if the state is
    // init.
    Map /* label -> state */<String, Object> label2state = HashMapFactory.make();

    // initialize map with the labels of the initial state
    mapLabelsOfInitialState(dfa, label2state, initState);
    // add states and update states and labels map
    for (Iterator stateIt = dfa.iterator(); stateIt.hasNext();) {
      Object state = stateIt.next();
      if (state != currState && state != initState) {
        // assume that every other state has at least one outgoing transition.
        // and that all the labels are the same up until the number of '$'
        String label = (String) dfa.getLabels(state, dfa.getSuccNodes(state).next()).iterator().next();
        if (label.endsWith("$")) {
          label = label.substring(0, label.indexOf("$"));
        }
        Object newState = label2state.get(label);
        if (newState == null) {
          newState = state;
          resultDFA.addNode(newState);
          label2state.put(label, newState);
        }
        stateMap.put(state, newState);
      }
    }
    constructFlattenedDFAEdges(dfa, resultDFA, stateMap);
    return new AbstractHistory(resultDFA, resultCurrentStates, history.getMerger());
  }

  private void constructFlattenedDFAEdges(IDFA dfa, IDFA resultDFA, Map<Object, Object> stateMap) {
    for (Iterator stateIt = dfa.iterator(); stateIt.hasNext();) {
      Object state = stateIt.next();
      Object newState = stateMap.get(state);

      assert newState != null : "flattening: all states should be mapped to new states";

      for (Iterator succIt = dfa.getSuccNodes(state); succIt.hasNext();) {
        Object succ = succIt.next();
        Object newSucc = stateMap.get(succ);

        assert newSucc != null : "flattening: all states should be mapped to new states";

        for (Iterator<Object> labelIt = dfa.getLabels(state, succ).iterator(); labelIt.hasNext();) {
          String label = (String) labelIt.next();
          if (label.endsWith("$")) {
            // the same successor should not have multiple labels that
            // differ only in the number of $'s
            label = label.substring(0, label.indexOf("$"));
          }
          // look for the successor or create it
          Object currSucc = null;
          do {
            currSucc = resultDFA.successor(newState, label);
            if (currSucc == null) {
              resultDFA.addLabeledEdge(newState, newSucc, label);
              currSucc = newSucc;
            } else {
              label = label.concat("$");
            }
          } while (currSucc != newSucc);
        }
      }
    }
  }

  public static IMergeFunctionFactory factory() {
    return new IMergeFunctionFactory() {
      public IMergeFunction create(MutableMapping domain) {

        assert (domain instanceof TypeStateDomain);

        return new FutureMerge((TypeStateDomain) domain);
      }
    };
  }
}