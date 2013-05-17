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
 * Created on Dec 31, 2004
 */
package com.ibm.safe.callgraph;

import java.util.Iterator;

import com.ibm.safe.utils.Trace;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.types.Descriptor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.NonNullSingletonIterator;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.strings.Atom;

/**
 * This utility class is used to create multiple or single entry points for a
 * J2SE application. Currently, the makeAllEntryPoints causes troubles with the
 * callGraph constructions since it "reaches" illegal states.
 * 
 * @author Eran Yahav (yahave)
 * @author sfink
 */
public class SafeEntryPoints {

  public static final String NO_VALID_ENTRYPOINTS = "No valid entry points have been detected for the analysis.";

  private static final int DEBUG_LEVEL = 0;

  /**
   * @return Entrypoints object for a Main J2SE class
   */
  @SuppressWarnings("unused")
  public static Iterable<Entrypoint> makeSingleMainEntrypoint(AnalysisScope scope, final ClassHierarchy cha, String className) {
    validateClassName(className);
    final Atom mainMethodAtom = Atom.findOrCreateAsciiAtom("main");
    final TypeReference T = TypeReference.findOrCreate(scope.getApplicationLoader(), TypeName.string2TypeName(className));
    final MethodReference main = MethodReference.findOrCreate(T, mainMethodAtom, Descriptor
        .findOrCreateUTF8("([Ljava/lang/String;)V"));

    final IClass klass = cha.lookupClass(T);
    if (klass == null) {
      throw new RuntimeException("could not load app client entrypoint " + T);
    }
    final IMethod m = cha.resolveMethod(klass, main.getSelector());
    if (m == null) {
      return null;
    }

    if (DEBUG_LEVEL > 1) {
      Trace.println("Single Entry Point: " + m);
    }

    return new Iterable<Entrypoint>() {
      public Iterator<Entrypoint> iterator() {
        return new NonNullSingletonIterator<Entrypoint>(new DefaultEntrypoint(m, cha));
      }
    };
  }

  public static Iterable<Entrypoint> makeAllEntrypoints(AnalysisScope scope, final ClassHierarchy cha) {
    return new AllApplicationEntrypoints(scope, cha);
  }

  private static void validateClassName(String className) {
    if (className.indexOf("L") != 0) {
      Assertions.productionAssertion(false, "Expected class name to start with L " + className);
    }
    if (className.indexOf(".") > 0) {
      Assertions.productionAssertion(false, "Expected class name formatted with /, not . " + className);
    }
  }
}