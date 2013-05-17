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
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.MapUtil;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.graph.impl.SlowNumberedNodeManager;
import com.ibm.wala.util.graph.impl.SlowSparseNumberedGraph;

/**
 * Simple implementation of a DFA
 * 
 * @author sfink
 * @author yahave
 */

public class DFA extends SlowSparseNumberedGraph<Object> implements IDFA {

  /**
   * Map: Pair -> Collection of Objects
   */
  private final Map<Pair<Object, Object>, Set<Object>> edgeLabels = HashMapFactory.make();

  private Object initialState;

  private Set<Object> alphabet = HashSetFactory.make();

  private Set<Object> acceptingStates = HashSetFactory.make();

  public DFA() {
  }

  public DFA(Object initialState) {
    this.initialState = initialState;
    addNode(initialState);
  }

  public void addNode(Object node, boolean accepting) {
    super.addNode(node);
    if (accepting) {
      acceptingStates.add(node);
    }
  }

  /**
   * Removes a node from the DFA.
   * 
   * @param node
   *          a DFA node
   * @throws UnsupportedOperationException
   *           if an attempt is made to remove the initial state of this DFA.
   */
  public void removeNode(Object node) throws UnsupportedOperationException {
    if (node == initialState) {
      throw new UnsupportedOperationException("Cannot remove initial state");
    }
    super.removeNode(node);
    acceptingStates.remove(node);
  }

  public void removeLabeledEdge(Object src, Object dst, Object label) {
    Pair<Object, Object> edge = Pair.make(src, dst);
    Set<Object> labels = MapUtil.findOrCreateSet(edgeLabels, edge);
    labels.remove(label);
    if (labels.isEmpty()) {
      edgeLabels.remove(edge);
      super.removeEdge(src, dst);
    }
  }

  public void removeAllEdges(Object src, Object dst) {
    Pair<Object, Object> edge = Pair.make(src, dst);
    edgeLabels.remove(edge);
    super.removeEdge(src, dst);

  }

  public void addLabeledEdge(Object src, Object dest, Object label) {
    addNode(src);
    addNode(dest);
    addEdge(src, dest);
    addLabel(src, dest, label);
  }

  public void addLabeledEdges(Object src, Object dest, Collection<Object> labels) {
    addNode(src);
    addNode(dest);
    addEdge(src, dest);
    for (Iterator<Object> it = labels.iterator(); it.hasNext();) {
      addLabel(src, dest, it.next());
    }
  }

  public Set<Object> acceptingStates() {
    return acceptingStates;
  }

  public boolean isAccepting(Object node) {
    return acceptingStates.contains(node);
  }

  public Set<Object> getLabels(Object src, Object dest) {
    Pair<Object, Object> p = Pair.make(src, dest);
    Set<Object> result = edgeLabels.get(p);
    return result != null ? result : Collections.emptySet();
  }

  public Set<Object> alphabet() {
    return alphabet;
  }

  public void addLabel(Object src, Object dest, Object label) {
    Pair<Object, Object> p = Pair.make(src, dest);
    Set<Object> labels = MapUtil.findOrCreateSet(edgeLabels, p);
    labels.add(label);
    alphabet.add(label);
  }

  public Object successor(Object src, Object label) {
    for (Iterator<? extends Object> it = getSuccNodes(src); it.hasNext();) {
      Object d = it.next();
      Collection<Object> labels = getLabels(src, d);
      if (labels.contains(label)) {
        return d;
      }
    }
    return null;
  }

  public Iterator<Pair<Object, Object>> iterateEdges() {
    return edgeLabels.keySet().iterator();
  }

  public boolean equals(Object arg0) {
    if (arg0 == null)
      return false;
    if (this == arg0) {
      return true;
    } else {
      if (!getClass().equals(arg0.getClass())) {
        return false;
      }
      DFA other = (DFA) arg0;

      for (Iterator<Object> it = iterator(); it.hasNext();) {
        Object node = it.next();
        if (!other.containsNode(node))
          return false;
        else if (other.isAccepting(node) != isAccepting(node))
          return false;
      }
      for (Iterator<Object> it = iterator(); it.hasNext();) {
        Object x = it.next();
        for (Iterator<? extends Object> s = getSuccNodes(x); s.hasNext();) {
          Object y = s.next();
          if (!other.hasEdge(x, y))
            return false;
          else if (!other.getLabels(x, y).equals(getLabels(x, y)))
            return false;
        }
      }
      return true;
    }
  }

  public int hashCode() {
    int result = 0;
    for (Iterator<Object> it = iterator(); it.hasNext();) {
      Object node = it.next();
      result += 6569 * node.hashCode();
      for (Iterator<? extends Object> s = getSuccNodes(node); s.hasNext();) {
        Object x = s.next();
        result += 6173 * x.hashCode();
        result += 5147 * getLabels(node, x).hashCode();
      }
    }
    return result;
  }

  public Object clone() {
    DFA result = new DFA(initialState);
    for (Iterator<Object> it = iterator(); it.hasNext();) {
      Object node = it.next();
      result.addNode(node, isAccepting(node));
    }
    for (Iterator<Object> it = iterator(); it.hasNext();) {
      Object x = it.next();
      for (Iterator<? extends Object> s = getSuccNodes(x); s.hasNext();) {
        Object y = s.next();
        Set<Object> c = HashSetFactory.make(getLabels(x, y));
        result.addLabeledEdges(x, y, c);
      }
    }
    return result;
  }

  public Object getInitialState() {
    return initialState;
  }

  public int getNumberOfLabels() {
    return edgeLabels.size();
  }

  public String toString() {
    SlowNumberedNodeManager<Object> nodeMgr = (SlowNumberedNodeManager<Object>) getNodeManager();
    StringBuffer buf = new StringBuffer();
    buf.append(super.toString() + "\n");
    buf.append("Initial: " + getInitialState().toString() + "\n");
    buf.append("Labels:\n");
    Iterator<Map.Entry<Pair<Object, Object>, Set<Object>>> edgeLabelsIter = edgeLabels.entrySet().iterator();
    while (edgeLabelsIter.hasNext()) {
      Map.Entry<Pair<Object, Object>, Set<Object>> edge2Label = edgeLabelsIter.next();
      Pair<Object, Object> edge = edge2Label.getKey();
      Object label = edge2Label.getValue();
      buf.append("(" + nodeMgr.getNumber(edge.fst) + ", " + nodeMgr.getNumber(edge.snd) + ") " + label + "\n");
    }
    return buf.toString();
  }

  public void setInitialState(Object init) {
    assert init != null;
    this.initialState = init;
  }

}
