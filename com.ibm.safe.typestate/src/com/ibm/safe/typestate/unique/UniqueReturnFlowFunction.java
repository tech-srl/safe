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
package com.ibm.safe.typestate.unique;

import java.util.Iterator;

import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.dataflow.IFDS.IReversibleFlowFunction;
import com.ibm.wala.dataflow.IFDS.VectorKillFlowFunction;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.collections.FilterIterator;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * The unique-flow function exploits escape analysis to kill data flow facts
 * corresponding to live instances when a method returns.
 * 
 * @author Stephen Fink
 * 
 */
public class UniqueReturnFlowFunction implements IReversibleFlowFunction {

  private final VectorKillFlowFunction delegate;

  /**
   * @param caller
   *          the node we are RETURNING TO
   */
  public UniqueReturnFlowFunction(UniqueTypeStateDomain d, OrdinalSet<InstanceKey> trackedInstances,
      final ILiveObjectAnalysis liveAnalysis, final CGNode caller, final HeapGraph hg, final CallSiteReference site, final IR ir) {

    // a filter that accepts a factoid only if it should be killed when we
    // return
    // to a node
    Filter shouldKill = new Filter() {
      public boolean accepts(Object o) {
        if (o instanceof UniqueFactoid) {
          UniqueFactoid f = (UniqueFactoid) o;
          InstanceKey ik = f.instance;
          if (ik instanceof AllocationSiteInNode) {
            AllocationSiteInNode ak = (AllocationSiteInNode) ik;
            if (!liveAnalysis.mayBeLive(ak, caller, ir.getCallInstructionIndices(site))) {
              // the object cannot be live in the caller. kill all facts
              return true;
            } else {
              if (!f.isUnique() && mustBeUniqueInNode(ak, caller, hg, liveAnalysis)) {
                // there can be at most one pointer to this instance key live
                // at any given time when
                // node is at the top of the stack. So, kill the "not-unique"
                // fact.
                return true;
              } else {
                return false;
              }
            }
          } else {
            return false;
          }
        } else {
          assert !(o instanceof BaseFactoid);
          // this is the universal dummy fact. exclude it from the kill.
          return false;
        }
      }

    };
    MutableSparseIntSet kill = MutableSparseIntSet.makeEmpty();
    for (Iterator<Object> it = new FilterIterator<Object>(d.iterator(), shouldKill); it.hasNext();) {
      BaseFactoid f = (BaseFactoid) it.next();
      kill.add(d.add(f));
    }
    delegate = VectorKillFlowFunction.make(kill);
  }

  /**
   * There must be at most one live instance of ik when "node" is executing at
   * the top of stack if the following conditions hold:
   * <ul>
   * <li>ik is only pointed to by locals (no heap pointers)
   * <li>ik is not passed into the node as a parameter
   * <li>ik is pointed to by at most one local in the node
   * </ul>
   */
  public static boolean mustBeUniqueInNode(AllocationSiteInNode ik, CGNode node, HeapGraph hg, ILiveObjectAnalysis live) {
    boolean foundFirstLocal = false;
    for (Iterator<? extends Object> it = hg.getPredNodes(ik); it.hasNext();) {
      PointerKey p = (PointerKey) it.next();
      if (!(p instanceof LocalPointerKey)) {
        // a pointer from the heap. give up.
        return false;
      } else {
        LocalPointerKey lpk = (LocalPointerKey) p;
        if (lpk.getNode().equals(node)) {
          if (foundFirstLocal) {
            // we already found a local. give up.
            return false;
          } else {
            foundFirstLocal = true;
            if (lpk.isParameter()) {
              // instance was passed in from caller. give up.
              return false;
            }
          }
        }
      }
    }

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.dataflow.IFDS.IReversibleFlowFunction#getSources(int)
   */
  public IntSet getSources(int d2) {
    return delegate.getSources(d2);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.dataflow.IFDS.IFlowFunction#getTargets(int)
   */
  public IntSet getTargets(int d1) {
    return delegate.getTargets(d1);
  }

}