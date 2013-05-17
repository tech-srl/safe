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

import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXCFABuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/**
 * A call-graph builder based on the 0-1-CFA builder, that also disambiguates
 * other container classes relevant to our current set of typestate properties
 * 
 * @author sfink
 * 
 */
public class CustomCICFABuilder extends ZeroXCFABuilder {

  /**
   * @param cha
   * @param warnings
   * @param options
   * @param appContextSelector
   * @param appContextInterpreter
   * @param reflect
   */
  public CustomCICFABuilder(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache, ContextSelector appContextSelector,
      SSAContextInterpreter appContextInterpreter) {
    super(cha, options, cache, appContextSelector, appContextInterpreter, ZeroXInstanceKeys.ALLOCATIONS
        | ZeroXInstanceKeys.SMUSH_MANY | ZeroXInstanceKeys.SMUSH_PRIMITIVE_HOLDERS | ZeroXInstanceKeys.SMUSH_STRINGS
        | ZeroXInstanceKeys.SMUSH_THROWABLES);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroOneContainerCFABuilder#makeInstanceKeys(com.ibm.wala.ipa.cha.ClassHierarchy,
   *      com.ibm.wala.util.warnings.WarningSet,
   *      com.ibm.wala.ipa.callgraph.AnalysisOptions,
   *      com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter)
   */
  protected ZeroXInstanceKeys makeInstanceKeys(ClassHierarchy cha, AnalysisOptions options,
      SSAContextInterpreter contextInterpreter, int instancePolicy) {
    return new CustomInstanceKeys(options, cha, contextInterpreter);
  }

}
