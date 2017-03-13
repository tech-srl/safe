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
package com.ibm.safe.cha;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;

import com.ibm.safe.internal.exceptions.CancelationWorkException;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.options.CommonOptions;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.perf.PerformanceTracker.Stages;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.client.AbstractAnalysisEngine;
import com.ibm.wala.ide.util.ProgressMonitorDelegate;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CallGraphBuilder;
import com.ibm.wala.ipa.callgraph.IAnalysisCacheView;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.debug.Assertions;

/**
 * @TODO: Should clean out the extension of AbstractAnalysisEngine
 * because it now implies the ability to get the callgraph builder.
 * While doable here, that is confusing.  
 */
public class J2SEClassHierarchyEngine extends AbstractAnalysisEngine {

  private CommonOptions commonOptions;

  public J2SEClassHierarchyEngine(final CommonOptions commonOptions, final PerformanceTracker domoPerfoTracker,
      final IProgressMonitor monitor) {
    this.perfoTracker = domoPerfoTracker;
    this.progressMonitor = monitor;
    this.commonOptions = commonOptions;
  }

  // --- Overridden methods

  public IClassHierarchy buildClassHierarchy() {
    try {
      if (this.perfoTracker != null) {
        this.perfoTracker.startTracking(Stages.CHA.toString());
      }

      super.scope = this.commonOptions.getOrCreateAnalysisScope();
      setClassHierarchy(createClassHierarchyInstance());
    } catch (SafeException except) {
      throw new RuntimeException("Unable to build class hierarchy.", except);
    } catch (JavaModelException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (CoreException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} finally {
      if (this.perfoTracker != null) {
        this.perfoTracker.stopTracking(Stages.CHA.toString());
      }
    }
    return getClassHierarchy();
  }

  // --- Private code

  protected final PerformanceTracker getPerformanceTracker() {
    return this.perfoTracker;
  }

  private ClassHierarchy createClassHierarchyInstance() throws SafeException {
    try {
      return ClassHierarchyFactory.make(getScope(), ProgressMonitorDelegate.createProgressMonitorDelegate(this.progressMonitor));
    } catch (ClassHierarchyException except) {
      throw new CancelationWorkException(except.getLocalizedMessage());
    }
  }

  private final PerformanceTracker perfoTracker;

  private final IProgressMonitor progressMonitor;

  public static boolean isApplicationClass(IClass klass) {
    boolean result = true;
    ClassLoaderReference loaderRef = klass.getClassLoader().getReference();
    if (loaderRef.equals(ClassLoaderReference.Extension) || loaderRef.equals(ClassLoaderReference.Primordial)) {
      result = false;
    }
    return result;
  }

  @Override
  protected CallGraphBuilder getCallGraphBuilder(IClassHierarchy cha, AnalysisOptions options, IAnalysisCacheView cache) {
    Assertions.UNREACHABLE("Not meant to build a call graph through ClassHierarchyEngine!");
    return null;
  }
}
