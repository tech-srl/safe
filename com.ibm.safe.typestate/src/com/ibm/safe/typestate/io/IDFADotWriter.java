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
 * Created on Dec 9, 2004
 */
package com.ibm.safe.typestate.io;

import java.io.File;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.safe.dfa.IDFA;
import com.ibm.wala.util.graph.traverse.SlowDFSDiscoverTimeIterator;

/**
 * Writes an IDFA into a DOT file. This is mostly for debugging purposes.
 * 
 * @author Eran Yahav (yahave)
 * 
 */
public class IDFADotWriter {

  private final boolean useAnonymousStates;

  public static IDFADotWriter instance() {
    return new IDFADotWriter(false);
  }

  public IDFADotWriter(boolean anonymousNames) {
    this.useAnonymousStates = anonymousNames;
  }

  public void write(String fileName, Set<IDFA> automata) {
    StringBuffer result = new StringBuffer();
    for (IDFA curr : automata) {
      StringBuffer dotStringBuffer = dotOutput(curr);
      result.append(dotStringBuffer);
    }
    try {
      FileWriter fw = new FileWriter(fileName, false);
      fw.write(result.toString());
      fw.close();
    } catch (Exception e) {
      throw new RuntimeException("Error writing dot file");
    }
  }

  /**
   * @param fileName
   * @param dfa
   * @return a handle to the file used for writing
   */
  public File writeDFA(String fileName, IDFA dfa) {
    StringBuffer dotStringBuffer = new StringBuffer();
    dotStringBuffer.append(openDigraph());
    dotStringBuffer.append(dotOutput(dfa));
    dotStringBuffer.append(closeDigraph());
    return writeDotFile(fileName, dotStringBuffer);
  }

  protected File writeDotFile(String fileName, StringBuffer dotStringBuffer) {
    try {
      File f = new File(fileName);
      FileWriter fw = new FileWriter(f);
      fw.write(dotStringBuffer.toString());
      fw.close();
      return f;
    } catch (Exception e) {
      throw new RuntimeException("Error writing dot file" + e);
    }
  }

  public StringBuffer openDigraph() {
    StringBuffer result = new StringBuffer("digraph \"DFA\" {\n");
    result.append("rankdir=LR;center=true;fontsize=12;node [fontsize=12];edge [fontsize=12]; \n");
    return result;
  }

  public StringBuffer closeDigraph() {
    // close digraph
    return new StringBuffer("}");
  }

  public StringBuffer dotOutput(IDFA dfa) {
    StringBuffer result = new StringBuffer();
    Map<Object, String> nameMap = Collections.emptyMap();
    if (useAnonymousStates) {
      nameMap = initStateNames(dfa);
    }

    // create nodes for states
    for (Iterator<Object> it = dfa.iterator(); it.hasNext();) {
      Object state = it.next();
      result.append(nodeDotOutput(state, nameMap));
      result.append(nodeDotDecoration(dfa, state, nameMap));
    }
    result.append(createInitialState(dfa, nameMap));
    result.append(createEdges(dfa, nameMap));
    return result;
  }

  protected StringBuffer createInitialState(IDFA dfa, Map<Object, String> nameMap) {
    StringBuffer result = new StringBuffer();
    Object initial = dfa.getInitialState();
    result.append("_init_ [label=\"\" shape=\"plaintext\"]\n");
    result.append("_init_ ->");
    result.append(nodeDotOutput(initial, nameMap));
    result.append("[color=\"blue\"]\n");
    return result;
  }

  protected StringBuffer createEdges(IDFA dfa, Map<Object, String> nameMap) {
    StringBuffer result = new StringBuffer();
    for (Iterator<Object> it = dfa.iterator(); it.hasNext();) {
      Object state = it.next();
      for (Iterator<? extends Object> succIt = dfa.getSuccNodes(state); succIt.hasNext();) {
        Object target = succIt.next();
        result.append(nodeDotOutput(state, nameMap));
        result.append(" -> ");
        result.append(nodeDotOutput(target, nameMap));
        String label = dfa.getLabels(state, target).toString();
        String optionalColor = dfa.isAccepting(target) ? " color= \" green \" " : "";
        result.append(" [label=\"" + label + "\"" + optionalColor + "]\n");
      }
    }
    return result;
  }

  protected StringBuffer nodeDotDecoration(IDFA dfa, Object state, Map<Object, String> nameMap) {
    StringBuffer result = new StringBuffer();
    if (dfa.isAccepting(state)) {
      result.append(" [color=green]\n");
    } else {
      result.append("\n");
    }
    return result;
  }

  protected StringBuffer nodeDotOutput(Object state, Map<Object, String> stateNames) {
    StringBuffer result = new StringBuffer();
    result.append("\"");
    if (useAnonymousStates) {
      result.append(stateNames.get(state));
    } else {
      result.append(state.toString());
    }
    result.append("\"");
    return result;
  }

  /**
   * rename states by their DFS order starting from the initial state
   * 
   * @param dfa
   * @return
   */
  protected Map<Object, String> initStateNames(IDFA dfa) {
    Map<Object, String> result = new HashMap<Object, String>();
    int counter = 0;
    SlowDFSDiscoverTimeIterator<Object> dfdi = new SlowDFSDiscoverTimeIterator<Object>(dfa, Collections.singleton(
        dfa.getInitialState()).iterator());
    while (dfdi.hasNext()) {
      Object state = dfdi.next();
      result.put(state, String.valueOf(counter++));
    }
    return result;
  }

}