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

import java.io.IOException;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;

import com.ibm.safe.cha.J2SEClassHierarchyEngine;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.options.WholeProgramOptions;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.perf.PerformanceTracker.Stages;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.client.AbstractAnalysisEngine;
import com.ibm.wala.ide.util.ProgressMonitorDelegate;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;

/**
 * specialized call graph engine
 * @author yahave
 * @author sjfink
 *
 */
public class CallGraphEngine extends AbstractAnalysisEngine {

  public static enum CallGraphKind {
    CHA, RTA, ZERO_CFA, ZERO_ONE_CFA, XTA, ZERO_CONTAINER_CFA, ZERO_ONE_CONTAINER_CFA, ZERO_ONE_CUSTOM, ONE_ONE_CFA
  }

  private final CallGraphKind callGraphKind;

  private AbstractAnalysisEngine chaEngine;

  private final AnalysisOptions domoOptions = new AnalysisOptions();

  private final WholeProgramOptions wholeProgramOptions;

  private final IProgressMonitor monitor;

  private final PerformanceTracker perfoTracker;

  private String[] interestingTypes;

  public CallGraphEngine(final CallGraphKind callGraphKind, final WholeProgramOptions wpOptions,
      final PerformanceTracker domoPerfoTracker, final IProgressMonitor monitor, boolean isJ2EE) throws PropertiesException {
    this.callGraphKind = callGraphKind;
    this.perfoTracker = domoPerfoTracker;
    this.wholeProgramOptions = wpOptions;
    this.monitor = monitor;
    if (isJ2EE) {
      // chaEngine = new J2EEClassHierarchyEngine(wpOptions, perfoTracker,
      // monitor);
    } else {
      chaEngine = new J2SEClassHierarchyEngine(wpOptions, perfoTracker, monitor);
    }
  }

  public void computeCallGraph() throws SafeException, IllegalArgumentException, CancelException {

    try {
      super.scope = wholeProgramOptions.getOrCreateAnalysisScope();
      if (chaEngine.getClassHierarchy() == null) {
        chaEngine.buildClassHierarchy();
      }
      super.setClassHierarchy(chaEngine.getClassHierarchy());
    } catch (JavaModelException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (CoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    final Iterable<Entrypoint> entrypoints;

    if (this.wholeProgramOptions.allMainClassesEntrypoints()) {
      entrypoints = new AllMainEntryPoints(getScope(), getClassHierarchy(), this.wholeProgramOptions.getMainClassesSelector(),
          this.wholeProgramOptions.isVerboseMode());
    } else {
      entrypoints = new SelectiveEntryPoints(getScope(), getClassHierarchy(), this.wholeProgramOptions.getEntryPointDefinitions());
    }

    this.domoOptions.setAnalysisScope(getScope());
    this.domoOptions.setEntrypoints(entrypoints);

    try {
      if (perfoTracker != null) {
        perfoTracker.startTracking(Stages.CALLGRAPH.toString());
      }
      buildCallGraph(getClassHierarchy(), this.domoOptions, true /* needsPointerAnalysis */, ProgressMonitorDelegate.createProgressMonitorDelegate(monitor));
    } finally {
      if (perfoTracker != null) {
        perfoTracker.stopTracking(Stages.CALLGRAPH.toString());
      }
    }
  }

  public void setInterestingTypes(final String[] interestingTypes) throws PropertiesException {
    this.interestingTypes = interestingTypes;
  }

  public CallGraphBuilder getCallGraphBuilder(IClassHierarchy cha, AnalysisOptions options, IAnalysisCacheView cache) {

    CallGraphBuilder builder = null;
    ClassLoader cl = getClass().getClassLoader(); // Only used to get
    // resources.
    switch (callGraphKind) {
    case RTA:
      builder = Util.makeRTABuilder(options, cache, cha, getScope());
      Trace.println("Using RTA Builder");
      break;
    case ZERO_CFA:
      builder = Util.makeZeroCFABuilder(options, cache, cha, getScope());
      Trace.println("Using 0-CFA Builder");
      break;
    case ZERO_ONE_CFA:
      builder = makeZeroOneCFABuilder(options, cache, cha, cl, getScope(), true);
      Trace.println("Using 0-1-CFA Builder");
      break;
    case ZERO_CONTAINER_CFA:
      builder = Util.makeZeroContainerCFABuilder(options, cache, cha, getScope());
      Trace.println("Using 0-Container-CFA Builder");
      break;
    case ZERO_ONE_CONTAINER_CFA:
      builder = Util.makeZeroOneContainerCFABuilder(options, cache, cha, getScope());
      Trace.println("Using 0-1-Container-CFA Builder");
      break;
    case ZERO_ONE_CUSTOM:
      builder = makeCustomCFABuilder(options, cache, cha, cl, getScope(), toIClassCollection(interestingTypes, cha));
      Trace.println("Using 0-1-Custom-CFA Builder");
      break;
    case CHA:
      builder = makeCHABasedCFABuilder(options, cache, cha, cl, getScope());
      Trace.println("Using CHA-Based-CFA Builder");
      break;
    default:
      Assertions.UNREACHABLE();
      break;
    }
    return builder;
  }

  /**
   * @return a 0-1-CFA Call Graph Builder.
   * 
   *         This version uses the DEDUCED_PLUS_STRINGSTUFF policy to avoid
   *         disambiguating uninteresting types.
   * 
   * @param options
   *          options that govern call graph construction
   * @param cha
   *          governing class hierarchy
   * @param cl
   *          classloader that can find DOMO resources
   * @param scope
   *          representation of the analysis scope
   * @param dmd
   *          deployment descriptor abstraction
   * @param warnings
   *          an object which tracks analysis warnings
   * @param keepPointsTo
   *          preserve PointsTo graph for posterity?
   */
  public static CallGraphBuilder makeZeroOneCFABuilder(final AnalysisOptions options, IAnalysisCacheView cache,
      final IClassHierarchy cha, final ClassLoader cl, final AnalysisScope scope, final boolean keepPointsTo) {

    com.ibm.wala.ipa.callgraph.impl.Util.addDefaultSelectors(options, cha);
    com.ibm.wala.ipa.callgraph.impl.Util.addDefaultBypassLogic(options, scope, cl, cha);
    ContextSelector appSelector = null;
    SSAContextInterpreter appInterpreter = null;

    return new CustomCICFABuilder(cha, options, cache, appSelector, appInterpreter);
  }

  /**
   * @return a customized 0-1-CFA builder
   * 
   *         This version uses the DEDUCED_PLUS_STRINGSTUFF policy to avoid
   *         disambiguating uninteresting types.
   * 
   * @param options
   *          options that govern call graph construction
   * @param cha
   *          governing class hierarchy
   * @param cl
   *          classloader that can find DOMO resources
   * @param scope
   *          representation of the analysis scope
   * @param dmd
   *          deployment descriptor abstraction
   * @param warnings
   *          an object which tracks analysis warnings
   * @param interestingTypes
   *          Collection<IClass>
   */
  public static CallGraphBuilder makeCustomCFABuilder(final AnalysisOptions options, IAnalysisCacheView cache,
      final IClassHierarchy cha, final ClassLoader cl, final AnalysisScope scope, final Collection<IClass> interestingTypes) {

    assert interestingTypes != null;

    com.ibm.wala.ipa.callgraph.impl.Util.addDefaultSelectors(options, cha);
    com.ibm.wala.ipa.callgraph.impl.Util.addDefaultBypassLogic(options, scope, cl, cha);
    ContextSelector appSelector = null;
    SSAContextInterpreter appInterpreter = null;

    return new CustomCFABuilder(cha, options, cache, appSelector, appInterpreter, interestingTypes);
  }

  public static CallGraphBuilder makeCHABasedCFABuilder(final AnalysisOptions options, IAnalysisCacheView cache,
      final IClassHierarchy cha, final ClassLoader cl, final AnalysisScope scope) {

    com.ibm.wala.ipa.callgraph.impl.Util.addDefaultSelectors(options, cha);
    com.ibm.wala.ipa.callgraph.impl.Util.addDefaultBypassLogic(options, scope, cl, cha);
    ContextSelector appSelector = null;
    SSAContextInterpreter appInterpreter = null;

    return new CHABasedCFABuilder(cha, options, cache, appSelector, appInterpreter);
  }

  public CallGraph getCallGraph() {
    return super.getCallGraph();
  }

  public PointerAnalysis getPointerAnalysis() {
    return super.getPointerAnalysis();
  }

  public AnalysisOptions getAnalysisOptions() {
    return this.domoOptions;
  }

  public void computeClassHierarchy() {
    if (getClassHierarchy() == null) {
      try {
        super.scope = wholeProgramOptions.getOrCreateAnalysisScope();
      } catch (PropertiesException e) {
        throw new RuntimeException("Arrgghh.");
      } catch (JavaModelException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (CoreException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      IClassHierarchy builtCha;

      builtCha = buildClassHierarchy();
      super.setClassHierarchy(builtCha);
      super.cg = new CHABasedCallGraph(builtCha, domoOptions, makeDefaultCache());

      // super.pointerAnalysis = new TypeBasedPointerAnalysis()
    }
  }

  public IClassHierarchy classHierarchy() {
    IClassHierarchy result = super.getClassHierarchy();
    assert (result != null);
    return result;
  }

  private static Collection<IClass> toIClassCollection(String[] propertyTypes, IClassHierarchy cha) {
    Collection<IClass> result = HashSetFactory.make();
    for (int i = 0; i < propertyTypes.length; i++) {
      String t = propertyTypes[i];
      IClass klass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Application, t));
      // It is okay for some types to be out of scope and to be simply
      // ignored here.
      if (klass != null) {
        result.add(klass);
        if (klass.isInterface()) {
          result.addAll(cha.getImplementors(klass.getReference()));
        } else {
          result.addAll(cha.computeSubClasses(klass.getReference()));
        }
      }
    }
    return result;
  }
}
