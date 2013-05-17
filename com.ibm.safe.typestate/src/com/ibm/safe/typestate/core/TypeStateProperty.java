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
package com.ibm.safe.typestate.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.safe.dfa.DFASpec;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.IDFATransition;
import com.ibm.safe.dfa.events.IDispatchEvent;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.dfa.events.IEventImpl;
import com.ibm.safe.dfa.events.IObjectDeathEvent;
import com.ibm.safe.dfa.events.IProgramExitEvent;
import com.ibm.safe.dfa.events.IReadFieldEventImpl;
import com.ibm.safe.internal.exceptions.AssertionFailedException;
import com.ibm.safe.rules.TypestateRule;
import com.ibm.safe.typestate.rules.AbstractTypeStateDFA;
import com.ibm.safe.utils.SafeAssertions;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.collections.HashMapFactory;

/**
 * Gives access to some information coming from an ITypeStateRule. Mainly, its
 * finite-state automaton and few others general information. Provides services
 * to walk through automaton graph.
 * 
 * @author Eran Yahav (yahave), egeay, sfink
 */
public class TypeStateProperty extends AbstractTypeStateDFA {

  /**
   * map: String (Event name) -> IEvent
   */
  Map<String, IEvent> eventName2Event = HashMapFactory.make();

  /**
   * map: String (State name) -> IState
   */
  Map<String, IDFAState> stateName2State = HashMapFactory.make();

  /**
   * event lookup by kind map: String (Class.getName()) -> List of IEvent
   */
  Map<Class, List<IEvent>> class2events = HashMapFactory.make();

  /**
   * A set of automaton accepting states (for a typestate property, we assume
   * for simplicity that the err state is the accepting one). Set<IState>
   */
  private Set<IDFAState> acceptingStates = new HashSet<IDFAState>(3);

  /**
   * A Map from automaton-state to its successor automaton state since this is a
   * determinstic automaton, there's only one successor for each state and edge
   * label. Map: IState -> IEvent -> IState Map<IState, Map<IEvent,IState>>
   */
  protected Map<IDFAState, Map<IEvent, IDFAState>> transitionMapping = new HashMap<IDFAState, Map<IEvent, IDFAState>>(20);

  /**
   * A Map from automaton-state to its predecessor automaton states Map: IState ->
   * IEvent -> Set of IState Map<IState,Map<IEvent,Set<IState>>>
   */
  private Map<IDFAState, Map<IEvent, Set<IDFAState>>> reverseTransitionMapping = new HashMap<IDFAState, Map<IEvent, Set<IDFAState>>>(
      20);

  /**
   * The ITypeStateRule instance that this class encapsulates and provides
   * services around it.
   */
  protected TypestateRule typeStateRule;

  /**
   * Initializes all fields of the class, except \a type which should be
   * initialized via a call to setType().
   * 
   * @param aTypeStateRule
   *            The ITypeStateRule instance that this class is encapsulating.
   */
  public TypeStateProperty(final TypestateRule aTypeStateRule, final IClassHierarchy cha) {
    super(cha, new HashSet<IClass>(1));
    this.typeStateRule = aTypeStateRule;
    this.typeStateRule.getTypeStateAutomaton().getEvents().add(IEventImpl.GENERIC_EVENT);
    mapEventNames2Events();
    mapStateNames2States();
    mapEventClass2Events();
    initAcceptingStates(this.typeStateRule.getTypeStateAutomaton());
    initTransitionMappings(this.typeStateRule.getTypeStateAutomaton());
  }

  protected TypeStateProperty(final IClassHierarchy cha) {
    super(cha, new HashSet<IClass>(1));
  }

  private void mapEventClass2Events() {
    for (Iterator<IEvent> it = alphabetIterator(); it.hasNext();) {
      IEvent e = it.next();
      Class eventClass = e.getClass();
      // treat all IDispathEvents the same way, required for multiple objects
      if (e instanceof IDispatchEvent) {
        eventClass = IDispatchEvent.class;
      }
      List<IEvent> classEntry = class2events.get(eventClass);
      if (classEntry == null) {
        classEntry = new ArrayList<IEvent>();
        class2events.put(eventClass, classEntry);
      }
      classEntry.add(e);
    }
  }

  /**
   * Set up the mapping of event names to IEvents and program-exit event (if
   * exists)
   */
  private void mapEventNames2Events() {
    for (Iterator<IEvent> it = alphabetIterator(); it.hasNext();) {
      IEvent e = it.next();
      eventName2Event.put(e.getName(), e);
      if (e instanceof IProgramExitEvent) {
        setObservesProgramExit(true);
      }
      if (e instanceof IObjectDeathEvent) {
        setObservesObjectDeath(true);
      }
    }
  }

  /**
   * Set up the mapping of state names to IStates
   */
  private void mapStateNames2States() {
    for (Iterator<IDFAState> it = statesIterator(); it.hasNext();) {
      IDFAState s = it.next();
      stateName2State.put(s.getName(), s);
    }
  }

  /**
   * Returns an iterator on all elements of DFA alphabet (here IEdgeLabel).
   */
  public Iterator<IEvent> alphabetIterator() {
    return this.typeStateRule.getTypeStateAutomaton().getEvents().iterator();
  }

  /**
   * Returns the set of accepting automaton states from the given DFA (Set<AutomatonState>).
   */
  public Set<IDFAState> getAcceptingStates() {
    return this.acceptingStates;
  }

  /**
   * Returns the number of elements in the DFA alphabet.
   */
  public int getAlphabetSize() {
    return this.typeStateRule.getTypeStateAutomaton().getEvents().size();
  }

  /**
   * Gets the event corresponding to a particular event name
   * 
   * @return The IEvent corresponding to this event name, or null if none found
   */
  public IEvent getEvent(final String eventName) {
    return eventName2Event.get(eventName);
  }

  /**
   * Returns the rule encapsulated.
   */
  public TypestateRule getRule() {
    return this.typeStateRule;
  }

  /**
   * Returns the number of states in the DFA.
   */
  public int getNumberOfStates() {
    return this.typeStateRule.getTypeStateAutomaton().getStates().size();
  }

  /**
   * Gets the automaton state from a given state name.
   * 
   * @param name
   *            State name.
   * @return An IState instance in case state exists, null otherwise. note: We
   *         could have stored names in a map, but we don't want to force
   *         automaton state name to be unique.
   */
  public IDFAState getState(final String name) {
    return stateName2State.get(name);
  }

  /**
   * Gets access to automaton initial state.
   */
  public IDFAState initial() {
    return this.typeStateRule.getTypeStateAutomaton().initialState();
  }

  /**
   * Returns a set of predecessors for a given state and event. Method takes
   * into account possible default-event edges, i.e., if an incoming edge with
   * the given event does not exist, and the given event is part of the
   * automaton's alphabet, and a default event exists, then this default event
   * is followed.
   * 
   * @param state
   *            Source state
   * @param event
   *            An event
   * @return A set of target states reached by following label backwards from
   *         source state. When an explicit edge with the given event name does
   *         not exist, a default event name may be followed if such default
   *         name exists.
   */
  public Set<IDFAState> predecessors(final IDFAState state, final IEvent event) {
    final Map<IEvent, Set<IDFAState>> stateMap = this.reverseTransitionMapping.get(state);
    if (stateMap == null) {
      return Collections.emptySet();
    }
    Set<IDFAState> predSet = stateMap.get(event);
    if (predSet == null) {
      predSet = stateMap.get(IEventImpl.GENERIC_EVENT);
      if (predSet == null) {
        predSet = Collections.emptySet();
      }
    }

    return predSet;
  }

  /**
   * Returns an iterator on all states of DFA.
   */
  public Iterator<IDFAState> statesIterator() {
    return this.typeStateRule.getTypeStateAutomaton().getStates().iterator();
  }

  /**
   * Returns a single successor for a given state and event. Method takes into
   * account possible default-event names, i.e., if an outgoing edge with the
   * given event does not exist, and the given event is part of the automaton's
   * alphabet, and a default event name exists, then this default event name is
   * followed.
   * 
   * @param state
   *            Source state
   * @param eventName
   *            Event name
   * @return The target state reached by following event from source state. When
   *         an explicit event with the given event does not exist, a default
   *         event name may be followed if such default event name exists.
   */
  public IDFAState successor(final IDFAState state, final IEvent eventName) {
    final Map<IEvent, IDFAState> stateMap = this.transitionMapping.get(state);

    if (SafeAssertions.verifyAssertions) {
      assert stateMap != null : "no successor found for " + state + " with " + eventName;
    }

    IDFAState result = stateMap.get(eventName);
    result = (result == null) ? stateMap.get(IEventImpl.GENERIC_EVENT) : result;

    if (SafeAssertions.verifyAssertions) {
      assert result != null : "No successor found for " + state + " " + eventName;
    }

    return result;
  }

  /*
   * return a string concatanting all types that re used in this propoerty use
   * for printing only.
   */
  public StringBuffer getTypesAsString() {
    StringBuffer result = new StringBuffer();
    for (Iterator<String> it = ((TypestateRule) getRule()).getTypes().iterator(); it.hasNext();) {
      String typeName = it.next();
      result.append(" ; " + typeName);
    }
    return result;
  }

  /**
   * validate typestate property. Check: 1. there exists an initial state 2.
   * every state has a defined single successor per event 3. every non-initial
   * state, has a defined set of predecessors per event
   */
  public void validate() {
    boolean illegal = false;
    StringBuffer errorString = new StringBuffer();
    for (Iterator<IDFAState> stateIt = this.statesIterator(); stateIt.hasNext();) {
      IDFAState curr = stateIt.next();
      for (Iterator<IEvent> eventIt = this.alphabetIterator(); eventIt.hasNext();) {
        IEvent currEvent = eventIt.next();
        if (currEvent.equals(IEventImpl.GENERIC_EVENT)) {
          // ignore default event
          continue;
        }
        IDFAState succ = this.successor(curr, currEvent);
        if (succ == null) {
          illegal = true;
          errorString.append("cannot find successor for state " + curr + " and event " + currEvent + "\n");
        }
        if (!this.initial().equals(curr)) {
          Set<IDFAState> pred = this.predecessors(curr, currEvent);
          if (pred == null) {
            illegal = true;
            errorString.append("cannot find predecessors for state " + curr + " and event " + currEvent + "\n");
          }
        }
      }
    }

    if (illegal) {
      throw new AssertionFailedException("Illegal typestate property: \n" + errorString.toString());
    }
  }

  /**
   * check whether event only transitions into an accepting state, that is ---
   * for all targets of the event, they are accepting states.
   */
  boolean eventTransitionsOnlyToAccept(IEvent e) {
    final DFASpec idfa = ((TypestateRule) getRule()).getTypeStateAutomaton();
    for (Iterator<IDFATransition> it = idfa.getTransitions().iterator(); it.hasNext();) {
      IDFATransition t = it.next();
      if (t.getEvent().equals(e.getName())) {
        if (!t.getSource().equals(t.getDestination())) {
          IDFAState dest = getState(t.getDestination());
          if (!dest.isAccepting()) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /**
   * check whether some targets of this event are accepting, that is --- there
   * exists a target of the event such that it is an accepting state.
   */
  protected boolean eventTransitionsToAccept(IEvent e) {
    final DFASpec idfa = ((TypestateRule) getRule()).getTypeStateAutomaton();
    for (Iterator<IDFATransition> it = idfa.getTransitions().iterator(); it.hasNext();) {
      IDFATransition t = it.next();
      if (t.getEvent().equals(e.getName())) {
        if (!t.getSource().equals(t.getDestination())) {
          IDFAState dest = getState(t.getDestination());
          if (dest.isAccepting()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  // --- Overridden methods

  public String toString() {
    return this.typeStateRule.getName();
  }

  // --- Private code

  private void addTransition(final IDFATransition transition) {
    final IDFAState src = getState(transition.getSource());
    final IEvent event = getEvent(transition.getEvent());
    final IDFAState dest = getState(transition.getDestination());
    if (src == null) {
      throw new AssertionFailedException("No source state " + transition.getSource() + " for the rule "
          + this.typeStateRule.getName());
    }
    if (event == null) {
      throw new AssertionFailedException("No event " + transition.getEvent() + " for the rule " + this.typeStateRule.getName());
    }
    if (dest == null) {
      throw new AssertionFailedException("No destination state " + transition.getDestination() + " for the rule "
          + this.typeStateRule.getName());
    }

    Map<IEvent, IDFAState> outedges = this.transitionMapping.get(src);
    if (outedges == null) {
      outedges = new HashMap<IEvent, IDFAState>(10);
      this.transitionMapping.put(src, outedges);
    }
    outedges.put(event, dest);

    Map<IEvent, Set<IDFAState>> reverseEdges = this.reverseTransitionMapping.get(dest);
    if (reverseEdges == null) {
      reverseEdges = new HashMap<IEvent, Set<IDFAState>>(10);
      this.reverseTransitionMapping.put(dest, reverseEdges);
    }

    Set<IDFAState> preds = reverseEdges.get(event);
    if (preds == null) {
      preds = new HashSet<IDFAState>(10);
      reverseEdges.put(event, preds);
    }
    preds.add(src);
  }

  private void initAcceptingStates(final DFASpec automaton) {
    for (Iterator<IDFAState> iter = automaton.getStates().iterator(); iter.hasNext();) {
      final IDFAState state = iter.next();
      if (state.isAccepting()) {
        this.acceptingStates.add(state);
      }
    }
  }

  private void initTransitionMappings(final DFASpec automaton) {
    for (Iterator<IDFATransition> iter = automaton.getTransitions().iterator(); iter.hasNext();) {
      addTransition(iter.next());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.ITypeStateDFA#getName()
   */
  public String getName() {
    return getRule().getName();
  }

  public IEvent match(final Class eventClass, final String param) {
    List<IEvent> eventList = class2events.get(eventClass);
    for (Iterator<IEvent> it = eventList.iterator(); it.hasNext();) {
      IEvent curr = it.next();
      if (curr.match(param)) {
        return curr;
      }
    }
    return null;
  }

  public IEvent matchReadFieldEvent(final String fld) {
    return match(IReadFieldEventImpl.class, fld);
  }

  public IEvent matchWriteFieldEvent(final String fld) {
    return match(IReadFieldEventImpl.class, fld);
  }
}
