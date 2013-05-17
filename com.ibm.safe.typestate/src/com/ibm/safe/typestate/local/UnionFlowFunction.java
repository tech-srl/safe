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
package com.ibm.safe.typestate.local;

import com.ibm.wala.dataflow.IFDS.IFlowFunction;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.dataflow.IFDS.IdentityFlowFunction;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * @author sfink
 * @author yahave
 * 
 * a flow function which performs the join (union) of two others.
 */
public class UnionFlowFunction implements IUnaryFlowFunction {

  private IUnaryFlowFunction f1;

  private IUnaryFlowFunction f2;

  private UnionFlowFunction(IUnaryFlowFunction f1, IUnaryFlowFunction f2) {
    this.f1 = f1;
    this.f2 = f2;
  }

  public SparseIntSet getTargets(int d1) {
    MutableSparseIntSet result = MutableSparseIntSet.make(f1.getTargets(d1));
    result.addAll(f2.getTargets(d1));
    return result;
  }

  public static IUnaryFlowFunction union(IUnaryFlowFunction f1, IUnaryFlowFunction f2) {
    IFlowFunction id = IdentityFlowFunction.identity();
    if (id.equals(f1)) {
      return f2;
    } else if (id.equals(f2)) {
      return f1;
    } else if (f1.equals(f2)) {
      return f1;
    } else {
      return new UnionFlowFunction(f1, f2);
    }
  }

}
