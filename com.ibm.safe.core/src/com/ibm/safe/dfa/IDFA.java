/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
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

import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.graph.Graph;

/**
 * @author sfink
 * @author eyahav
 * 
 * A deterministic finite automaton (a labelled directed graph)
 */
public interface IDFA extends Graph<Object>, Cloneable {

  /**
   * @param src
   *            source node of an edge
   * @param dest
   *            target node of an edge
   * @return the label for an edge in the graph
   */
  public Set<Object> getLabels(Object src, Object dest);

  /**
   * @param src
   *            source node of an edge
   * @param dest
   *            target node of an edge
   * @param label
   *            the label for an edge in the graph
   */
  public void addLabel(Object src, Object dest, Object label);

  public void removeLabeledEdge(Object src, Object dst, Object label);

  public void removeAllEdges(Object src, Object dst);

  /**
   * Add a node, indicate whether node is accepting or not
   * 
   * @param node
   * @param accepting
   */
  public void addNode(Object node, boolean accepting);

  /**
   * @param src
   *            source node of an edge
   * @param dest
   *            target node of an edge
   * @param label
   *            the label for an edge in the graph
   */
  public void addLabeledEdge(Object src, Object dest, Object label);

  public void addLabeledEdges(Object src, Object dest, Collection<Object> labels);

  /**
   * Remove a node.
   * 
   * @param node
   *            a node of the DFA
   * @throws UnsupportedOperationException
   *             if <code>node</code> is the initial state.
   */
  public void removeNode(Object node) throws UnsupportedOperationException;

  /**
   * @return the "initial" state.
   */
  public Object getInitialState();

  public void setInitialState(Object init);
  
  /**
   * @return set of accepting states
   */
  public Set<Object> acceptingStates();

  /**
   * @return true if given state is accepting
   */
  public boolean isAccepting(Object node);

  /**
   * @return alphabet (Set of labels)
   */
  public Set<Object> alphabet();

  public int getNumberOfLabels();

  /**
   * @param src -
   *            source state
   * @param label -
   *            edge label
   * @return successor state of src with label
   */
  public Object successor(Object src, Object label);

  public Object clone();

  public Iterator<Pair<Object,Object>> iterateEdges();
}
