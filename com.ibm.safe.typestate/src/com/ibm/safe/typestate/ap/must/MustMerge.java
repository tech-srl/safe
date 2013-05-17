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

package com.ibm.safe.typestate.ap.must;

import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.quad.QuadFactoid;
import com.ibm.safe.typestate.quad.QuadTypeStateDomain;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.dataflow.IFDS.IMergeFunction;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableMapping;

/**
 * A lossless merge function for APMust factoids
 * 
 * @author sfink
 * @author yahave
 */
public class MustMerge implements IMergeFunction {

  private final static boolean DEBUG = false;

  private final QuadTypeStateDomain domain;

  protected MustMerge(QuadTypeStateDomain domain) {
    this.domain = domain;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.wala.dataflow.IFDS.IMergeFunction#merge(com.ibm.capa.util.intset
   * .IntSet, int)
   */
  public int merge(IntSet x, int j) {
    if (DEBUG) {
      System.err.println("Merge " + x + " " + j);
      Trace.println("Merge " + x + " " + j);
    }

    if (j == 0) {
      return j;
    }

    QuadFactoid f_j = (QuadFactoid) domain.getMappedObject(j);

    for (IntIterator it = x.intIterator(); it.hasNext();) {
      int i = it.next();
      if (i != 0 && i != j) {
        QuadFactoid f_i = (QuadFactoid) domain.getMappedObject(i);
        if (isWeakerThan(f_i, f_j)) {
          if (DEBUG) {
            System.err.println("kill " + j + " for " + i);
            Trace.println("kill " + j + " for " + i);
          }
          j = i;
          f_j = f_i;
        }
      }
    }
    return j;
  }

  /**
   * TODO: this is a quick and dirty hack. do a better job. is f_i strictly
   * weaker than f_j?
   */
  public static boolean isWeaker(QuadFactoid f_i, QuadFactoid f_j) {
    MustAuxiliary aux_i = (MustAuxiliary) f_i.aux;
    MustAuxiliary aux_j = (MustAuxiliary) f_j.aux;
    if (f_i.state.equals(f_j.state) && f_i.instance.equals(f_j.instance)) {
      if (!aux_i.isComplete() || aux_j.isComplete()) {
        if (!f_i.isUnique() || f_j.isUnique()) {
          if (aux_j.getMustPaths().containsAll(aux_i.getMustPaths())) {
            // aux_j holds more information than aux_i!!
            return true;
          }
        }
      }
    }
    // give up. TODO add more smarts.
    return false;
  }

  /**
   * TODO: this is a quick and dirty hack. do a better job. is f_i strictly
   * weaker than f_j?
   */
  public boolean isWeakerThan(QuadFactoid f_i, QuadFactoid f_j) {
    return isWeaker(f_i, f_j);
  }

  public static IMergeFunctionFactory factory() {
    return new IMergeFunctionFactory() {
      public IMergeFunction create(MutableMapping domain) {
        assert (domain instanceof QuadTypeStateDomain);
        return new MustMerge((QuadTypeStateDomain) domain);
      }
    };
  }

  /**
   * @return Returns the domain.
   */
  protected QuadTypeStateDomain getDomain() {
    return domain;
  }

}
