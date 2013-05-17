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
 /*
 * $Id: DFASpec.java,v 1.5 2010/10/17 01:20:31 eyahav Exp $
 */
package com.ibm.safe.dfa;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.safe.dfa.events.IEvent;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.graph.NumberedGraph;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;

/**
 * @TODO: this should be re-factored out of existence [EY]
 * @author yahave
 */
public class DFASpec {

  protected Map<String, IDFAState> name2state = HashMapFactory.make();

  protected IDFAState initial;

  protected List<IEvent> events = new ArrayList<IEvent>();

  protected List<IDFAState> states = new ArrayList<IDFAState>();

  protected List<IDFATransition> transitions = new ArrayList<IDFATransition>();

  public DFASpec() {
    super();
  }

  public List<IEvent> getEvents() {
    return events;
  }

  public List<IDFAState> getStates() {
    return states;
  }

  public List<IDFATransition> getTransitions() {
    return transitions;
  }

  public IDFAState initialState() {
    return initial;
  }

  public void setInitialState(IDFAState s) {
    initial = s;
  }

  public void addState(IDFAState s) {
    states.add(s);
    name2state.put(s.getName(), s);
  }

  public void addTransition(IDFATransition t) {
    transitions.add(t);
  }

  public void addEvent(IEvent e) {
    events.add(e);
  }

  private IDFAState getState(final String stateName) {
    final Object mapValue = name2state.get(stateName);
    if (mapValue != null)
      return (IDFAState) mapValue;

    for (Iterator<IDFAState> iter = this.states.iterator(); iter.hasNext();) {
      final IDFAState state = iter.next();
      if (state.getName().equals(stateName)) {
        name2state.put(stateName, state);
        return state;
      }
    }

    assert false : "State '" + stateName + "' couldn't be found among the list !"; //$NON-NLS-1$
    return null;
  }

  public NumberedGraph<Object> asGraph() {
    NumberedGraph<Object> result = SlowSparseNumberedGraph.make();
    for (IDFAState s : states) {
      result.addNode(s);
    }
    for (IDFATransition t : transitions) {
      IDFAState src = getState(t.getSource());
      IDFAState dst = getState(t.getDestination());
      result.addEdge(src, dst);
    }
    return result;
  }

}
