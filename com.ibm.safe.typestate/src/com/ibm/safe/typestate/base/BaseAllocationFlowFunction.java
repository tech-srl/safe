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
package com.ibm.safe.typestate.base;

import java.util.Iterator;

import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.dataflow.IFDS.IReversibleFlowFunction;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * @author Eran Yahav
 * @author sfink
 * 
 * A Base flow function represents a particular call in the call graph, which
 * causes a typestate transition according to the typestate property DFA
 */
public class BaseAllocationFlowFunction implements IReversibleFlowFunction {

  /**
   * domain of dataflow facts
   */
  private TypeStateDomain domain;

  /**
   * relevant instances being allocated
   */
  private OrdinalSet<InstanceKey> instances;

  /**
   * @param domain
   * @param instances
   */
  public BaseAllocationFlowFunction(TypeStateDomain domain, OrdinalSet<InstanceKey> instances) {
    this.domain = domain;
    this.instances = instances;
  }

  /**
   * 
   * @param d1 =
   *            integer corresponding to an (instance, state) pair
   * @return set of d2 such that (d1,d2) is an edge in this distributive
   *         function's graph representation, or null if there are none
   */
  public SparseIntSet getTargets(int d1) {

    MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();

    if (BaseFunctionProvider.DEBUG_LEVEL > 1) {
      Trace.println("Alloc : " + instances);
      Trace.println("Getting targets for: " + d1);
    }

    if (d1 != 0) {
      BaseFactoid inputFact = (BaseFactoid) domain.getMappedObject(d1);
      if (!instances.contains(inputFact.instance) || !strongUpdate(inputFact)) {
        result.add(d1);
      }
      return result;
    } else {
      // d1 == 0. gen new facts for each instance allocated
      result.add(0);
      for (Iterator<InstanceKey> it = instances.iterator(); it.hasNext();) {
        result.add(domain.getIndexForInitialState(it.next()));
      }
    }

    return result.isEmpty() ? null : result;
  }

  /**
   * Should this function use strong update to kill the inputFact?
   * 
   * Subclasses should override as desired.
   */
  protected boolean strongUpdate(BaseFactoid inputFact) {
    return false;
  }

  /**
   * @param d2 =
   *            integer corresponding to an (instance, state) pair
   * @return set of d1 such that (d1,d2) is an edge in this distributive
   *         function's graph representation, or null if there are none
   */
  public SparseIntSet getSources(int d2) {

    if (d2 == 0) {
      return SparseIntSet.singleton(0);
    } else {
      BaseFactoid fact = (BaseFactoid) domain.getMappedObject(d2);

      if (instances.contains(fact.instance)) {
        MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
        if (!strongUpdate(fact)) {
          result.add(d2);
        }
        result.add(0);
        return result;
      } else {
        // not a tracked instance. use identity flow
        return SparseIntSet.singleton(d2);
      }
    }
  }

  /**
   * @return Returns the instances.
   */
  public OrdinalSet<InstanceKey> getInstances() {
    return instances;
  }
}