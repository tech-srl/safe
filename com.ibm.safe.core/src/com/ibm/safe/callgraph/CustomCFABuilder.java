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

import java.util.Collection;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXContainerCFABuilder;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/**
 * A call-graph builder based on the 0-1-container builder, that also
 * disambiguates other container classes relevant to our current set of
 * typestate properties
 * 
 * @author sfink
 * 
 */
public class CustomCFABuilder extends ZeroXContainerCFABuilder {

  /**
   * @param fiatSet
   *            Set<IClass> that are interesting by fiat.
   */

  public CustomCFABuilder(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache, ContextSelector appContextSelector,
      SSAContextInterpreter appContextInterpreter, Collection<IClass> fiatSet) {
    super(cha, options, cache, appContextSelector, appContextInterpreter, 1);
    // this is horrible.
    CustomInstanceKeys c = (CustomInstanceKeys) getInstanceKeys();
    c.setFiatSet(fiatSet);
  }

  protected ContextSelector makeContainerContextSelector(ClassHierarchy cha, ZeroXInstanceKeys keys) {
    return new CustomContextSelector(cha, keys);
  }

  @Override
  protected ZeroXInstanceKeys makeInstanceKeys(IClassHierarchy cha, AnalysisOptions options,
      SSAContextInterpreter contextInterpreter, int instancePolicy) {
    return new CustomInstanceKeys(options, cha, contextInterpreter);
  }

}
