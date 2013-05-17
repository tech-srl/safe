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
package com.ibm.safe.typestate.mine;

import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.merge.AbstractUnification;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.wala.dataflow.IFDS.IMergeFunction;
import com.ibm.wala.util.intset.BitVectorIntSet;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableMapping;

/**
 * Merge function that merges a set of AbstractEventTraces. This
 * implementation does total merge: at every program point it merges all
 * factoids for a given "auxiliary" state, a la Property Simulation
 * @author sfink
 * @author yahave
 * 
 */
public class UnifyMerge extends AbstractUnification {

  private UnifyMerge(TypeStateDomain domain) {
    super(domain);
  }

  public int merge(IntSet x, int j) {

    if (j == 0)
      return 0;
    
    assert j != 0 : "don't merge 0 please";

    // reps := set of factoids to merge.
    BitVectorIntSet reps = new BitVectorIntSet();
    int jrep = uf.find(j);
    reps.add(jrep);

    // System.err.println("UNIFY " + x + " " + j);

    BaseFactoid f_j = getRepresentativeFactoid(jrep);

    for (IntIterator it = x.intIterator(); it.hasNext();) {
      int i = it.next();
      if (i != 0) {
        int r = uf.find(i);
        if (!reps.contains(r)) {
          // first check: are the 2 factoids equal, modulo the abstract history?
          // note that this comparison is genearl for all kinds of
          // factoids that inherit from BaseFactoid
          // TODO: find a way to do this without creating a new index.
          AbstractHistory t_r = (AbstractHistory) getRepresentativeFactoid(r).state;
          int test = getDomain().getIndexForStateDelta(f_j, t_r);
          if (test == getRealRepresentative(r)) {
            // yes, the traces are equal excluding the dfa
            // we need to merge with r.
            reps.add(r);
          }
        }
      }
    }

    return unify(reps, jrep);
  }

  public static IMergeFunctionFactory factory() {
    return new IMergeFunctionFactory() {
      public IMergeFunction create(MutableMapping domain) {

        assert (domain instanceof TypeStateDomain);

        return new UnifyMerge((TypeStateDomain) domain);
      }
    };
  }

}
