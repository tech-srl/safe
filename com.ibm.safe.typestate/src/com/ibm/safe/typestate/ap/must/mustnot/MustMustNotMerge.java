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

package com.ibm.safe.typestate.ap.must.mustnot;

import com.ibm.safe.typestate.ap.must.MustMerge;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.quad.QuadFactoid;
import com.ibm.safe.typestate.quad.QuadTypeStateDomain;
import com.ibm.wala.dataflow.IFDS.IMergeFunction;
import com.ibm.wala.util.intset.MutableMapping;

/**
 * A lossless merge function for APMustMustNot factoids
 * 
 * @author sfink, yahave
 */
public class MustMustNotMerge extends MustMerge {

  private MustMustNotMerge(QuadTypeStateDomain domain) {
    super(domain);
  }

  /**
   * TODO: this is a quick and dirty hack. do a better job. is f_i strictly
   * weaker than f_j?
   */
  public static boolean isWeaker(QuadFactoid f_i, QuadFactoid f_j) {
    MustMustNotAuxiliary aux_i = (MustMustNotAuxiliary) f_i.aux;
    MustMustNotAuxiliary aux_j = (MustMustNotAuxiliary) f_j.aux;
    if (f_i.state.equals(f_j.state) && f_i.instance.equals(f_j.instance)) {
      if (!aux_i.isComplete() || aux_j.isComplete()) {
        if (!f_i.isUnique() || f_j.isUnique()) {
          if (aux_j.getMustPaths().containsAll(aux_i.getMustPaths())) {
            if (aux_j.getMustNotPaths().containsAll(aux_i.getMustNotPaths())) {
              // aux_j holds more information than aux_i!!
              return true;
            }
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
        return new MustMustNotMerge((QuadTypeStateDomain) domain);
      }
    };
  }

}
