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
 * Created on Dec 23, 2004
 */
package com.ibm.safe.typestate.strongUpdate;

import java.util.Collection;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.typestate.base.SeparatingSolver;
import com.ibm.safe.typestate.core.BenignOracle;
import com.ibm.safe.typestate.core.TypeStateProblem;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.metrics.TypeStateMetrics;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.util.WalaException;

/**
 * An extension of the base (actually separating) typestate solver that
 * *unsoundly* always performs strong updates.
 * 
 * @author Stephen Fink
 * @author yahave
 * 
 */
public class StrongUpdateSolver extends SeparatingSolver {

  public StrongUpdateSolver(CallGraph cg, PointerAnalysis pointerAnalysis, ITypeStateDFA dfa, TypeStateOptions options,
      ILiveObjectAnalysis live, BenignOracle ora, TypeStateMetrics metrics, IReporter reporter, TraceReporter traceReporter,
      IMergeFunctionFactory mergeFactory) {
    super(cg, pointerAnalysis, dfa, options, live, ora, metrics, reporter, traceReporter, mergeFactory);
  }

  protected TypeStateProblem createTypeStateProblem(ICFGSupergraph supergraph, Collection<InstanceKey> instances)
      throws WalaException {
    return new StrongUpdateProblem(getCallGraph(), getPointerAnalysis(), supergraph, getDomain(), getDFA(), instances,
        getLiveObjectAnalysis(), getTraceReporter(), getMergeFactory().create(getDomain()));
  }
}