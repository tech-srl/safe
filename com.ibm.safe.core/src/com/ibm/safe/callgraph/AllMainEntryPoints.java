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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.strings.Atom;


/**
 * @author egeay 
 * @author yahave
 */
public final class AllMainEntryPoints implements Iterable<Entrypoint> {

  public AllMainEntryPoints(final AnalysisScope analysisScope, final IClassHierarchy classHierarchy,
      final Pattern[] mainClassesSelector, final boolean isVerboseMode) throws SetUpException {
    final Atom mainMethod = Atom.findOrCreateAsciiAtom(MAIN_METHOD_NAME);
    final Descriptor mainDescriptor = Descriptor.findOrCreateUTF8(MAIN_DESCRIPTOR);

    for (IClass clazz : classHierarchy) {
      if (!clazz.isInterface() && !clazz.isAbstract() && isApplicationClass(analysisScope, clazz)) {
        for (Iterator<IMethod> methodIter = clazz.getDeclaredMethods().iterator(); methodIter.hasNext();) {
          final IMethod method = methodIter.next();
          if (isMainMethod(method.getReference(), mainMethod, mainDescriptor)) {
            addPotentiallyMainClass(classHierarchy, mainClassesSelector, clazz, method);
          }
        }
      }
    }
    if (this.entryPoints.isEmpty()) {
      throw new SetUpException(SafeEntryPoints.NO_VALID_ENTRYPOINTS); //$NON-NLS-1$
    } else if (isVerboseMode) {
      System.out.println("Number of entrypoints detected: " + String.valueOf(this.entryPoints.size()));
    }
  }

  // --- Interface methods implementation

  public Iterator<Entrypoint> iterator() {
    return this.entryPoints.iterator();
  }

  // --- Private code

  private void addEntryPoint(final String className, final MethodReference methodRef, final IClassHierarchy cha) {
    System.out.println("Main class " + className + " detected as entry point.");
    this.entryPoints.add(new DefaultEntrypoint(methodRef, cha));
  }

  private void addPotentiallyMainClass(final IClassHierarchy classHierarchy, final Pattern[] mainClassesSelector,
      final IClass clazz, final IMethod method) {
    final String className = clazz.getName().toString().substring(1).replace('/', '.');
    if (mainClassesSelector.length > 0) {
      for (int i = 0; i < mainClassesSelector.length; i++) {
        final Matcher matcher = mainClassesSelector[i].matcher(className);
        if (matcher.matches()) {
          addEntryPoint(className, method.getReference(), classHierarchy);
        }
      }
    } else {
      addEntryPoint(className, method.getReference(), classHierarchy);
    }
  }

  private boolean isApplicationClass(final AnalysisScope scope, final IClass clazz) {
    return scope.getApplicationLoader().equals(clazz.getClassLoader().getReference());
  }

  private boolean isMainMethod(final MethodReference methodRef, final Atom mainMethod, final Descriptor mainDescriptor) {
    return (methodRef.getName().equals(mainMethod) && methodRef.getDescriptor().equals(mainDescriptor));
  }

  private final Collection<Entrypoint> entryPoints = new ArrayList<Entrypoint>(10);

  private static final String MAIN_METHOD_NAME = "main"; //$NON-NLS-1$

  private static final String MAIN_DESCRIPTOR = "([Ljava/lang/String;)V"; //$NON-NLS-1$
}
