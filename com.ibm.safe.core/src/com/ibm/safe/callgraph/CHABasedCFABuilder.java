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
package com.ibm.safe.callgraph;

import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.IClassHierarchy;

public class CHABasedCFABuilder implements CallGraphBuilder {

  private IClassHierarchy cha;
  private IAnalysisCacheView cache;

  public CHABasedCFABuilder(IClassHierarchy cha, AnalysisOptions options, IAnalysisCacheView cache, ContextSelector appContextSelector,
      SSAContextInterpreter appContextInterpreter) {
    this.cha = cha;
    this.cache = cache;
    
    //super(cha, options, cache);
  }

  protected ContextSelector makeContainerContextSelector(ClassHierarchy cha, ZeroXInstanceKeys keys) {
    return new CustomContextSelector(cha, keys);
  }
  
  public CallGraph makeCallGraph(AnalysisOptions options) throws IllegalArgumentException, CallGraphBuilderCancelException {
    return makeCallGraph(options, null);
  }

  public IAnalysisCacheView getAnalysisCache() {
    return cache;
  }

  public PointerAnalysis getPointerAnalysis() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CallGraph makeCallGraph(AnalysisOptions options, com.ibm.wala.util.MonitorUtil.IProgressMonitor monitor)
      throws IllegalArgumentException, CallGraphBuilderCancelException {
    return new CHABasedCallGraph(cha, options, getAnalysisCache());
  }

  @Override
  public IClassHierarchy getClassHierarchy() {
    return this.cha;
  }
}
