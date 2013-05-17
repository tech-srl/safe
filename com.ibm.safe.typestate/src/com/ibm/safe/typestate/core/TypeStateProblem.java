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
package com.ibm.safe.typestate.core;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.solvers.ICFGTabulationProblem;
import com.ibm.wala.dataflow.IFDS.IFlowFunctionMap;
import com.ibm.wala.dataflow.IFDS.IMergeFunction;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;

/**
 * common base class for all typestate problems
 * @author Eran Yahav (yahave)
 */
public class TypeStateProblem extends ICFGTabulationProblem {

  public TypeStateDomain getDomain() {
    return (TypeStateDomain) domain;
  }

  public TypeStateProblem(ICFGSupergraph supergraph, TypeStateDomain domain,
      IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> functions, IMergeFunction merge) {
    super(supergraph, domain, functions, merge);
  }
}
