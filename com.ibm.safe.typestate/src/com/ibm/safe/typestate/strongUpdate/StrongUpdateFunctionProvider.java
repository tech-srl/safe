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
package com.ibm.safe.typestate.strongUpdate;

import java.util.Collection;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.typestate.base.BaseFunctionProvider;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.core.TypeStateFunctionProvider;
import com.ibm.safe.typestate.core.UniversalKillFlowFunction;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.dataflow.IFDS.IReversibleFlowFunction;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.dataflow.IFDS.IdentityFlowFunction;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * An extension of the base (actually separating) typestate solver that
 * unsoundly always performs strong updates.
 * 
 * @author Stephen Fink
 * @author eyahav
 */
public class StrongUpdateFunctionProvider extends BaseFunctionProvider {

  public StrongUpdateFunctionProvider(CallGraph cg, PointerAnalysis pointerAnalysis, ICFGSupergraph supergraph,
      TypeStateDomain domain, ITypeStateDFA dfa, Collection<InstanceKey> trackedInstances, ILiveObjectAnalysis live,
      TraceReporter traceReporter) {
    super(cg, pointerAnalysis, supergraph, domain, dfa, trackedInstances, live, traceReporter);
  }

  protected IReversibleFlowFunction makeNonEntryCallFunction(OrdinalSet<InstanceKey> relevantInstances, IEvent event,
      BasicBlockInContext<IExplodedBasicBlock> srcBlock, SSAInvokeInstruction srcInvokeInstr, CGNode caller, CGNode callee, BasicBlockInContext<IExplodedBasicBlock> destBlock) {
    return new StrongUpdateCallFlowFunction(getDomain(), getDFA(), relevantInstances, event, srcBlock, srcInvokeInstr, caller);
  }

  /**
   * @param src
   * @param dest
   * @return the flow function for a call in the supergraph, which does NOT
   *         represent a call to an entrypoint.
   */
  @SuppressWarnings("unused")
  protected IUnaryFlowFunction getNonEntryCallFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest) {
    IReversibleFlowFunction resultFlowFunction = IdentityFlowFunction.identity();

    CGNode callee = (CGNode) getSupergraph().getProcOf(dest);

    if (DEBUG_LEVEL > 1) {
      Trace.println("-----------------");
      Trace.println("| Source: " + src);
      Trace.println("| Dest: " + dest);
    }

    // note that we check if there might be an event on ANY interesting object
    // here!
    CGNode caller = (CGNode) getSupergraph().getProcOf(src);
    IEvent event = getDFA().matchDispatchEvent(caller, callee.getMethod().getSignature());

    if (event != null) {
      SSAInvokeInstruction srcInvokeInstr = TypeStateFunctionProvider.getLastCallInstruction(getCFG(src), src);
      if (srcInvokeInstr.isStatic()) {
        // TODO: do something for static calls
      } else {
        int rcv = srcInvokeInstr.getReceiver();
        PointerKey p = getPointerAnalysis().getHeapModel().getPointerKeyForLocal(caller, rcv);

        if (DEBUG_LEVEL > 1) {
          Trace.println("| InvokeInstruction: " + srcInvokeInstr);
          Trace.println("| PointerKey used: " + p);
        }

        OrdinalSet<InstanceKey> pointsTo = getPointerAnalysis().getPointsToSet(p);
        OrdinalSet<InstanceKey> relevantInstances = OrdinalSet.intersect(pointsTo, getTrackedInstanceSet());

        if (DEBUG_LEVEL > 1) {
          Trace.println("-----------------");
        }
        if (relevantInstances.size() == 0) {
          // this call does not apply to any interesting instances. ignore it.
          // in fact, pretend it doesn't happen!
          // this is extremely unsound: we're saying that if a receiver may
          // alias, then it must alias.
          resultFlowFunction = UniversalKillFlowFunction.kill();
        } else {
          resultFlowFunction = makeNonEntryCallFunction(relevantInstances, event, src, srcInvokeInstr, caller, callee, dest);
        }
      }
    }
    return resultFlowFunction;
  }
}