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

package com.ibm.safe.typestate.ap.must;

import java.util.Collection;

import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.typestate.ap.AccessPathSolver;
import com.ibm.safe.typestate.core.BenignOracle;
import com.ibm.safe.typestate.core.TypeStateDomain;
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
import com.ibm.wala.util.graph.GraphReachability;

/**
 * Base class access path solver which tracks must-alias
 * 
 * @author Stephen Fink
 * @author yahave
 */
public abstract class AbstractMustAPSolver extends AccessPathSolver {

  /**
   * Instantiate a new AccessPathsSafeSolver
   * 
   * @param cg -
   *            underlying callgraph
   * @param pointerAnalysis -
   *            results of pointer-analysis
   * @param dfa -
   *            automaton of typestate property to be verified
   * @param warnings -
   *            collector of produced warnings
   */
  public AbstractMustAPSolver(AnalysisOptions domoOptions, CallGraph cg, GraphReachability<CGNode,CGNode> reach,
      PointerAnalysis pointerAnalysis, ITypeStateDFA dfa, TypeStateOptions options, ILiveObjectAnalysis live, BenignOracle ora,
      TypeStateMetrics metrics, IReporter reporter, TraceReporter traceReporter, IMergeFunctionFactory mergeFactory) {
    super(domoOptions, cg, reach, pointerAnalysis, dfa, options, new MustAPSetTransformers(pointerAnalysis, reach), live, ora,
        metrics, reporter, traceReporter, mergeFactory);
  }

  /**
   * 
   * @param instances -
   *            base instances
   */
  protected void initializeDomain(Collection<InstanceKey> instances) {
    TypeStateDomain d = new QuadTypeStateDomain(getDFA(), getOptions(), getPointerAnalysis());
    setDomain(d);
  }
}
