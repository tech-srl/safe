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
package com.ibm.safe.typestate.merge;

import com.ibm.safe.dfa.IDFAStateFactory;
import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.mine.AbstractHistory;
import com.ibm.safe.typestate.mine.EventNameStateFactory;
import com.ibm.wala.dataflow.IFDS.IMergeFunction;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.IntegerUnionFind;
import com.ibm.wala.util.intset.SimpleIntVector;

/**
 * @author sfink
 * @author yahave
 * 
 * Base class for merge functions that rely on unification.
 */
public abstract class AbstractUnification implements IMergeFunction {

  private final TypeStateDomain domain;

  protected IDFAStateFactory stateFactory;

  /**
   * each factoid has a representative
   */
  protected final IntegerUnionFind uf = new IntegerUnionFind();

  /**
   * The union-find chooses an arbitary integer as the representative for each
   * equivalence class. For each representive, this vector holds the "actual"
   * representative we want.
   */
  final SimpleIntVector rep2Last = new SimpleIntVector(-1);

  protected AbstractUnification(TypeStateDomain domain) {
    this.domain = domain;
    stateFactory = EventNameStateFactory.getInstance();
  }

  protected final int getRealRepresentative(int x) {
    int rep = uf.find(x);
    rep = rep2Last.get(x);
    return (rep == -1) ? x : rep;
  }

  protected final BaseFactoid getRepresentativeFactoid(int x) {
    int rep = uf.find(x);
    rep = rep2Last.get(x);
    return (rep == -1) ? (BaseFactoid) domain.getMappedObject(x) : (BaseFactoid) domain.getMappedObject(rep);
  }

  /**
   * @return Returns the domain.
   */
  protected TypeStateDomain getDomain() {
    return domain;
  }

  /**
   * @param reps
   *            set of representative factoid numbers to unify
   * @param jrep
   *            number of the representative of the "new" (non-preexisting)
   *            factoid
   * @return the number of the new, unified, factoid.
   */
  protected int unify(IntSet reps, int jrep) {
    assert jrep != 0; 
    if (reps.size() == 1) {
      // no merge is necessary
      return getRealRepresentative(jrep);
    } else {
      // a merge is necessary
      AbstractHistory t_jrep = (AbstractHistory) getRepresentativeFactoid(jrep).state;
      AbstractHistory t_merged = (AbstractHistory) t_jrep.clone();
      for (IntIterator it = reps.intIterator(); it.hasNext();) {
        int i = it.next();
        if (i != jrep) {
          AbstractHistory t_i = (AbstractHistory) getRepresentativeFactoid(i).state;
          t_merged.extend(t_i);
          uf.union(i, jrep);
        }
      }
      BaseFactoid f_j = getRepresentativeFactoid(jrep);
      int newJ = getDomain().getIndexForStateDelta(f_j, t_merged);
      uf.union(jrep, newJ);
      int newRep = uf.find(newJ);
      rep2Last.set(newRep, newJ);
      return newJ;
    }
  }

  public IDFAStateFactory getStateFactory() {
    return stateFactory;
  }

}
