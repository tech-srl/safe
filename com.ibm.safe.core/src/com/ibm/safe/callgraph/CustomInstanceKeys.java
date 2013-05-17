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
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.callgraph.propagation.rta.RTAContextInterpreter;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;

/**
 * @author sfink
 * @author eyahav
 */
public class CustomInstanceKeys extends ZeroXInstanceKeys {

  private final static boolean USE_BASE_POLICY = false;

  /**
   * Set<IClass> that are interesting by fiat.
   */
  private Collection<IClass> fiatSet;

  public CustomInstanceKeys(AnalysisOptions options, IClassHierarchy cha, RTAContextInterpreter contextInterpreter) {
    super(options, cha, contextInterpreter, ZeroXInstanceKeys.ALLOCATIONS | ZeroXInstanceKeys.SMUSH_MANY
        | ZeroXInstanceKeys.SMUSH_PRIMITIVE_HOLDERS | ZeroXInstanceKeys.SMUSH_STRINGS | ZeroXInstanceKeys.SMUSH_THROWABLES);
  }

  /**
   * Consider everything from java.util "interesting"
   * 
   * @see com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys#isInteresting(com.ibm.wala.classLoader.IClass)
   */
  public boolean isInteresting(IClass C) {
    if (USE_BASE_POLICY) {
      return super.isInteresting(C);
    }
    if (fiatSet.contains(C) || super.isInteresting(C)) {
      return true;
    } else {
      if (C.getReference().getName().getPackage() != null) {
        if (C.getClassLoader().getReference().equals(ClassLoaderReference.Primordial)
            && C.getReference().getName().getPackage().equals(TypeReference.JavaUtilCollection.getName().getPackage())) {
          return !isThrowable(C);
        }
      }
      if (C.getReference().getName().getPackage() != null) {
        if (C.getClassLoader().getReference().equals(ClassLoaderReference.Primordial)
            && C.getReference().getName().getPackage().equals(TypeReference.JavaIoSerializable.getName().getPackage())) {
          return !isThrowable(C);
        }
      }
      IClass thread = getClassHierarchy().lookupClass(TypeReference.JavaLangThread);
      if (getClassHierarchy().isSubclassOf(C, thread)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return Returns the fiatSet.
   */
  protected Collection<IClass> getFiatSet() {
    return fiatSet;
  }

  /**
   * @param fiatSet
   *            The fiatSet to set.
   */
  public void setFiatSet(Collection<IClass> fiatSet) {
    this.fiatSet = fiatSet;
  }
}
