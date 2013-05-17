/*******************************************************************************
 * Copyright (c) 2002-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Dec 6, 2004
 */
package com.ibm.safe.io;

import java.io.FileWriter;
import java.util.Iterator;

import com.ibm.safe.utils.Trace;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.ArrayContentsKey;
import com.ibm.wala.ipa.callgraph.propagation.ConcreteTypeKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder.TypedPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.ReturnValueKey;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ExceptionReturnValueKey;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class DotWriter {

  public static void write(String fileName, PointerAnalysis pai) {
    StringBuffer dotStringBuffer = dotOutput(pai);
    try {
      FileWriter fw = new FileWriter(fileName, false);
      fw.write(dotStringBuffer.toString());
      fw.close();
    } catch (Exception e) {
      throw new RuntimeException("Error writing dot file");
    }
  }

  public static StringBuffer dotOutput(PointerAnalysis pai) {
    StringBuffer result = new StringBuffer("digraph \"PointsTo Graph\" {\n");

    result.append("rankdir=LR;center=true;fontsize=12;node [fontsize=12];edge [fontsize=12]; \n");
    // nodesep=0.1; ranksep=0.1;

    for (Iterator<PointerKey> pkit = pai.getPointerKeys().iterator(); pkit.hasNext();) {
      PointerKey p = pkit.next();
      result.append("   ").append(dotOutput(p, false));
      result.append(" [color=\"green\"] \n");
    }

    for (Iterator<PointerKey> pkit = pai.getPointerKeys().iterator(); pkit.hasNext();) {
      PointerKey p = pkit.next();
      OrdinalSet<InstanceKey> O = pai.getPointsToSet(p);

      if (O.size() != 0) {
        for (Iterator<InstanceKey> it2 = O.iterator(); it2.hasNext();) {
          result.append("     ");
          result.append(dotOutput(p, true));
          result.append(" -> ");
          result.append(dotOutput(it2.next(), true));
          result.append(" \n");
        }
      }
    }
    result.append("\n}");
    return result;
  }

  public static StringBuffer dotOutput(LocalPointerKey lpk, boolean edgeLabel) {
    StringBuffer result = new StringBuffer();
    result.append("\"");
    result.append(dotOutput(lpk.getNode(), edgeLabel));
    result.append(",v ");
    result.append(lpk.getValueNumber());
    result.append("\"");
    return result;
  }

  public static StringBuffer dotOutput(ReturnValueKey rvk, boolean edgeLabel) {
    StringBuffer result = new StringBuffer();
    result.append("\" ");
    result.append(rvk.getNode().getMethod().getName());
    result.append(" \"");
    return result;
  }

  public static StringBuffer dotOutput(TypedPointerKey tpk, boolean edgeLabel) {
    StringBuffer result = new StringBuffer();
    result.append("\"");
    result.append(tpk.getBase().toString().substring(10));
    result.append("T ");
    result.append(tpk.getTypeFilter().toString().substring(10));
    result.append("\"");
    return result;
  }

  public static StringBuffer dotOutput(ConcreteTypeKey ctk, boolean edgeLabel) {
    StringBuffer result = new StringBuffer();
    result.append("\"");
    result.append(ctk.getType());
    result.append("\"");
    return result;
  }

  public static StringBuffer dotOutput(ExceptionReturnValueKey ervk, boolean edgeLabel) {
    StringBuffer result = new StringBuffer();
    result.append("\"");
    result.append("ERV ");
    result.append(ervk.getNode().getMethod().getReference().getName());
    result.append("\"");
    return result;
  }

  public static StringBuffer dotOutput(AllocationSiteInNode nask, boolean edgeLabel) {
    StringBuffer result = new StringBuffer();
    CGNode node = nask.getNode();
    result.append("\"");
    result.append("ST ");
    result.append(node.getMethod().getName());
    result.append(nask.getSite());
    result.append(" in " + node.getContext());
    result.append("\"");
    return result;
  }

  public static StringBuffer dotOutput(ArrayContentsKey aik, boolean edgeLabel) {
    StringBuffer result = new StringBuffer();
    result.append("\"");
    result.append(aik.getClass());
    result.append("\"");
    return result;
  }

  public static StringBuffer dotOutput(InstanceFieldKey ifk, boolean edgeLabel) {
    StringBuffer result = new StringBuffer();
    result.append("\" ");
    result.append(ifk.getField());
    result.append(" \"");
    return result;
  }

  public static StringBuffer dotOutput(CGNode node, boolean edgeLabel) {
    StringBuffer result = new StringBuffer();
    result.append(node.getMethod().getSignature());
    return result;
  }

  public static StringBuffer dotOutput(Object o, boolean edgeLabel) {

    if (o instanceof LocalPointerKey) {
      return dotOutput((LocalPointerKey) o, edgeLabel);
    } else if (o instanceof AllocationSiteInNode) {
      return dotOutput((AllocationSiteInNode) o, edgeLabel);
    } else if (o instanceof PropagationCallGraphBuilder.TypedPointerKey) {
      return dotOutput((PropagationCallGraphBuilder.TypedPointerKey) o, edgeLabel);
    } else if (o instanceof ConcreteTypeKey) {
      return dotOutput((ConcreteTypeKey) o, edgeLabel);
    } else if (o instanceof ExceptionReturnValueKey) {
      return dotOutput((ExceptionReturnValueKey) o, edgeLabel);
    } else if (o instanceof ArrayContentsKey) {
      return dotOutput((ArrayContentsKey) o, edgeLabel);
    } else if (o instanceof InstanceFieldKey) {
      return dotOutput((InstanceFieldKey) o, edgeLabel);
    } else if (o instanceof ReturnValueKey) {
      return dotOutput((ReturnValueKey) o, edgeLabel);
    } else if (o instanceof CGNode) {
      return dotOutput((CGNode) o, edgeLabel);
    }

    Trace.println("Object of class " + o.getClass() + " has no dot representation");
    return new StringBuffer();
  }

}