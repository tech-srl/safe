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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import com.ibm.safe.internal.entrypoints.EntryPointDefinition;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.MethodReference;

/**
 * @author egeay 
 * @author yahave
 *
 */
public final class SelectiveEntryPoints implements Iterable<Entrypoint> {

  public SelectiveEntryPoints(final AnalysisScope analysisScope, final IClassHierarchy classHierarchy,
      final EntryPointDefinition[] entryPointDefs) throws SetUpException {
    for (IClass clazz : classHierarchy) {
      for (int i = 0; i < entryPointDefs.length; i++) {
        if (clazz.getName().toString().equals(entryPointDefs[i].getClassName()) && !clazz.isInterface()
            && isApplicationClass(analysisScope, clazz)) {
          for (Iterator<IMethod> methodIter = clazz.getDeclaredMethods().iterator(); methodIter.hasNext();) {
            final IMethod method = methodIter.next();
            if (!method.isAbstract() &&
                isSameMethod(method.getReference(), entryPointDefs[i].getMethodName(), entryPointDefs[i].getMethodDescriptor())) {
              this.entryPoints.add(new DefaultEntrypoint(method.getReference(), classHierarchy));
            }
          }
        }
      }
    }
    if (this.entryPoints.isEmpty()) {
      throw new SetUpException(SafeEntryPoints.NO_VALID_ENTRYPOINTS);
    }
  }

  // --- Interface methods implementation

  public Iterator<Entrypoint> iterator() {
    return this.entryPoints.iterator();
  }

  // --- Private code

  private boolean isApplicationClass(final AnalysisScope scope, final IClass clazz) {
    return scope.getApplicationLoader().equals(clazz.getClassLoader().getReference());
  }

  private boolean isSameMethod(final MethodReference methodRef, final String entryMethodName, final String entryMethodDescriptor) {
    return (methodRef.getName().toString().equals(entryMethodName) && methodRef.getDescriptor().toString().equals(
        entryMethodDescriptor));
  }

  private final Collection<Entrypoint> entryPoints = new ArrayList<Entrypoint>(10);

}
