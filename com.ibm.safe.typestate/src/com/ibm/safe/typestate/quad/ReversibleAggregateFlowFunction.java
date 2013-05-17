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

import java.util.Iterator;

import com.ibm.wala.dataflow.IFDS.IFlowFunction;
import com.ibm.wala.dataflow.IFDS.IReversibleFlowFunction;
import com.ibm.wala.util.collections.ReverseIterator;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * @author yahave
 * @author sfink
 * 
 */
public class ReversibleAggregateFlowFunction extends AggregateFlowFunction implements IReversibleFlowFunction {

  /**
   * @param f1
   * @param f2
   */
  public ReversibleAggregateFlowFunction(IReversibleFlowFunction f1, IReversibleFlowFunction f2) {
    super();
    composeFunction(f1);
    composeFunction(f2);
  }

  public SparseIntSet getSources(int d2) {
    if (getFunctions() == null || getFunctions().isEmpty()) {
      return SparseIntSet.singleton(d2);
    }

    MutableSparseIntSet currSet = MutableSparseIntSet.makeEmpty();
    MutableSparseIntSet nextSet = MutableSparseIntSet.makeEmpty();
    currSet.add(d2);

    for (Iterator<IFlowFunction> it = ReverseIterator.reverse(getFunctions().iterator()); it.hasNext();) {
      IReversibleFlowFunction curr = (IReversibleFlowFunction) it.next();
      for (IntIterator factoidIt = currSet.intIterator(); factoidIt.hasNext();) {
        int currFactoid = factoidIt.next();
        IntSet currResult = curr.getSources(currFactoid);
        // ND: currResult maybe null due to
        // VectorKillFlowFunction\VectorGenFlowFunction that return
        // TODO - check if it it not bettern to retunr a singleton {0}.
        if (currResult != null) {
          nextSet.addAll(currResult);
        }
      }
      currSet = nextSet;
      // initialize nextSet for the next iteration
      nextSet = MutableSparseIntSet.makeEmpty();
    }

    assert (currSet != null);

    return currSet;
  }

  public static IReversibleFlowFunction compose(IReversibleFlowFunction f1, IReversibleFlowFunction f2) {
    return new ReversibleAggregateFlowFunction(f1, f2);
  }

}
