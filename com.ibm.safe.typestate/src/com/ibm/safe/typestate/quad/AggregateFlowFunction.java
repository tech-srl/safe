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
package com.ibm.safe.typestate.quad;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ibm.safe.utils.Trace;
import com.ibm.wala.dataflow.IFDS.IFlowFunction;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.dataflow.IFDS.IdentityFlowFunction;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * Composes the effect of flow functions. Mainly used to compute the combined
 * effect of instructions in a basic-block. Reverse-flow is currently based on
 * caching of forward-computation result. While this generally consumes more
 * space, computing backwards-flow in the presence of pointers (as we do in the
 * AP engine) may be less efficient (and lead to imprecisions). Since
 * reverse-flow is only needed when a call-function is present in the aggregate,
 * we only maintain the cache in that case.
 * 
 * @author Eran Yahav (yahave)
 * 
 */
public class AggregateFlowFunction implements IUnaryFlowFunction {

  private final static int DEBUG_LEVEL = 0;

  /**
   * since we are not adding identity functions to the aggregate the block size
   * is usually very small (measurements show an average size of 2).
   */
  private static final int ARRAY_SIZE = 2;

  private static final boolean DEBUG_COUNT_INSTANCES = false;

  private static long instances = 0;

  /**
   * composed functions
   */
  private List<IFlowFunction> functions;

  public AggregateFlowFunction() {
    if (DEBUG_COUNT_INSTANCES) {
      instances++;
      if (instances % 5000 == 0) {
        Trace.println("-I-" + instances);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.dataflow.IFDS.IFlowFunction#getTargets(int)
   */
  public SparseIntSet getTargets(int d1) {
    if (functions == null || functions.isEmpty()) {
      return SparseIntSet.singleton(d1);
    }

    if (DEBUG_LEVEL > 0) {
      Trace.println("Aggregate getTargets for: " + d1);
    }

    MutableSparseIntSet currSet = MutableSparseIntSet.makeEmpty();
    MutableSparseIntSet nextSet = MutableSparseIntSet.makeEmpty();
    currSet.add(d1);

    for (Iterator<IFlowFunction> it = functions.iterator(); it.hasNext();) {

      IUnaryFlowFunction curr = (IUnaryFlowFunction) it.next();
      for (IntIterator factoidIt = currSet.intIterator(); factoidIt.hasNext();) {
        int currFactoid = factoidIt.next();
        IntSet currResult = curr.getTargets(currFactoid);
        if (DEBUG_LEVEL > 1) {
          Trace.println("intermediate targets for " + currFactoid + ": " + currResult + " (" + curr + ")");
        }
        // ND: currResult maybe null due to
        // VectorKillFlowFunction\VectorGenFlowFunction that return null
        // TODO - check if it is not better to return a singleton {0}.
        if (currResult != null) {
          nextSet.addAll(currResult);
        }
      }
      currSet = nextSet;
      // initialize nextSet for the next iteration
      nextSet = MutableSparseIntSet.makeEmpty();
    }
    if (DEBUG_LEVEL > 1) {
      Trace.println("final targets for " + d1 + ": " + currSet);
    }

    assert currSet != null;

    return currSet;
  }

  /**
   * @param flow
   */
  public void composeFunction(IFlowFunction flow) {
    assert flow != null : "cannot add a null flow function";
    if (flow == IdentityFlowFunction.identity()) {
      // no need to compose with identity, just ignore
      return;
    }
    if (functions == null) {
      functions = new ArrayList<IFlowFunction>(ARRAY_SIZE);
    }
    functions.add(flow);

    // EY: disable this expensive and redundant postcond for now.
    // Assertions.postcondition(functions.contains(flow), "flow not added to
    // functions");
  }

  public boolean isEmpty() {
    return (functions == null || functions.isEmpty());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "Aggregate:" + (functions != null ? functions.toString() : "[]");
  }

  /**
   * @return Returns the functions.
   */
  protected List<IFlowFunction> getFunctions() {
    return functions;
  }

  public static IUnaryFlowFunction compose(IUnaryFlowFunction f1, IUnaryFlowFunction f2) {
    if (f2 == IdentityFlowFunction.identity()) {
      return f1;
    }
    if (f1 == IdentityFlowFunction.identity()) {
      return f2;
    }
    if (f1 instanceof AggregateFlowFunction) {
      AggregateFlowFunction ag = (AggregateFlowFunction) f1;
      ag.composeFunction(f2);
      return ag;
    }
    AggregateFlowFunction result = new AggregateFlowFunction();
    result.composeFunction(f1);
    result.composeFunction(f2);
    return result;
  }

  /**
   * Simlifies the aggregated flow function to
   * 
   * @return Identity - if no elements where composed FlowFunction - if only 1
   *         element was composed itself - if the function cannot be simplified
   */
  public IFlowFunction simplify() {
    if (functions == null)
      return IdentityFlowFunction.identity();

    switch (functions.size()) {
    case 0:
      return IdentityFlowFunction.identity();
    case 1:
      return functions.get(0);
    default:
      return this;
    }
  }

}
