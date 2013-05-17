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
package com.ibm.safe.solvers;

import java.util.Collection;
import java.util.Iterator;

import com.ibm.safe.Factoid;
import com.ibm.safe.ICFGSupergraph;
import com.ibm.wala.dataflow.IFDS.IFlowFunctionMap;
import com.ibm.wala.dataflow.IFDS.IMergeFunction;
import com.ibm.wala.dataflow.IFDS.PathEdge;
import com.ibm.wala.dataflow.IFDS.TabulationDomain;
import com.ibm.wala.dataflow.IFDS.TabulationProblem;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * @author yahave
 */
public abstract class ICFGTabulationProblem implements TabulationProblem<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Factoid> {
  /**
   * governing supergraph
   */
  protected final ICFGSupergraph supergraph;

  /**
   * domain of dataflow facts
   */
  protected final TabulationDomain<Factoid, BasicBlockInContext<IExplodedBasicBlock>> domain;

  /**
   * object which provides IFDS functions
   */
  protected final IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> functions;

  protected final IMergeFunction merge;

  public ICFGTabulationProblem(ICFGSupergraph supergraph,
      TabulationDomain<Factoid, BasicBlockInContext<IExplodedBasicBlock>> domain,
      IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> functions, IMergeFunction merge) {
    this.supergraph = supergraph;
    this.domain = domain;
    this.functions = functions;
    this.merge = merge;
  }

  public ICFGSupergraph getSupergraph() {
    return supergraph;
  }

  public TabulationDomain<Factoid, BasicBlockInContext<IExplodedBasicBlock>> getDomain() {
    return domain;
  }

  public IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> getFunctionMap() {
    return functions;
  }

  public IMergeFunction getMergeFunction() {
    return merge;
  }

  public Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> initialSeeds() {
    Collection<BasicBlockInContext<IExplodedBasicBlock>> entryBlocks = HashSetFactory.make();
    Collection<CGNode> entryNodes = supergraph.getICFG().getCallGraph().getEntrypointNodes();
    for (CGNode n : entryNodes) {
      BasicBlockInContext<IExplodedBasicBlock> entry = supergraph.getEntriesForProcedure(n)[0];
      entryBlocks.add(entry);
    }
    return initialSeeds(entryBlocks);
  }

  public Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> initialSeeds(
      Collection<BasicBlockInContext<IExplodedBasicBlock>> entryPoints) {
    Collection<PathEdge<BasicBlockInContext<IExplodedBasicBlock>>> seeds = HashSetFactory.make();
    for (Iterator<BasicBlockInContext<IExplodedBasicBlock>> iter = entryPoints.iterator(); iter.hasNext();) {
      BasicBlockInContext<IExplodedBasicBlock> entryPoint = iter.next();
      // Just in case 0 was left out of reachableOnEntry:
      PathEdge<BasicBlockInContext<IExplodedBasicBlock>> pe = PathEdge.createPathEdge(entryPoint, 0, entryPoint, 0);
      seeds.add(pe);
      for (IntIterator it = getReachableOnEntry().intIterator(); it.hasNext();) {
        int i = it.next();
        if (i != 0) {
          seeds.add(PathEdge.createPathEdge(entryPoint, 0, entryPoint, i));
        }
      }
    }
    return seeds;
  }

  /**
   * Initial state of the BaseSafeProblem. In most cases, just the universal
   * fact is reachable initially. Override, if necessary.
   */
  public SparseIntSet getReachableOnEntry() {
    return SparseIntSet.singleton(0);
  }
}
