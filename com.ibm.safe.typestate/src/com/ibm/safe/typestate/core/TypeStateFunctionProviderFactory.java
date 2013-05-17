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

import com.ibm.safe.typestate.rules.InterproceduralCFG;
import com.ibm.wala.dataflow.IFDS.ISupergraph;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchy;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public interface TypeStateFunctionProviderFactory {
  public TypeStateFunctionProvider getFunctionProvider(ClassHierarchy classHierarchy, CallGraph callGraph,
      PointerAnalysis pointerAnalysis, ISupergraph supergraph, InterproceduralCFG uncollapsed, TypeStateDomain domain,
      TypeStateProperty property);
}