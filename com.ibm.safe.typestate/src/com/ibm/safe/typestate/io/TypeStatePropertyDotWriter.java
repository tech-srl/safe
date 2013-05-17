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

import java.io.FileWriter;
import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.typestate.core.TypeStateProperty;

/**
 * Writes a typestate proeprty into a DOT file. This is mostly for debugging
 * purposes.
 * 
 * @author Eran Yahav (yahave)
 * 
 */
public class TypeStatePropertyDotWriter {

  public static void write(String fileName, Set<TypeStateProperty> properties) {
    StringBuffer result = new StringBuffer();
    for (Iterator<TypeStateProperty> it = properties.iterator(); it.hasNext();) {
      TypeStateProperty curr = it.next();
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

  public static void writeSingleProperty(String fileName, TypeStateProperty property) {
    StringBuffer dotStringBuffer = dotOutput(property);
    try {
      FileWriter fw = new FileWriter(fileName, false);
      fw.write(dotStringBuffer.toString());
      fw.close();
    } catch (Exception e) {
      throw new RuntimeException("Error writing dot file");
    }
  }

  private static StringBuffer dotOutput(TypeStateProperty property) {
    StringBuffer result = new StringBuffer("digraph \"TypeStateProeprty:" + property.getTypesAsString() + "\" {\n");
    result.append("rankdir=LR;center=true;fontsize=12;node [fontsize=12];edge [fontsize=12]; \n");

    // create title
    result.append("__node [label=\"" + property.getTypesAsString() + "\" shape=\"box\"]\n");

    // create nodes for states
    for (Iterator<IDFAState> it = property.statesIterator(); it.hasNext();) {
      IDFAState state = it.next();
      result.append(dotOutput(state));
      if (state.isAccepting()) {
        result.append(" [color=green]\n");
      } else {
        result.append("\n");
      }

    }
    // mark initial state
    IDFAState initial = property.initial();
    result.append("init [shape=\"plaintext\"]\n");
    result.append("init ->");
    result.append(dotOutput(initial));
    result.append("\n");

    // create edges
    for (Iterator<IDFAState> it = property.statesIterator(); it.hasNext();) {
      IDFAState state = it.next();
      for (Iterator<IEvent> letterIt = property.alphabetIterator(); letterIt.hasNext();) {
        IEvent event = letterIt.next();
        IDFAState target = property.successor(state, event);
        if (target != null) {
          result.append(dotOutput(state));
          result.append(" -> ");
          result.append(dotOutput(target));
          result.append(" [label=\"" + event.getName() + "\"]\n");
        }

      }
    }
    // close digraph
    result.append("}");

    return result;
  }

  private static StringBuffer dotOutput(IDFAState state) {
    StringBuffer result = new StringBuffer();
    result.append("\"");
    result.append(state.getName());
    result.append("\"");
    return result;
  }

}