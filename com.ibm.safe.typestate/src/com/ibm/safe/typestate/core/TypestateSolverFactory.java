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
 * Created on Dec 22, 2004
 */
package com.ibm.safe.typestate.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.ibm.safe.controller.ISafeSolver;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.typestate.ap.must.MustAPSolver;
import com.ibm.safe.typestate.ap.must.MustMerge;
import com.ibm.safe.typestate.ap.must.mustnot.MustMustNotAPSolver;
import com.ibm.safe.typestate.ap.must.mustnot.MustMustNotMerge;
import com.ibm.safe.typestate.base.BaseSolver;
import com.ibm.safe.typestate.base.SeparatingSolver;
import com.ibm.safe.typestate.controller.TypeStateSolverCreator;
import com.ibm.safe.typestate.controller.TypeStateSolverKind;
import com.ibm.safe.typestate.local.LocalMustMustNotSolver;
import com.ibm.safe.typestate.merge.DebugMerge;
import com.ibm.safe.typestate.merge.FutureMerge;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.metrics.TypeStateMetrics;
import com.ibm.safe.typestate.mine.LossLessMerge;
import com.ibm.safe.typestate.mine.StateSimulationMerge;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.mine.UnifyMerge;
import com.ibm.safe.typestate.options.MineMergeKind;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.typestate.staged.StagedSolver;
import com.ibm.safe.typestate.strongUpdate.StrongUpdateSolver;
import com.ibm.safe.typestate.unique.UniqueSolver;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.IndiscriminateFilter;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.GraphReachability;

/**
 * A factory creating typestate solvers.
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 * 
 */
public class TypestateSolverFactory {

  private final static boolean DEBUG_MERGE = false;

  public static ISafeSolver getSolver(AnalysisOptions domoOptions, CallGraph cg, PointerAnalysis pointerAnalysis, HeapGraph hg,
      ITypeStateDFA dfa, BenignOracle ora, TypeStateOptions options, TypeStateMetrics metrics, IReporter reporter,
      PerformanceTracker perfTracker, TraceReporter traceReporter) throws PropertiesException, CancelException {
    return getSolver(options.getTypeStateSolverKind(), domoOptions, cg, pointerAnalysis, hg, dfa, ora, options, metrics, reporter,
        perfTracker, traceReporter);
  }

  public static IMergeFunctionFactory makeMergeFactory(TypeStateOptions options, TypeStateSolverKind kind)
      throws PropertiesException {

    if (options.shouldMineDFA()) {
      MineMergeKind mmk = options.getMineMergeKind();
      if (mmk == MineMergeKind.NONE) {
        return null;
      } else if (mmk == MineMergeKind.TOTAL) {
        return UnifyMerge.factory();
      } else if (mmk == MineMergeKind.SIMULATION) {
        return StateSimulationMerge.factory();
      } else if (mmk == MineMergeKind.LOSSLESS) {
        return LossLessMerge.factory();
      } else if (mmk == MineMergeKind.FUTURE) {
        return FutureMerge.factory();
      } else {
        Assertions.UNREACHABLE();
        return null;
      }
    } else {
      if (DEBUG_MERGE) {
        return DebugMerge.factory();
      } else {
        if (kind == TypeStateSolverKind.BASE) {
          return null;
        } else if (kind == TypeStateSolverKind.SEPARATING) {
          return null;
        } else if (kind == TypeStateSolverKind.LOCAL_MUST_MUSTNOT) {
          return MustMustNotMerge.factory();
        } else if (kind == TypeStateSolverKind.STRONG_UPDATE) {
          return null;
        } else if (kind == TypeStateSolverKind.UNIQUE) {
          return null;
        } else if (kind == TypeStateSolverKind.AP_MUST) {
          return MustMerge.factory();
        } else if (kind == TypeStateSolverKind.AP_MUST_MUSTNOT) {
          return MustMustNotMerge.factory();
        } else if (kind == TypeStateSolverKind.NULL_DEREF) {
          return null;
        } else if (kind == TypeStateSolverKind.STAGED) {
          // note that staged will create each individual solver and merge
          // function later.
          return null;
        } else if (kind == TypeStateSolverKind.TVLA) {
          return null;
        } else if (kind == TypeStateSolverKind.MODULAR) {
          return null;
        } else {
          Assertions.UNREACHABLE("unsupported " + kind);
          return null;
        }
      }
    }
  }

  public static ISafeSolver getSolver(TypeStateSolverKind kind, AnalysisOptions domoOptions, CallGraph cg,
      PointerAnalysis pointerAnalysis, HeapGraph hg, ITypeStateDFA dfa, BenignOracle ora, TypeStateOptions options,
      TypeStateMetrics metrics, IReporter reporter, PerformanceTracker perfTracker, TraceReporter traceReporter)
      throws PropertiesException, CancelException {

    TypeStateProperty property = (dfa instanceof TypeStateProperty) ? (TypeStateProperty) dfa : null;
    IMergeFunctionFactory mergeFactory = makeMergeFactory(options, kind);

    // TODO: this is ugly. fix it.
    if (kind == TypeStateSolverKind.BASE) {
      ILiveObjectAnalysis live = options.shouldUseLiveAnalysis() ? TypeStateSolverCreator.computeLiveObjectAnalysis(cg, hg, false)
          : null;
      return new BaseSolver(cg, pointerAnalysis, dfa, options, live, ora, metrics, reporter, traceReporter, mergeFactory);
    } else if (kind == TypeStateSolverKind.SEPARATING) {
      ILiveObjectAnalysis live = options.shouldUseLiveAnalysis() ? TypeStateSolverCreator.computeLiveObjectAnalysis(cg, hg, false)
          : null;
      return new SeparatingSolver(cg, pointerAnalysis, dfa, options, live, ora, metrics, reporter, traceReporter, mergeFactory);
    } else if (kind == TypeStateSolverKind.LOCAL_MUST_MUSTNOT) {
      ILiveObjectAnalysis live = options.shouldUseLiveAnalysis() ? TypeStateSolverCreator.computeLiveObjectAnalysis(cg, hg, false)
          : null;
      return new LocalMustMustNotSolver(cg, pointerAnalysis, hg, property, options, live, computeReachability(cg), ora, metrics,
          reporter, mergeFactory);
    } else if (kind == TypeStateSolverKind.STRONG_UPDATE) {
      ILiveObjectAnalysis live = options.shouldUseLiveAnalysis() ? TypeStateSolverCreator.computeLiveObjectAnalysis(cg, hg, false)
          : null;
      return new StrongUpdateSolver(cg, pointerAnalysis, dfa, options, live, ora, metrics, reporter, traceReporter, mergeFactory);
    } else if (kind == TypeStateSolverKind.UNIQUE) {
      ILiveObjectAnalysis live = options.shouldUseLiveAnalysis() ? TypeStateSolverCreator.computeLiveObjectAnalysis(cg, hg, false)
          : null;
      return new UniqueSolver(cg, pointerAnalysis, dfa, options, live, ora, metrics, reporter, traceReporter, mergeFactory);
    } else if (kind == TypeStateSolverKind.AP_MUST) {
      ILiveObjectAnalysis live = options.shouldUseLiveAnalysis() ? TypeStateSolverCreator.computeLiveObjectAnalysis(cg, hg, true)
          : null;
      return new MustAPSolver(domoOptions, cg, computeReachability(cg), pointerAnalysis, dfa, options, live, ora, metrics,
          reporter, traceReporter, mergeFactory);
    } else if (kind == TypeStateSolverKind.AP_MUST_MUSTNOT) {
      ILiveObjectAnalysis live = options.shouldUseLiveAnalysis() ? TypeStateSolverCreator.computeLiveObjectAnalysis(cg, hg, true)
          : null;
      return new MustMustNotAPSolver(domoOptions, cg, computeReachability(cg), pointerAnalysis, dfa, options, live, ora, metrics,
          reporter, traceReporter, mergeFactory);
      // } else if (kind == TypeStateSolverKind.NULL_DEREF) {
      // ILiveObjectAnalysis live = options.shouldUseLiveAnalysis() ?
      // TypeStateSolverCreator.computeLiveObjectAnalysis(cg, hg, true)
      // : null;
      // return new NullDerefSolver(cg, pointerAnalysis, property, options,
      // warnings, live);
    } else if (kind == TypeStateSolverKind.STAGED) {
      return new StagedSolver(domoOptions, cg, pointerAnalysis, property, options, ora, metrics, reporter, perfTracker);
    } else if (kind == TypeStateSolverKind.TVLA) {
      ILiveObjectAnalysis live = options.shouldUseLiveAnalysis() ? TypeStateSolverCreator.computeLiveObjectAnalysis(cg, hg, true)
          : null;
      return loadTVLASolver(domoOptions, cg, pointerAnalysis, property, options, live, ora, metrics, reporter);
      // return null;
    } else {
      Assertions.UNREACHABLE("unsupported " + kind);
      return null;
    }
  }

  private static ISafeSolver loadTVLASolver(AnalysisOptions domoOptions, CallGraph cg, PointerAnalysis pointerAnalysis,
      TypeStateProperty property, TypeStateOptions options, ILiveObjectAnalysis live, BenignOracle ora, TypeStateMetrics metrics,
      IReporter reporter) {

    Class klass = null;
    ISafeSolver ret = null;

    String fullClassName = "com.ibm.safe.safet.SafeTSolver";

    try {
      klass = Class.forName(fullClassName);
      Constructor ctr = klass.getConstructors()[0];
      Object params[] = { domoOptions, cg, pointerAnalysis, property, options, live, ora, metrics, reporter };
      ret = (ISafeSolver) ctr.newInstance(params);
    } catch (ClassNotFoundException e) {
      Trace.println(" TVLA Implmentation " + fullClassName + " not found!");
      ret = null;
    } catch (InstantiationException e) {
      Trace.println(" Failed to initialized TVLA Implmentation " + fullClassName);
      ret = null;
    } catch (IllegalAccessException e) {
      Trace.println(" Got an illegal access exception while initializing TVLA Implmentation " + fullClassName);
      ret = null;
    } catch (IllegalArgumentException e) {
      Trace.println(" Got an illegal arguemnt exception while initializing TVLA Implmentation " + fullClassName);
      ret = null;
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      Trace.println(" Got an invocation target exception while initializing TVLA Implmentation " + fullClassName);
      ret = null;
      e.printStackTrace();
    }
    Assertions.productionAssertion(ret != null, "could not create " + fullClassName);
    return ret;
  }

  private static GraphReachability<CGNode,CGNode> computeReachability(CallGraph cg) throws CancelException {
    GraphReachability<CGNode,CGNode> reach = new GraphReachability<CGNode,CGNode>(cg, IndiscriminateFilter.singleton());
    // TODO: Allow real timeouts of CG reachability computations?
    reach.solve(null);
    return reach;
  }
}