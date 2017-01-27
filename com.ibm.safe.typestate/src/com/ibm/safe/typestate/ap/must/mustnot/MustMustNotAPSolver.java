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

package com.ibm.safe.typestate.ap.must.mustnot;

import java.util.Collection;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.typestate.ap.must.MustAPSolver;
import com.ibm.safe.typestate.core.BenignOracle;
import com.ibm.safe.typestate.core.TypeStateProblem;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.metrics.TypeStateMetrics;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.quad.QuadTypeStateDomain;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.graph.GraphReachability;

/**
 * 
 * @author sfink
 * @author eyahav
 */
public class MustMustNotAPSolver extends MustAPSolver {

  public MustMustNotAPSolver(AnalysisOptions domoOptions, CallGraph cg, GraphReachability<CGNode,CGNode> reach,
      PointerAnalysis pointerAnalysis, ITypeStateDFA dfa, TypeStateOptions options, ILiveObjectAnalysis live, BenignOracle ora,
      TypeStateMetrics metrics, IReporter reporter, TraceReporter traceReporter, IMergeFunctionFactory mergeFactory) {
    super(domoOptions, cg, reach, pointerAnalysis, dfa, options, live, ora, metrics, reporter, traceReporter, mergeFactory);
  }

  protected TypeStateProblem createTypeStateProblem(ICFGSupergraph supergraph, Collection<InstanceKey> instances)
      throws WalaException, PropertiesException {
    return new TypeStateProblem(supergraph, getDomain(), new MustMustNotAPFunctionProvider(getCallGraph(), getPointerAnalysis(),
        supergraph, (QuadTypeStateDomain) getDomain(), getDFA(), instances, getApsTransformer(), getReach(), getOptions(),
        getLiveObjectAnalysis(), getTraceReporter()), getMergeFactory().create(getDomain()));
  }

}
