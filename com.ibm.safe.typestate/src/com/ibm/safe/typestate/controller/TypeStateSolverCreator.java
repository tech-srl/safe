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
package com.ibm.safe.typestate.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.callgraph.CallGraphEngine;
import com.ibm.safe.controller.ISafeSolver;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.io.DotWriter;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.rules.TypestateRule;
import com.ibm.safe.typestate.core.BenignOracle;
import com.ibm.safe.typestate.core.TypeStateProperty;
import com.ibm.safe.typestate.core.TypestateSolverFactory;
import com.ibm.safe.typestate.io.TypeStatePropertyDotWriter;
import com.ibm.safe.typestate.metrics.TypeStateMetrics;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.mine.TracingProperty;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.escape.FILiveObjectAnalysis;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.strings.StringStuff;

/**
 * A factory to TypeState solvers and properties
 */
public class TypeStateSolverCreator {

  protected final CallGraphEngine callGraphEngine;

  protected final TypeStateOptions options;

  protected final TypeStateMetrics metrics;

  protected final IReporter reporter;

  protected final PerformanceTracker perfTracker;

  protected final TraceReporter traceReporter;

  /**
   * @param domoCallGraphBuilder
   * @param typeStateOptions
   * @throws CancelException
   * @throws IllegalArgumentException
   * @throws SetUpException
   */
  public TypeStateSolverCreator(final CallGraphEngine domoCallGraphBuilder, final TypeStateOptions typeStateOptions,
      final IReporter reporter, final PerformanceTracker perfTracker, TraceReporter traceReporter) throws SafeException,
      IllegalArgumentException, CancelException {
    this.callGraphEngine = domoCallGraphBuilder;
    this.options = typeStateOptions;
    this.reporter = reporter;
    this.perfTracker = perfTracker;
    this.traceReporter = traceReporter;

    computeCallGraph(typeStateOptions.shouldDumpCallGraph());

    if (typeStateOptions.shouldCreatePointsToDotFile()) {
      DotWriter.write(typeStateOptions.getPointsToDotFile(), this.callGraphEngine.getPointerAnalysis());
    }

    metrics = typeStateOptions.shouldCollectStatistics() ? new TypeStateMetrics(this.callGraphEngine.getCallGraph()
        .getClassHierarchy(), this.callGraphEngine.getCallGraph()) : null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.solvers.ISolverCreator#createSolvers()
   */
  public ISafeSolver[] createSolvers() throws SafeException, CancelException {

    HeapGraph hg = callGraphEngine.getPointerAnalysis().getHeapGraph();
    // ILiveObjectAnalysis live = options.shouldUseLiveAnalysis() ?
    // computeLiveObjectAnalysis(domoCallGraph.getCallGraph(), hg) : null;
    Collection<ISafeSolver> solvers = null;
    if (options.shouldMineDFA()) {
      Assertions.productionAssertion(options.getRules().length == 0, "don't specify rules when mining");
      solvers = createMiners(hg, traceReporter);
    } else {
      final TypestateRule[] rules = this.options.getRules();
      solvers = new ArrayList<ISafeSolver>(rules.length);
      final Set<TypeStateProperty> typeStatePropertySet = new HashSet<TypeStateProperty>(rules.length);
      createVerifiers(rules, solvers, typeStatePropertySet, hg);
      if (this.options.shouldCreatePropertyDotFile()) {
        TypeStatePropertyDotWriter.write(this.options.getPropertyDotFile(), typeStatePropertySet);
      }
    }

    return solvers.toArray(new ISafeSolver[solvers.size()]);
  }

  private Collection<ISafeSolver> createMiners(HeapGraph hg, TraceReporter traceReporter) throws PropertiesException, CancelException {
    Set<ISafeSolver> result = HashSetFactory.make();
    String type = options.getMineType();
    Assertions.productionAssertion(type != null, "mine_type cannot be null!");
    type = StringStuff.deployment2CanonicalTypeString(type);
    TypeReference t = TypeReference.findOrCreate(ClassLoaderReference.Application, type);
    IClass klass = callGraphEngine.getCallGraph().getClassHierarchy().lookupClass(t);
    Assertions.productionAssertion(klass != null, "Failed to find class " + t);
    final ITypeStateDFA dfa = createTypeStateTracer(klass);
    BenignOracle ora = new BenignOracle(callGraphEngine.getCallGraph(), callGraphEngine.getPointerAnalysis());
    result.add(TypestateSolverFactory.getSolver(callGraphEngine.getAnalysisOptions(), callGraphEngine.getCallGraph(),
        callGraphEngine.getPointerAnalysis(), hg, dfa, ora, options, metrics, reporter, perfTracker, traceReporter));

    return result;
  }

  protected void createVerifiers(final TypestateRule[] rules, final Collection<ISafeSolver> solvers,
      final Set<TypeStateProperty> typeStatePropertySet, HeapGraph hg) throws PropertiesException, CancelException {
    for (int i = 0; i < rules.length; i++) {
      final TypeStateProperty property = createTypeStateProperty(rules[i]);
      typeStatePropertySet.add(property);
      BenignOracle ora = new BenignOracle(callGraphEngine.getCallGraph(), callGraphEngine.getPointerAnalysis());
      solvers.add(TypestateSolverFactory.getSolver(callGraphEngine.getAnalysisOptions(), callGraphEngine.getCallGraph(),
          callGraphEngine.getPointerAnalysis(), hg, property, ora, options, metrics, reporter, perfTracker, traceReporter));

    }
  }

  public static ILiveObjectAnalysis computeLiveObjectAnalysis(CallGraph cg, HeapGraph hg, boolean expensiveLiveAnalysis) {
    return new FILiveObjectAnalysis(cg, hg, expensiveLiveAnalysis);
  }

  /**
   * @param shouldDumpCallGraph
   * @throws CancelException
   * @throws IllegalArgumentException
   * @throws SetUpException
   */
  private void computeCallGraph(final boolean shouldDumpCallGraph) throws SafeException, IllegalArgumentException, CancelException {
    this.callGraphEngine.computeCallGraph();
    if (Trace.getTraceFile() != null) {
      if (shouldDumpCallGraph) {
        Trace.println(this.callGraphEngine.getCallGraph());
      }
    }
  }

  private TracingProperty createTypeStateTracer(final IClass klass) {
    return new TracingProperty(callGraphEngine.getCallGraph().getClassHierarchy(), Collections.singleton(klass));
  }

  protected TypeStateProperty createTypeStateProperty(final TypestateRule rule) {

    TypeStateProperty result = new TypeStateProperty(rule, callGraphEngine.getCallGraph().getClassHierarchy());
    result.validate();
    return result;
  }

  @SuppressWarnings("unused")
  private String[] getPropertyTypes() {
    Collection<String> propertyTypes = HashSetFactory.make();

    final TypestateRule[] rules = this.options.getRules();
    for (int i = 0; i < rules.length; i++) {
      for (Iterator<String> it = rules[i].getTypes().iterator(); it.hasNext();) {
        final String typeDef = it.next();
        propertyTypes.add(typeDef);
      }
    }
    return propertyTypes.toArray(new String[propertyTypes.size()]);
  }

  /**
   * @return Returns the metrics.
   */
  public TypeStateMetrics getMetrics() {
    return metrics;
  }

}
