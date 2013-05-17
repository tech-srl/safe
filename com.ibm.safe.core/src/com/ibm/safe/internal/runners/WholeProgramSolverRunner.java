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
package com.ibm.safe.internal.runners;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;

import com.ibm.safe.callgraph.CallGraphEngine;
import com.ibm.safe.internal.entrypoints.EntryPointDefinition;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SafeException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.options.CommonOptions;
import com.ibm.safe.options.WholeProgramOptions;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.properties.PropertiesManager;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.runners.IResultObserver;
import com.ibm.wala.util.CancelException;

public class WholeProgramSolverRunner extends AbstractSolverRunner {

  public WholeProgramSolverRunner(PropertiesManager thePropertiesManager, PerformanceTracker performanceTracker,
      IReporter safeReporter) {
    super(thePropertiesManager, performanceTracker, safeReporter);
  }

  public void run(IResultObserver observer, IProgressMonitor monitor) throws SafeException, CancelException, IllegalArgumentException, CoreException {
    // TODO Auto-generated method stub

  }

  protected CallGraphEngine createCallGraphEngine(IProgressMonitor monitor) throws PropertiesException, SetUpException, CoreException {
    final CommonOptions commonOptions = getCommonOptions();
    CodeKind modulesCodeKind = null;
    try {
      modulesCodeKind = getModulesCodeKind(commonOptions);
    } catch (JavaModelException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    if (modulesCodeKind == CodeKind.J2SE_J2EE_MIX) {
      throw new SetUpException("J2SE with J2EE modules are used with null-deref analysis. Separation is recommended."); //$NON-NLS-1$
    }
    if (modulesCodeKind == CodeKind.J2SE) {
      if (getManager().isJ2EEOptionsActivated()) {
        throw new SetUpException("J2EE option(s) used with J2SE code. Please correct.");
      }
    } else {
      if (getManager().isJ2SEOptionsActivated()) {
        throw new SetUpException("J2SE option(s) used with J2EE code. Please correct.");
      }
    }

    final EntryPointDefinition[] entryPoints = getManager().getEntryPointDefinitions();
    CallGraphEngine cgEngine = new CallGraphEngine(getManager().getCallGraphKind(), getManager(),
        getPerformanceTracker(), monitor, modulesCodeKind != CodeKind.J2SE);

    if (!getManager().isContradictionAnalysis() &&  // Contradiction analysis works without entry points.
        (modulesCodeKind == CodeKind.J2SE) && (entryPoints.length == 0) && !getManager().isJ2SEOptionsActivated()) {
      throw new SetUpException(
          "Entry points are required. \nSpecify them with one of next options: 'main_classes', 'makeAllMainClassesEntryPoint', entry_points' or 'entry_points_file'.");
    }
    return cgEngine;
  }

  protected WholeProgramOptions getManager() {
    return (WholeProgramOptions) this.propertiesManager;
  }

}
