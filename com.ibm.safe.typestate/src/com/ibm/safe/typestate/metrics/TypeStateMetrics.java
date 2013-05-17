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
package com.ibm.safe.typestate.metrics;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.safe.metrics.IMetrics;
import com.ibm.safe.metrics.IntHistogram;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IClassLoader;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.ShrikeCTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.bytecode.BytecodeStream;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;

/**
 * statistics about the typestate solver
 * 
 * @author egeay
 * @author Stephen Fink
 */
public final class TypeStateMetrics implements IMetrics {

  /**
   * TODO: this has nothing to do with typestate and should be moved elsewhere
   */
  private final Map<ClassLoaderReference, TypeStateMetricsByLoader> callGraphMetrics = HashMapFactory.make(3);

  /**
   * TODO: this has nothing to do with typestate and should be moved elsewhere
   */
  private final Map<ClassLoaderReference, TypeStateMetricsByLoader> chaMetrics = HashMapFactory.make(3);

  /**
   * Map: property name (String) -> Integer
   */
  private final Map<String, Integer> candidateStatements = HashMapFactory.make(10);

  /**
   * Map: property name (String) -> Integer
   */
  private final Map<String, Integer> DFAcandidateStatements = HashMapFactory.make(10);

  /**
   * Number of nodes in the supergraph built over the entire call graph
   */
  private int unoptimizedSupergraphSize;

  /**
   * The frequency count for the various supergraph sizes
   */
  private final IntHistogram supergraphSizes = new IntHistogram();

  public TypeStateMetrics(final IClassHierarchy classHierarchy, final CallGraph callGraph) {
    assert (classHierarchy != null);
    assert (callGraph != null);
    this.chaMetrics.put(ClassLoaderReference.Primordial, compute(classHierarchy, ClassLoaderReference.Primordial));
    this.chaMetrics.put(ClassLoaderReference.Extension, compute(classHierarchy, ClassLoaderReference.Extension));
    this.chaMetrics.put(ClassLoaderReference.Application, compute(classHierarchy, ClassLoaderReference.Application));

    this.callGraphMetrics.put(ClassLoaderReference.Primordial, compute(callGraph, ClassLoaderReference.Primordial));
    this.callGraphMetrics.put(ClassLoaderReference.Extension, compute(callGraph, ClassLoaderReference.Extension));
    this.callGraphMetrics.put(ClassLoaderReference.Application, compute(callGraph, ClassLoaderReference.Application));
  }

  // --- Interface methods implementation

  public CallGraphMetricsByLoader getCallGraphMetrics(final ClassLoaderReference classLoaderRef) {
    return (CallGraphMetricsByLoader) this.callGraphMetrics.get(classLoaderRef);
  }

  public TypeStateMetricsByLoader getCHAMetrics(final ClassLoaderReference classLoaderRef) {
    return this.chaMetrics.get(classLoaderRef);
  }

  // --- Private code

  private CallGraphMetricsByLoader compute(final CallGraph callGraph, final ClassLoaderReference classLoaderRef) {
    final Set<IMethod> methods = HashSetFactory.make();
    final Set<IClass> classes = HashSetFactory.make();
    long byteCodeStatements = 0;
    long cgNodes = 0;

    for (Iterator<CGNode> iter = callGraph.iterator(); iter.hasNext();) {
      final IMethod method = ((CGNode) iter.next()).getMethod();

      if (method.getDeclaringClass().getReference().getClassLoader().equals(classLoaderRef)) {
        if (!methods.contains(method)) {
          methods.add(method);
          cgNodes += callGraph.getNodes(method.getReference()).size();

          if (method instanceof ShrikeCTMethod) {
            byteCodeStatements += countStatements(method);
          }
        }

        classes.add(method.getDeclaringClass());
      }
    }

    return new CallGraphMetricsByLoader(byteCodeStatements, classes.size(), methods.size(), cgNodes, classLoaderRef.getName()
        .toString());
  }

  private TypeStateMetricsByLoader compute(final IClassHierarchy classHierarchy, final ClassLoaderReference classLoaderRef) {
    final IClassLoader classLoader = classHierarchy.getLoader(classLoaderRef);
    long numberOfClasses = 0;
    long numberOfMethods = 0;
    long byteCodeStatements = 0;
    for (Iterator<IClass> iter = classLoader.iterateAllClasses(); iter.hasNext();) {
      final IClass clazz = iter.next();
      ++numberOfClasses;

      for (Iterator<IMethod> iterator = clazz.getDeclaredMethods().iterator(); iterator.hasNext();) {
        final IMethod method = iterator.next();
        ++numberOfMethods;

        if (method instanceof ShrikeCTMethod) {
          byteCodeStatements += countStatements(method);
        }
      }
    }
    return new TypeStateMetricsByLoader(byteCodeStatements, numberOfClasses, numberOfMethods, classLoaderRef.getName().toString());
  }

  private long countStatements(final IMethod method) {
    final BytecodeStream bcStream = ((ShrikeCTMethod) method).getBytecodeStream();
    long count = 0;
    if (bcStream == null) {
      return 0;
    }
    while (bcStream.hasMoreBytecodes()) {
      bcStream.nextInstruction();
      count++;
    }
    return count;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.metrics.ITypeStateMetrics#setNumberOfCandidateStatements(java.lang.String,
   *      int)
   */
  public void setNumberOfCandidateStatements(String propertyName, int n) {
    // a sanity check ... staged solver will call this more than once
    int old = getNumberOfCandidateStatements(propertyName);
    if (old > -1) {
      assert(old == n);
    }
    candidateStatements.put(propertyName, n);
  }

  /**
   * @param propertyName
   * @return -1 if not registered
   */
  public int getNumberOfCandidateStatements(String propertyName) {
    Integer n = candidateStatements.get(propertyName);
    return n == null ? -1 : n.intValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.metrics.ITypeStateMetrics#setNumberOfCandidateStatements(java.lang.String,
   *      int)
   */
  public void setNumberOfDFASliceCandidateStatements(String propertyName, int n) {
    // a sanity check ... staged solver will call this more than once
    int old = getNumberOfDFASliceCandidateStatements(propertyName);
    if (old > -1) {
      assert(old == n);
    }
    DFAcandidateStatements.put(propertyName, n);
  }

  /**
   * @param propertyName
   * @return -1 if not registered
   */
  public int getNumberOfDFASliceCandidateStatements(String propertyName) {
    Integer n = DFAcandidateStatements.get(propertyName);
    return n == null ? -1 : n.intValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.metrics.ITypeStateMetrics#getPropertyNames()
   */
  public Iterator<String> getPropertyNames() {
    return candidateStatements.keySet().iterator();
  }

  /**
   * @return Returns the unoptimizedSupergraphSize.
   */
  public int getUnoptimizedSupergraphSize() {
    return unoptimizedSupergraphSize;
  }

  /**
   * @param unoptimizedSupergraphSize
   *            The unoptimizedSupergraphSize to set.
   */
  public void setUnoptimizedSupergraphSize(int unoptimizedSupergraphSize) {
    this.unoptimizedSupergraphSize = unoptimizedSupergraphSize;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.metrics.ITypeStateMetrics#recordSupergraphSize(int)
   */
  public void recordSupergraphSize(int numberOfNodes) {
    supergraphSizes.add(numberOfNodes, 1);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.metrics.ITypeStateMetrics#getSupergraphSizes()
   */
  public Iterator getSupergraphSizes() {
    return supergraphSizes.iterator();
  }

}
