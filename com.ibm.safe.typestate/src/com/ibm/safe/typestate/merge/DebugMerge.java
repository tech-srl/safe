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

import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.dataflow.IFDS.IMergeFunction;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableMapping;

/**
 * @author yahave
 * @author sfink
 * 
 *         A merge function that simply prints some debug information
 */
public class DebugMerge implements IMergeFunction {

  private DebugMerge() {
  }

  public int merge(IntSet x, int j) {
    System.err.println("Merge " + x + " " + j);
    Trace.println("Merge " + x + " " + j);
    return j;
  }

  public static IMergeFunctionFactory factory() {
    return new IMergeFunctionFactory() {
      public IMergeFunction create(MutableMapping domain) {
        assert (domain instanceof TypeStateDomain);
        return new DebugMerge();
      }
    };
  }

}
