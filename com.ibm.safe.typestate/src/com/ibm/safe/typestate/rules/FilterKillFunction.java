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
package com.ibm.safe.typestate.rules;

import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * @author sfink
 * 
 * a flow function that kills any factoids accepted by an IntFilter
 */
public class FilterKillFunction implements IUnaryFlowFunction {

  private final IntFilter f;

  private FilterKillFunction(IntFilter f) {
    this.f = f;
  }

  public SparseIntSet getTargets(int d1) {
    return (f.accepts(d1)) ? null : SparseIntSet.singleton(d1);
  }

  public static IUnaryFlowFunction make(IntFilter kill) {
    return new FilterKillFunction(kill);
  }

}
