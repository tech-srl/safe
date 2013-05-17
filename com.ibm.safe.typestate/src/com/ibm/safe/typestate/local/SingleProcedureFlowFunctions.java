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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.typestate.core.TypeStateFunctionProvider;
import com.ibm.safe.typestate.quad.AggregateFlowFunction;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.dataflow.IFDS.IFlowFunction;
import com.ibm.wala.dataflow.IFDS.IFlowFunctionMap;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.dataflow.IFDS.IdentityFlowFunction;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;

/**
 * @author sfink
 * @author yahave
 */
public abstract class SingleProcedureFlowFunctions implements IFlowFunctionMap<BasicBlockInContext<IExplodedBasicBlock>> {

  private final CGNode node;

  private final ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> cfg;

  private final TypeStateFunctionProvider delegate;

  /**
   * Nodes that might affect dataflow for this problem
   */
  private final Collection<CGNode> nodesThatMatter;

  /**
   * Instances being tracked
   */
  private final Collection<InstanceKey> instances;

  private final ILiveObjectAnalysis live;

  private final CallGraph cg;

  /**
   * @param delegate
   */
  public SingleProcedureFlowFunctions(CGNode node, ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> cfg,
      TypeStateFunctionProvider delegate, Collection<CGNode> nodesThatMatter, Collection<InstanceKey> instances,
      ILiveObjectAnalysis live, CallGraph cg) {
    this.node = node;
    this.cfg = cfg;
    this.delegate = delegate;
    this.nodesThatMatter = nodesThatMatter;
    this.instances = instances;
    this.live = live;
    this.cg = cg;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.wala.dataflow.IFDS.IFlowFunctionMap#getNormalFlowFunction(java.
   * lang.Object, java.lang.Object)
   */
  public IUnaryFlowFunction getNormalFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest) {
    if (src.getDelegate().equals(cfg.entry())) {
      if (anyInstanceMayBeLiveInCaller()) {
        return AggregateFlowFunction.compose(makeWorstCaseFlowFunction(), delegate.getNormalFlowFunction(src, dest));
      } else {
        return delegate.getNormalFlowFunction(src, dest);
      }
    } else {
      return delegate.getNormalFlowFunction(src, dest);
    }
  }

  /**
   * Is any instance we care about potentially live in a caller to this node?
   */
  private boolean anyInstanceMayBeLiveInCaller() {
    try {
      for (Iterator<InstanceKey> it = instances.iterator(); it.hasNext();) {
        InstanceKey ik = it.next();
        if (!(ik instanceof AllocationSiteInNode)) {
          // give up
          return true;
        }
        AllocationSiteInNode ak = (AllocationSiteInNode) ik;
        for (Iterator<? extends CGNode> it2 = delegate.getCallGraph().getPredNodes(node); it2.hasNext();) {
          CGNode p = (CGNode) it2.next();
          if (live.mayBeLive(ak, p, -1)) {
            return true;
          }
        }
      }
      return false;
    } catch (WalaException e) {
      // uh oh. shouldn't happen.
      e.printStackTrace();
      return true;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.wala.dataflow.IFDS.IFlowFunctionMap#getCallFlowFunction(java.lang
   * .Object, java.lang.Object)
   */
  public IUnaryFlowFunction getCallFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest) {
    Assertions.UNREACHABLE();
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.wala.dataflow.IFDS.IFlowFunctionMap#getReturnFlowFunction(java.
   * lang.Object, java.lang.Object, java.lang.Object)
   */
  public IFlowFunction getReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> call,
      BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest) {
    Assertions.UNREACHABLE();
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.wala.dataflow.IFDS.IFlowFunctionMap#getCallToReturnFlowFunction
   * (java.lang.Object, java.lang.Object)
   */
  public IUnaryFlowFunction getCallToReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest) {
    Assertions.UNREACHABLE();
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.wala.dataflow.IFDS.IFlowFunctionMap#getCallNoneToReturnFlowFunction
   * (java.lang.Object, java.lang.Object)
   */
  public IUnaryFlowFunction getCallNoneToReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest) {
    if (hasCalleeThatMatters(src)) {
      return makeCallFlow(src);
    } else {
      // the callee doesn't change anything
      return IdentityFlowFunction.identity();
    }
  }

  private IUnaryFlowFunction makeCallFlow(BasicBlockInContext<IExplodedBasicBlock> call) {
    Collection<CGNode> callees = getCalleesThatMatter(call);

    assert (callees.size() > 0);

    IUnaryFlowFunction result = null;
    // compute the flow function for each callee individually, and
    // let the final result be the join (union) of these individual flows
    for (Iterator<CGNode> it = callees.iterator(); it.hasNext();) {
      CGNode callee = it.next();
      IEvent event = delegate.getEventForNode(callee);
      if (event == null) {
        IUnaryFlowFunction f = makeWorstCaseFlowFunction();
        result = (result == null) ? f : UnionFlowFunction.union(result, f);
      } else {
        IUnaryFlowFunction f = makeEventFlowFunction(event, call, callee);
        result = (result == null) ? f : UnionFlowFunction.union(result, f);
      }
    }
    if (hasCalleeThatDoesNotMatter(call)) {
      result = UnionFlowFunction.union(result, IdentityFlowFunction.identity());
    }
    return result;
  }

  /**
   * @param event
   * @returna flow function which models the behavior of a callee which forces a
   *          typestate transition
   */
  protected abstract IUnaryFlowFunction makeEventFlowFunction(IEvent event, BasicBlockInContext<IExplodedBasicBlock> block,
      CGNode callee);

  /**
   * @return a flow function which models the behavior of an unknown callee.
   */
  protected abstract IUnaryFlowFunction makeWorstCaseFlowFunction();

  private Collection<CGNode> getCalleesThatMatter(BasicBlockInContext<IExplodedBasicBlock> call) {
    SSAInvokeInstruction s = TypeStateFunctionProvider.getLastCallInstruction(cfg, call);
    HashSet<CGNode> result = HashSetFactory.make(3);
    for (Iterator<CGNode> it = cg.getPossibleTargets(node, s.getCallSite()).iterator(); it.hasNext();) {
      CGNode n = it.next();
      if (nodesThatMatter.contains(n)) {
        result.add(n);
      }
    }
    return result;
  }

  private boolean hasCalleeThatMatters(BasicBlockInContext<IExplodedBasicBlock> callBlock) {
    SSAInvokeInstruction s = TypeStateFunctionProvider.getLastCallInstruction(cfg, callBlock);
    for (Iterator<CGNode> it = cg.getPossibleTargets(node, s.getCallSite()).iterator(); it.hasNext();) {
      if (nodesThatMatter.contains(it.next())) {
        return true;
      }
    }
    return false;
  }

  private boolean hasCalleeThatDoesNotMatter(BasicBlockInContext<IExplodedBasicBlock> callBlock) {
    SSAInvokeInstruction s = TypeStateFunctionProvider.getLastCallInstruction(cfg, callBlock);
    for (Iterator<CGNode> it = cg.getPossibleTargets(node, s.getCallSite()).iterator(); it.hasNext();) {
      if (!nodesThatMatter.contains(it.next())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return Returns the delegate.
   */
  protected TypeStateFunctionProvider getDelegate() {
    return delegate;
  }

  /**
   * @return Returns the instances.
   */
  protected Collection<InstanceKey> getInstances() {
    return instances;
  }

  /**
   * @return Returns the node.
   */
  protected CGNode getNode() {
    return node;
  }

  /**
   * @return Returns the cfg.
   */
  protected ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> getCfg() {
    return cfg;
  }
}
