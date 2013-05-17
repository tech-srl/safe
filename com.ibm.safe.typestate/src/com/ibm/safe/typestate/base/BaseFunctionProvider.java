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
package com.ibm.safe.typestate.base;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.core.TypeStateFunctionProvider;
import com.ibm.safe.typestate.core.TypeStatePropertyContext;
import com.ibm.safe.typestate.core.UniversalKillFlowFunction;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.mine.TracingProperty;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.classLoader.IClass;
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
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * @author Eran Yahav (yahave)
 * @author Stephen Fink 
 */
public class BaseFunctionProvider extends TypeStateFunctionProvider {

  public BaseFunctionProvider(CallGraph cg, PointerAnalysis pointerAnalysis, ICFGSupergraph supergraph, TypeStateDomain domain,
      ITypeStateDFA dfa, Collection<InstanceKey> trackedInstances, ILiveObjectAnalysis live, TraceReporter traceReporter) {
    super(cg, domain, dfa, supergraph, pointerAnalysis, trackedInstances, live, traceReporter);
  }

  /**
   * Create a new flow function to handle dataflow at a particular call
   * instruction. Subclasses should override this method as desired.
   * 
   * @param relevantInstances
   *          set of receivers to the call
   * @param event
   *          the event which occurs as a result of executing this call.
   * @param srcBlock
   *          basic block of the call instruction
   * @param srcInvokeInstr
   *          the call instruction
   * @param caller
   *          CGNode of the call instruction
   * @return a flow function object which encapsulates the dataflow logic for a
   *         particular call edge
   */
  protected IReversibleFlowFunction makeNonEntryCallFunction(OrdinalSet<InstanceKey> relevantInstances, IEvent event,
      BasicBlockInContext<IExplodedBasicBlock> srcBlock, SSAInvokeInstruction srcInvokeInstr, CGNode caller, CGNode callee, BasicBlockInContext<IExplodedBasicBlock> destBlock) {
    return new BaseCallFlowFunction(getDomain(), getDFA(), relevantInstances, event, srcBlock, srcInvokeInstr, caller);
  }

  /**
   * Create a new flow function to handle dataflow representing a particular
   * allocation site. Subclasses should override this method as desired.
   * 
   * @param relevantInstances
   *          set of receivers to the call
   */
  protected IReversibleFlowFunction makeAllocationFlowFunction(OrdinalSet<InstanceKey> relevantInstances) {
    return new BaseAllocationFlowFunction(getDomain(), relevantInstances);
  }

  /**
   * A normal intraprocedural edge may cause a transition to the initial state
   * if it allocates an object
   * 
   * @see com.ibm.wala.dataflow.IFDS.IFlowFunctionMap#getNormalFlowFunction(java.lang.Object,
   *      java.lang.Object)
   */
  public IUnaryFlowFunction makeNormalFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest) {

    if (DEBUG_LEVEL > 1) {
      Trace.println("**Edge:" + src + " to " + dest);
    }
    OrdinalSet<InstanceKey> affectedInstances = getInstancesAllocated(src);
    if (affectedInstances.size() > 0) {
      return makeAllocationFlowFunction(affectedInstances);
    } else {
      return IdentityFlowFunction.identity();
    }
  }

  protected OrdinalSet<InstanceKey> getInstancesAllocated(BasicBlockInContext<IExplodedBasicBlock> src) {

    Collection<InstanceKey> allocKeys = getInstanceKeysAllocatedInBasicBlock(getSupergraph(), getCallGraph().getClassHierarchy(),
        getCallGraph(), getDFA(), getPointerAnalysis(), src);
    MutableSparseIntSet affected = MutableSparseIntSet.makeEmpty();
    for (Iterator<InstanceKey> it = getTrackedInstanceSet().iterator(); it.hasNext();) {
      InstanceKey ik = it.next();
      if (allocKeys.contains(ik)) {
        affected.add(getPointerAnalysis().getInstanceKeyMapping().getMappedIndex(ik));
      }
    }
    OrdinalSet<InstanceKey> affectedInstances = new OrdinalSet<InstanceKey>(affected, getPointerAnalysis().getInstanceKeyMapping());
    return affectedInstances;
  }

  protected Collection<InstanceKey> getInstanceKeysAllocatedInBasicBlock(final ICFGSupergraph supergraph,
      final IClassHierarchy classHierarchy, final CallGraph callGraph, final ITypeStateDFA dfa,
      final PointerAnalysis pointerAnalysis, final BasicBlockInContext<IExplodedBasicBlock> bb) {
    CGNode n = (CGNode) supergraph.getProcOf(bb);
    IR ir = n.getIR();
    final HashSet<InstanceKey> result = HashSetFactory.make(5);

    SSAInstruction[] statements = ir.getInstructions();

    final class Visitor extends SSAInstruction.Visitor {
      public void visitNew(SSANewInstruction instruction) {
        TypeReference allocatedType = instruction.getConcreteType();
        // does the type match the property?
        IClass theClass = classHierarchy.lookupClass(allocatedType);
        if (theClass == null) {
          Trace.println("Allocated type not found: " + allocatedType);
        }
        if (theClass != null && TypeStatePropertyContext.isTrackedType(classHierarchy, dfa.getTypes(), theClass)) {
          final PointerKey pLocalKey = pointerAnalysis.getHeapModel().getPointerKeyForLocal((CGNode) supergraph.getProcOf(bb),
              instruction.getDef());
          for (Iterator<InstanceKey> iter = pointerAnalysis.getPointsToSet(pLocalKey).iterator(); iter.hasNext();) {
            result.add(iter.next());
          }
        }
      }
    }
    Visitor v = new Visitor();
    int first = bb.getFirstInstructionIndex();
    int last = bb.getLastInstructionIndex();
    if (first >=0 && last >=0) {
      for (int i = first; i <= last; i++) {
        SSAInstruction s = statements[i];
        if (s != null) {
          s.visit(v);
        }
      }  
    }
    return result;
  }

  /**
   * @param src
   * @param dest
   * @return the flow function for a call in the supergraph, which does NOT
   *         represent a call to an entrypoint.
   */
  @SuppressWarnings("unused")
  protected IUnaryFlowFunction getNonEntryCallFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest) {
    IReversibleFlowFunction resultFlowFunction = IdentityFlowFunction.identity();

    CGNode callee = (CGNode) getSupergraph().getProcOf(dest);

    if (DEBUG_LEVEL > 1) {
      Trace.println("-----------------");
      Trace.println("| Source: " + src);
      Trace.println("| Dest: " + dest);
    }

    IEvent event = getEventForNode(callee);
    if (event != null && getDFA() instanceof TracingProperty) {
      // a hack: the tracing property may filter events based on the
      // caller. check this. we don't do this in the normal verifier
      // because the following is slow.
      CGNode caller = (CGNode) getSupergraph().getProcOf(src);
      event = getDFA().matchDispatchEvent(caller, callee.getMethod().getSignature());
    }
    if (event != null) {
      SSAInvokeInstruction srcInvokeInstr = TypeStateFunctionProvider.getLastCallInstruction(getCFG(src), src);
      if (srcInvokeInstr.isStatic()) {
        // TODO: do something for static calls
      } else {
        int rcv = srcInvokeInstr.getReceiver();
        CGNode caller = (CGNode) getSupergraph().getProcOf(src);
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
          resultFlowFunction = IdentityFlowFunction.identity();
        } else {
          resultFlowFunction = makeNonEntryCallFunction(relevantInstances, event, src, srcInvokeInstr, caller, callee, dest);
        }
      }
    }
    return resultFlowFunction;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.TypeStateFunctionProvider#getProgramExitFlowFunction
   * (java.lang.Object, java.lang.Object)
   */
  public IUnaryFlowFunction getProgramExitFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest) {
    if (DEBUG_LEVEL > 1) {
      Trace.println("Base:getProgramExitFlowFunction");  
    }
    if (!getDFA().observesProgramExit()) {
      return UniversalKillFlowFunction.kill();
    }
    // SSAInvokeInstruction srcInvokeInstr =
    // SafeDomoUtils.getLastCallInstruction(getUncollapsed(), srcBlock);
    SSAInvokeInstruction srcInvokeInstr = null;
    CGNode caller = (CGNode) getSupergraph().getProcOf(src);

    return new BaseProgramExitFlowFunction(getDomain(), getDFA(), src, srcInvokeInstr, caller, getTraceReporter());
  }

}