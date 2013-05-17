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
 * Created on Jan 23, 2005
 */
package com.ibm.safe.processors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.safe.cha.J2SEClassHierarchyEngine;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.CancelException;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class BaseClassProcessor implements ClassProcessor {

  private final static boolean DEBUG = false;

  /** underlying class hierarchy */
  protected IClassHierarchy classHierarchy;

  /** underlying callgraph */
  protected CallGraph callGraph;

  protected List<MethodProcessor> methodProcessors;

  public BaseClassProcessor(IClassHierarchy hierarchy, CallGraph callGraph) {
    this.classHierarchy = hierarchy;
    this.callGraph = callGraph;
    this.methodProcessors = new ArrayList<MethodProcessor>();
  }

  public void processProlog(IClass klass) {

  }

  public void processEpilog(IClass klass) {

  }

  public void addMethodProcessor(MethodProcessor mp) {
    methodProcessors.add(mp);
  }

  public void process(IClass klass) throws CancelException {

    for (Iterator<MethodProcessor> procIt = methodProcessors.iterator(); procIt.hasNext();) {
      MethodProcessor processor = procIt.next();
      processor.setup(klass, classHierarchy);
    }

    for (Iterator<IMethod> methodIterator = klass.getDeclaredMethods().iterator(); methodIterator.hasNext();) {
      IMethod method = methodIterator.next();

      if (!J2SEClassHierarchyEngine.isApplicationClass(method.getDeclaringClass())) {
        continue;
      }

      if (DEBUG) {
        Trace.println("-----------------------------------------------------------");
        Trace.println("Processing Method " + method + " of class " + method.getDeclaringClass());
        Trace.println("-----------------------------------------------------------");
      }

      for (Iterator<MethodProcessor> it = methodProcessors.iterator(); it.hasNext();) {
        MethodProcessor mp = it.next();
        mp.processProlog(method);
        mp.process(method);
        mp.processEpilog(method);
      }

    }
  }

  public Object getResult() {
    return null;
  }

}