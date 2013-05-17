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
package com.ibm.safe.typestate.metrics;

final public class CallGraphMetricsByLoader extends TypeStateMetricsByLoader {

  CallGraphMetricsByLoader(final long theByteCodeStatements, final long theClassesNumber, final long theMethodsNumber,
      final long theCallGraphNodesNumber, final String theClassLoaderName) {
    super(theByteCodeStatements, theClassesNumber, theMethodsNumber, theClassLoaderName);
    this.callGraphNodesNumber = theCallGraphNodesNumber;
  }

  // --- Interface methods implementation

  public long getCallGraph_NodesNumber() {
    return this.callGraphNodesNumber;
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append(super.toString());
    result.append("CGNodes: ");
    result.append(getCallGraph_NodesNumber());
    result.append("\n");
    return result.toString();
  }

  // --- Private code

  private final long callGraphNodesNumber;

}
