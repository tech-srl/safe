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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.dfa.DFA;
import com.ibm.safe.dfa.DFASpec;
import com.ibm.safe.dfa.DFAState;
import com.ibm.safe.dfa.DFATransition;
import com.ibm.safe.dfa.IDFA;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.IDFATransition;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.dfa.events.IEventImpl;
import com.ibm.safe.utils.SafeAssertions;
import com.ibm.wala.util.collections.HashSetFactory;

/**
 * Utilities for persisting abstract traces.
 * 
 * @author yahave
 * @author sfink
 * 
 */
public class TracePersist {

  public static DFASpec toEMF(IDFA dfa) {
    DFASpec result = new DFASpec();

    IDFAState init = makeStates(dfa, result);
    makeEvents(dfa, result);
    makeTransitions(dfa, result);

    check(dfa, result, init);
    return result;
  }

  @SuppressWarnings("unchecked")
  private static void makeTransitions(IDFA dfa, DFASpec result) {
    for (Iterator it = dfa.iterator(); it.hasNext();) {
      Object xObj = it.next();
      String x = xObj.toString();
      for (Iterator it2 = dfa.getSuccNodes(xObj); it2.hasNext();) {
        Object yObj = it2.next();
        String y = yObj.toString();
        Set<Object> labels = dfa.getLabels(xObj, yObj);
        for (Iterator<Object> labelIt = labels.iterator(); labelIt.hasNext();) {
          Object label = labelIt.next();
          IDFATransition t = new DFATransition();
          t.setDestination(y);
          t.setSource(x);
          t.setEvent(label.toString());
          result.getTransitions().add(t);
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void makeEvents(IDFA dfa, DFASpec result) {
    HashSet<IEvent> allEvents = HashSetFactory.make();
    for (Iterator it = dfa.iterator(); it.hasNext();) {
      Object xObj = it.next();
      for (Iterator it2 = dfa.getSuccNodes(xObj); it2.hasNext();) {
        Object yObj = it2.next();
        Set<Object> labels = dfa.getLabels(xObj, yObj);
        for (Iterator<Object> labelIt = labels.iterator(); labelIt.hasNext();) {
          Object label = labelIt.next();
          IEvent e = makeEvent(label.toString());
          allEvents.add(e);
        }
      }
    }
    for (Iterator<IEvent> it = allEvents.iterator(); it.hasNext();) {
      result.getEvents().add(it.next());
    }

    assert result.getEvents().size() == allEvents.size();

  }

  private static IEvent makeEvent(String label) {
    IEvent e = new IEventImpl();
    e.setName(label);
    return e;
  }

  /**
   * @return initial state
   */
  @SuppressWarnings("unchecked")
  private static IDFAState makeStates(IDFA dfa, DFASpec result) {
    String initial = dfa.getInitialState().toString();
    IDFAState init = makeState(initial);
    result.getStates().add(init);
    for (Iterator it = dfa.iterator(); it.hasNext();) {
      Object node = it.next();
      String nodeName = node.toString();
      if (!nodeName.equals(initial)) {
        IDFAState state = makeState(nodeName);
        result.getStates().add(state);
      }
    }
    return init;
  }

  private static void check(IDFA dfa, DFASpec result, IDFAState init) {
    if (SafeAssertions.verifyAssertions) {
      assert result.initialState().equals(init);
      assert result.getStates().size() == dfa.getNumberOfNodes();
    }
  }

  private static IDFAState makeState(String name) {
    IDFAState state = new DFAState();
    state.setAccepting(false);
    state.setName(name);
    return state;
  }

  @SuppressWarnings("unchecked")
  public static IDFA fromEMF(DFASpec dfa) {
    DFA result = new DFA(dfa.initialState().getName());
    for (Iterator it = dfa.getStates().iterator(); it.hasNext();) {
      IDFAState state = (IDFAState) it.next();
      result.addNode(state.getName());
    }
    for (Iterator<IDFATransition> it = dfa.getTransitions().iterator(); it.hasNext();) {
      IDFATransition t = it.next();
      result.addLabeledEdge(t.getSource(), t.getDestination(), t.getEvent());
    }

    return result;
  }

}
