/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.safe.dfa.DFASpec;
import com.ibm.safe.dfa.DFAState;
import com.ibm.safe.dfa.DFATransition;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.IDFATransition;
import com.ibm.safe.dfa.events.IDispatchEventImpl;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.dfa.events.IProgramExitEvent;
import com.ibm.safe.dfa.events.IProgramExitEventImpl;

public class TypestateRuleParser {
  public static TypestateRule parseRule(Node node) {

    TypestateRule tr = new TypestateRule();

    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node curr = children.item(i);
      String nodeName = curr.getNodeName();
      if (nodeName.equals("automaton")) {
        tr.setTypeStateAutomaton(parseAutomaton(curr));
      } else if (nodeName.equals("type")) {
        String type = parseType(curr);
        tr.addType(type);
      } else if (nodeName.equals("attributes")) {
        NamedNodeMap attr = curr.getAttributes();
        Node name = attr.getNamedItem("name");
        Node severity = attr.getNamedItem("severity");
        Node level = attr.getNamedItem("level");
        if (name != null) {
          tr.setName(name.getNodeValue());
        }
        if (severity != null) {
          tr.setSeverity(RuleSeverity.getByName(severity.getNodeValue()));
        }
        if (level != null) {
          tr.setLevel(RuleLevel.getByName(level.getNodeValue()));
        }
      }
    }

    if (tr.getTypes().isEmpty()) {
      throw new RuntimeException("No tracked type specified");
    }

    assert (tr.getName() != null);
    
    return tr;

  }

  public static DFASpec parseAutomaton(Node node) {
    //NamedNodeMap attr = node.getAttributes();
    //Node name = attr.getNamedItem("name");
    DFASpec dfa = new DFASpec();

    Set<IDFAState> states = new HashSet<IDFAState>();
    Set<IEvent> events = new HashSet<IEvent>();
    Set<IDFATransition> transitions = new HashSet<IDFATransition>();

    IDFAState initial = null;

    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node curr = children.item(i);
      String nodeName = curr.getNodeName();
      if (nodeName.equals("state")) {
        IDFAState state = parseState(curr);
        if (isInitialState(curr)) {
          initial = state;
        }
        states.add(state);
      } else if (nodeName.equals("transition")) {
        IDFATransition t = parseTransition(curr);
        transitions.add(t);
      } else if (nodeName.equals("event")) {
        IEvent e = parseEvent(curr);
        events.add(e);
      }
    }

    if (initial == null) {
      throw new RuntimeException("no initial state found");
    }

    dfa.setInitialState(initial);
    for (IDFAState s : states) {
      dfa.addState(s);
    }
    for (IEvent e : events) {
      dfa.addEvent(e);
    }
    for (IDFATransition t : transitions) {
      dfa.addTransition(t);
    }
    return dfa;
  }

  private static String parseType(Node node) {
    NamedNodeMap attr = node.getAttributes();
    Node name = attr.getNamedItem("name");
    return name.getNodeValue();
  }

  private static IEvent parseEvent(Node node) {
    NamedNodeMap attr = node.getAttributes();
    Node type = attr.getNamedItem("type");
    Node name = attr.getNamedItem("name");
    List<String> patterns = new ArrayList<String>();
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node curr = children.item(i);
      if (curr.getNodeName().equals("pattern")) {
        NamedNodeMap currAtt = curr.getAttributes();
        Node pat = currAtt.getNamedItem("pattern");
        String val = pat.getNodeValue();
        if (val != null) {
          patterns.add(val);
        }
      }
    }
    if (type != null && type.getNodeValue() != null && type.getNodeValue().equals("IDispatchEvent")) {
      IDispatchEventImpl d = new IDispatchEventImpl();
      d.setName(name.getNodeValue());
      d.setPattern(patterns.get(0)); // for now, only take 1st pattern
      return d;
    } else if (type != null && type.getNodeValue() != null && type.getNodeValue().equals("IProgramExitEvent")) {
      IProgramExitEvent p = new IProgramExitEventImpl();
      return p;
    }

    throw new RuntimeException("Invalid event type: " + name + " type: " + type);
  }

  private static IDFATransition parseTransition(Node node) {
    NamedNodeMap attr = node.getAttributes();
    Node source = attr.getNamedItem("source");
    Node event = attr.getNamedItem("event");
    Node dest = attr.getNamedItem("destination");

    IDFATransition t = new DFATransition();
    t.setSource(source.getNodeValue());
    t.setEvent(event.getNodeValue());
    t.setDestination(dest.getNodeValue());

    return t;
  }

  public static IDFAState parseState(Node node) {
    NamedNodeMap attr = node.getAttributes();
    Node stateName = attr.getNamedItem("name");
    Node accepting = attr.getNamedItem("accepting");
    IDFAState state = new DFAState();
    String name = stateName.getNodeValue();
    if (accepting != null) {
      boolean isAccepting = Boolean.valueOf(accepting.getNodeValue());
      state.setAccepting(isAccepting);
    }
    state.setName(name);
    return state;
  }

  public static boolean isInitialState(Node node) {
    NamedNodeMap attr = node.getAttributes();
    Node initial = attr.getNamedItem("initial");
    return initial != null && Boolean.valueOf(initial.getNodeValue());
  }

}