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

import com.ibm.safe.utils.Trace;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Pair;

/**
 * Quadratic minimization algorithm for DFAs the algorithm assumes a
 * deterministic automaton, results for non-deterministic automaton will be
 * bogus.
 * 
 * @author yahave
 * 
 */
public class DFAMinimizer extends DFAStateMerger {

  private static final int DEBUG_LEVEL = 0;

  private static DFAMinimizer theInstance;

  public static DFAMinimizer getInstance() {
    if (theInstance == null) {
      theInstance = new DFAMinimizer();
    }
    return theInstance;
  }

  /**
   * singleton
   */
  private DFAMinimizer() {
  }

  public IDFA minimize(IDFA dfa) {
    return mergeStates(dfa);
  }

  /**
   * compute the state-equivalence relation
   * 
   * @param dfa
   * @param reachable
   * @param cut
   *          (initial partition of the states)
   * @return a set of pairs (p,q) where states p and q are equivalent
   */
  @SuppressWarnings("unused")
  public Set<Pair<Object, Object>> computeEquivalence(IDFA dfa, Collection<Object> reachable, Set<Object> cut) {

    Set<Pair<Object, Object>> unmarked = HashSetFactory.make();
    Set<Pair<Object, Object>> marked = HashSetFactory.make();

    // Set accepting = dfa.acceptingStates();

    assert !cut.isEmpty() : "cannot minimize automaton with empty cut";

    // create initial partition:
    // mark (p,q) if p \in F and q \notin F, or the other way around.
    for (Iterator<Object> srcIt = reachable.iterator(); srcIt.hasNext();) {
      Object src = srcIt.next();
      for (Iterator<Object> dstIt = reachable.iterator(); dstIt.hasNext();) {
        Object dst = dstIt.next();
        if (!src.equals(dst)) {
          Pair<Object, Object> pair = Pair.make(src, dst);
          if (cut.contains(src) && !cut.contains(dst) || !cut.contains(src) && cut.contains(dst)) {
            marked.add(pair);
          } else {
            unmarked.add(pair);
          }
        }
      }
    }

    if (DEBUG_LEVEL > 0) {
      Trace.println("marked: " + marked);
      Trace.println("unmarked: " + unmarked);
    }

    boolean changed = false;
    do {
      changed = false;
      for (Iterator<Pair<Object, Object>> it = unmarked.iterator(); it.hasNext();) {
        Pair<Object, Object> pair = it.next();
        for (Iterator<Object> labelIt = dfa.alphabet().iterator(); labelIt.hasNext();) {
          Object label = labelIt.next();
          Object nextSrc = dfa.successor(pair.fst, label);
          Object nextDest = dfa.successor(pair.snd, label);
          if (nextSrc != null && nextDest != null) {
            Pair<Object, Object> nextPair = Pair.make(nextSrc, nextDest);
            if (marked.contains(nextPair)) {
              marked.add(pair);
              changed = true;
            }
          }
        }
      }
      unmarked.removeAll(marked);
    } while (changed);

    if (DEBUG_LEVEL > 0) {
      Trace.println("end marked: " + marked);
      Trace.println("end unmarked: " + unmarked);
      Trace.println("---------------------------------");
    }

    return unmarked;
  }

  public IDFA removeAccepting(IDFA dfa) {
    IDFA res = new DFA(dfa.getInitialState());

    for (Iterator<Object> it = dfa.iterator(); it.hasNext();) {
      Object node = it.next();
      if (!dfa.isAccepting(node)) {
        res.addNode(node);
      }
    }
    for (Iterator<Object> it = dfa.iterator(); it.hasNext();) {
      Object x = it.next();
      if (dfa.isAccepting(x)) {
        continue;
      }
      for (Iterator<? extends Object> s = dfa.getSuccNodes(x); s.hasNext();) {
        Object y = s.next();
        if (dfa.isAccepting(y)) {
          continue;
        }
        res.addLabeledEdges(x, y, dfa.getLabels(x, y));
      }
    }
    return res;
  }

}