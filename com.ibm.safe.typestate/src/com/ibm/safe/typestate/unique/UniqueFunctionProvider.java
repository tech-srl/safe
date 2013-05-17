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
package com.ibm.safe.typestate.unique;

import java.util.Collection;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.typestate.base.BaseAllocationFlowFunction;
import com.ibm.safe.typestate.base.BaseFunctionProvider;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.core.UniversalKillFlowFunction;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.dataflow.IFDS.IFlowFunction;
import com.ibm.wala.dataflow.IFDS.IReversibleFlowFunction;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.FakeRootMethod;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public class UniqueFunctionProvider extends BaseFunctionProvider {

  /**
   * if non-null, provides information to enhance the possibility of strong
   * updates.
   */
  private final HeapGraph hg;

  public UniqueFunctionProvider(CallGraph cg, PointerAnalysis pointerAnalysis, ICFGSupergraph supergraph, TypeStateDomain domain,
      ITypeStateDFA dfa, Collection<InstanceKey> trackedInstances, ILiveObjectAnalysis liveAnalysis, TraceReporter traceReporter) {
    super(cg, pointerAnalysis, supergraph, domain, dfa, trackedInstances, liveAnalysis, traceReporter);
    this.hg = pointerAnalysis.getHeapGraph();
  }

  /**
   * @return getDomain() casted to a UniqueTypeStateDomain
   */
  UniqueTypeStateDomain getUniqueDomain() {
    return (UniqueTypeStateDomain) getDomain();
  }

  /**
   * Create a new flow function to handle dataflow representing a particular
   * allocation site. Subclasses should override this method as desired.
   * 
   * @param relevantInstances
   *          set of receivers to the call
   */
  protected IReversibleFlowFunction makeAllocationFlowFunction(OrdinalSet<InstanceKey> relevantInstances) {
    return new UniqueAllocFlowFunction(relevantInstances);
  }

  /**
   * @author eyahav
   * 
   *         A flow function which only modifies the count information for a
   *         unique factoid, based on the allocations in the source basic block.
   */
  public class UniqueAllocFlowFunction extends BaseAllocationFlowFunction {

    public UniqueAllocFlowFunction(OrdinalSet<InstanceKey> instances) {
      super(getDomain(), instances);
    }

    /**
     * @param d1
     *          = integer corresponding to an (instance, state,aux) tuple
     * @return set of d2 such that (d1,d2) is an edge in this distributive
     *         function's graph representation, or null if there are none
     */
    public SparseIntSet getTargets(int d1) {

      MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
      result.add(0);

      if (d1 == 0) {
        // delegate to get the initial states
        return super.getTargets(d1);
      } else {
        UniqueFactoid f = (UniqueFactoid) getDomain().getMappedObject(d1);
        if (getInstances().contains(f.instance)) {
          // we're either in count == 1 or count == 2. either way, we move to
          // state 2.
          int newStateIndex = getUniqueDomain().add(new UniqueFactoid(f.instance, f.state, false));
          result.add(newStateIndex);
        } else {
          result.add(d1);
        }
        return result;
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.wala.dataflow.IFDS.IReversibleFlowFunction#getSources(int)
     */
    public SparseIntSet getSources(int d2) {

      if (d2 == 0) {
        return SparseIntSet.singleton(0);
      }
      UniqueFactoid triplet = (UniqueFactoid) getDomain().getMappedObject(d2);

      if (getInstances().contains(triplet.instance)) {
        if (triplet.isUnique()) {
          // initial allocation. only get here from universal fact.
          return SparseIntSet.singleton(0);
        } else {
          MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
          result.add(d2);
          result.add(getUniqueDomain().add(new UniqueFactoid(triplet.instance, triplet.state, true)));
          return result;
        }
      } else {
        return SparseIntSet.singleton(d2);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.base.BaseSafeFunctionProvider#makeNonEntryCallFunction
   * (com.ibm.capa.util.intset.OrdinalSet, com.ibm.safe.emf.typestate.IEvent,
   * com.ibm.wala.cfg.IBasicBlock, com.ibm.wala.ssa.SSAInvokeInstruction,
   * com.ibm.wala.ipa.callgraph.CGNode)
   */
  protected IReversibleFlowFunction makeNonEntryCallFunction(OrdinalSet<InstanceKey> relevantInstances, IEvent event,
      BasicBlockInContext<IExplodedBasicBlock> srcBlock, SSAInvokeInstruction srcInvokeInstr, CGNode caller, CGNode callee,
      BasicBlockInContext<IExplodedBasicBlock> destBlock) {
    return new UniqueCallFlowFunction(getDomain(), getDFA(), relevantInstances, event, srcBlock, srcInvokeInstr, caller, callee,
        getPointerAnalysis(), getCallGraph().getClassHierarchy());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.TypeStateFunctionProvider#getNonExitReturnFlowFunction
   * (java.lang.Object, java.lang.Object, java.lang.Object)
   */
  public IFlowFunction getNonExitReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> call,
      BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest) {

    if (getLiveObjectAnalysis() == null) {
      return super.getNonExitReturnFlowFunction(call, src, dest);
    } else {
      if (FakeRootMethod.isFromFakeRoot(dest)) {
        return UniversalKillFlowFunction.kill();
      } else {
        CGNode node = (CGNode) getSupergraph().getProcOf(dest);
        SSAInvokeInstruction callS = getInvokeInstruction(call);
        return new UniqueReturnFlowFunction(getUniqueDomain(), getTrackedInstanceSet(), getLiveObjectAnalysis(), node, hg,
            callS.getCallSite(), node.getIR());
      }
    }
  }
}