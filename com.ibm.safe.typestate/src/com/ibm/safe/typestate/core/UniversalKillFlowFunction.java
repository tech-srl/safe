/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.typestate.core;

import com.ibm.wala.dataflow.IFDS.IReversibleFlowFunction;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * A flow function where out == in
 */
public class UniversalKillFlowFunction implements IReversibleFlowFunction {

  private final static UniversalKillFlowFunction singleton = new UniversalKillFlowFunction();

  public SparseIntSet getTargets(int i) {
    return null;
  }

  public SparseIntSet getSources(int i) {
    return null;
  }

  public static UniversalKillFlowFunction kill() {
    return singleton;
  }

  @Override
  public String toString() {
    return "UniversalKillFlowFunction Flow";
  }

}