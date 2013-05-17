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

import java.util.Iterator;

import com.ibm.safe.dfa.IDFA;
import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.wala.dataflow.IFDS.IMergeFunction;
import com.ibm.wala.util.intset.BasicNaturalRelation;
import com.ibm.wala.util.intset.IBinaryNaturalRelation;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableMapping;

/**
 * Merge function that merges a set of AbstractEventTraces. This implementation
 * does "lossless" merge; for DFA j, if there already exists a DFA i that is a
 * superset of j, then merge() == i.
 * 
 * @author sharonsh
 * @author yahave
 * @author sfink
 */
public class LossLessMerge implements IMergeFunction {

  private final TypeStateDomain domain;

  /**
   * Cache of the relation "isSubset" between abstract event traces
   */
  private final BinaryRelationTestCache subsetCache = new BinaryRelationTestCache();

  /**
   * if merged(i,j), then factoid j was the result of a merge of factoid i with
   * something else.
   */
  final IBinaryNaturalRelation merged = new BasicNaturalRelation();

  private LossLessMerge(TypeStateDomain domain) {
    this.domain = domain;
  }

  /**
   * @return an object that can merge abstract event traces
   */
  public static LossLessMerge make(TypeStateDomain domain) {
    return new LossLessMerge(domain);
  }

  public int merge(IntSet x, int j) {

    assert j != 0 : "don't merge 0 please";

    for (IntIterator it = x.intIterator(); it.hasNext();) {
      int i = it.next();
      if (i != 0) {
        IntSet supersOfI = subsetCache.getKnownTrue(i);
        if (supersOfI != null && x.containsAny(supersOfI)) {
          // we've determined that x contains some other integer
          // k, and i is a subset of k. so, don't bother to merge with
          // i, since merging with k will happen later
          continue;
        }
        if (merged.contains(i, j) || merged.contains(j, i)) {
          // j was generated as a merge involving i, or vice versa.
          // don't bother merging again .. you're sure to get the same answer
          // (j)
          continue;
        }
        testSubset(i, j);
        testSubset(j, i);
        if (subsetCache.isTrue(i, j)) {
          // t_i DFA is a subset of t_j dfa! merge i into j!!
          AbstractHistory t_i = getAbstractHistory(i);
          AbstractHistory t_j = getAbstractHistory(j);
          AbstractHistory t_new = (AbstractHistory) t_j.clone();
          t_new.addCurrentStates(t_i.getCurrentStates());
          BaseFactoid f_j = (BaseFactoid) domain.getMappedObject(j);

          int newJ = domain.getIndexForStateDelta(f_j, t_new);
          // by construction, the new j is a superset of the old i and the old j
          subsetCache.recordTrue(i, newJ);
          subsetCache.recordTrue(j, newJ);
          merged.add(i, newJ);
          merged.add(j, newJ);

          // update j .. it now represents the merged factoid, which is a
          // candidate
          // for further propagation
          j = newJ;
          if (x.contains(j)) {
            // we've already propagated j. stop merging .. the extant facts
            // should already be merged
            return j;
          }
        } else if (subsetCache.isTrue(j, i)) {
          // t_j DFA is a subset of t_i dfa! merge j into i!!
          AbstractHistory t_i = getAbstractHistory(i);
          AbstractHistory t_j = getAbstractHistory(j);
          AbstractHistory t_new = (AbstractHistory) t_i.clone();
          t_new.addCurrentStates(t_j.getCurrentStates());
          BaseFactoid f_j = (BaseFactoid) domain.getMappedObject(j);

          int newJ = domain.getIndexForStateDelta(f_j, t_new);
          // by construction, the new j is a superset of the old i and the old j
          subsetCache.recordTrue(i, newJ);
          subsetCache.recordTrue(j, newJ);
          merged.add(i, newJ);
          merged.add(j, newJ);

          // update j .. it now represents the merged factoid, which is a
          // candidate
          // for further propagation
          j = newJ;
          if (x.contains(j)) {
            // we've already propagated j. stop merging .. the extant facts
            // should already be merged
            return j;
          }
        }
      }
    }
    return j;
  }

  /**
   * Ensure that the subset cache holds the result of testing whether i is a
   * subset of j
   */
  private void testSubset(int i, int j) {

    assert i != j;

    if (subsetCache.isTrue(i, j)) {
      return;
    } else if (subsetCache.isFalse(i, j)) {
      return;
    } else {
      // System.err.println("test " + i + " " + j);
      BaseFactoid f_j = (BaseFactoid) domain.getMappedObject(j);
      AbstractHistory t_i = getAbstractHistory(i);
      AbstractHistory t_j = getAbstractHistory(j);
      // first check: are the 2 trace equal, modulo the dfa?
      // TODO: find a way to do this without creating a new index.
      int test = domain.getIndexForStateDelta(f_j, t_i);
      if (test == i) {
        if (isSubset(t_i.getDfa(), t_j.getDfa())) {
          subsetCache.recordTrue(i, j);
          // since these two factoids are the same in every aspect except the
          // dfa,
          // they cannot be equals, since i != j. So the subset relation (j,i)
          // cannot hold
          subsetCache.recordFalse(j, i);
        } else {
          subsetCache.recordFalse(i, j);
          subsetCache.recordTrue(j, i);
        }
      } else {
        // the factoids differ in state unrelated to the dfa. they are
        // incomparable
        subsetCache.recordFalse(i, j);
      }
    }
  }

  /**
   * is a a subset of b?
   */
  private boolean isSubset(IDFA a, IDFA b) {
    for (Iterator it = a.iterator(); it.hasNext();) {
      Object x = it.next();
      if (!b.containsNode(x))
        return false;
      for (Iterator it2 = a.getSuccNodes(x); it2.hasNext();) {
        Object y = it2.next();
        if (!b.containsNode(y))
          return false;
        // TODO: Steve, this is an awfully expensive operation now [EY]
        if (!a.getLabels(x, y).equals(b.getLabels(x, y))) {
          return false;
        }
      }
    }
    return true;
  }

  private AbstractHistory getAbstractHistory(int j) {
    BaseFactoid f_j = (BaseFactoid) domain.getMappedObject(j);
    return (AbstractHistory) f_j.state;
  }

  public static IMergeFunctionFactory factory() {
    return new IMergeFunctionFactory() {
      public IMergeFunction create(MutableMapping domain) {
        assert (domain instanceof TypeStateDomain);
        return new LossLessMerge((TypeStateDomain) domain);
      }

    };
  }

}