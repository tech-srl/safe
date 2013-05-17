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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.dfa.events.IObjectDeathEventImpl;
import com.ibm.safe.solvers.WholeProgramFunctionProvider;
import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.mine.TracingProperty;
import com.ibm.safe.typestate.quad.AggregateFlowFunction;
import com.ibm.safe.typestate.rules.FilterKillFunction;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.typestate.rules.IntFilter;
import com.ibm.safe.utils.SafeAssertions;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.dataflow.IFDS.IFlowFunction;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.dataflow.IFDS.IdentityFlowFunction;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.FakeRootMethod;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSite;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.Iterator2Collection;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.BitVectorIntSet;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 * 
 *         Abstract base class for all type state flow function providers
 */
public abstract class TypeStateFunctionProvider extends WholeProgramFunctionProvider {

  static public final int DEBUG_LEVEL = 2;

  /**
   * use liveness to kill factoids on each intraprocedural edge?
   */
  private final static boolean NORMAL_LIVE_KILLS = false;

  /**
   * DFA and other information on the type-state problem
   */
  private final ITypeStateDFA dfa;

  /**
   * a Map: CGNode -> IEvent indicates the event which happens as a result of
   * calling a particular node
   */
  protected final Map<CGNode, IEvent> node2event = HashMapFactory.make();

  /**
   * instance key indices for tracked instances
   */
  private OrdinalSet<InstanceKey> trackedInstanceSet;

  /**
   * if non-null, an analysis which helps kill factoids during return flow based
   * on liveness information
   */
  private ILiveObjectAnalysis liveObjectAnalysis;

  /**
   * If non-null, an object to record tracing information
   */
  private final TraceReporter traceReporter;

  /**
   * Does exceptional control flow kill all facts?
   */
  private final boolean exceptionalFlowKills;

  /**
   * @param cg
   *          governing call graph
   * @param domain
   *          Domain of dataflow facts
   * @param dfa
   *          information on the type-state problem
   */
  public TypeStateFunctionProvider(CallGraph cg, TypeStateDomain domain, ITypeStateDFA dfa, ICFGSupergraph supergraph,
      PointerAnalysis pointerAnalysis, Collection<InstanceKey> trackedInstances, ILiveObjectAnalysis liveObjectAnalysis,
      TraceReporter traceReporter) {
    super(cg, pointerAnalysis, supergraph, domain);
    this.dfa = dfa;
    this.liveObjectAnalysis = liveObjectAnalysis;
    this.traceReporter = traceReporter;
    this.exceptionalFlowKills = (traceReporter != null);
    initTrackedInstanceSet(pointerAnalysis, trackedInstances);
    computeEventMap(cg);

    if (dfa instanceof TracingProperty) {
      assert (traceReporter != null);
    }

  }

  /**
   * Special treatment for call edge from fake-root method.
   * 
   * @return flow function
   */
  protected IUnaryFlowFunction makeProgramEntryCallFlowFunction(Object src, Object dest) {
    return IdentityFlowFunction.identity();
  }

  /**
   * @return the flow function for a call in the supergraph, which does NOT
   *         represent a call to an entrypoint.
   */
  protected abstract IUnaryFlowFunction getNonEntryCallFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest);

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.wala.dataflow.IFDS.IUnaryFlowFunctionMap#getCallFlowFunction(java
   * .lang.Object, java.lang.Object)
   */
  public IUnaryFlowFunction getCallFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest, BasicBlockInContext<IExplodedBasicBlock> ret) {
    if (FakeRootMethod.isFromFakeRoot(src)) {
      return makeProgramEntryCallFlowFunction(src, dest);
    } else {
      return getNonEntryCallFlowFunction(src, dest);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.wala.dataflow.IFDS.IUnaryFlowFunctionMap#getCallToReturnFlowFunction
   * (java.lang.Object, java.lang.Object)
   */
  public IUnaryFlowFunction getCallToReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest) {
    
    // we need to be careful if we've sliced away any callees of this source
    // block.
    if (slicedAnyCallee(src)) {
      // we sliced away some callee, which means we treat such a callee as a
      // no-op.
      if (exceptionalFlowKills) {
        ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> cfg = supergraph.getCFG(src);
        for (Iterator<IExplodedBasicBlock> it = cfg.getNormalSuccessors(src.getDelegate()).iterator(); it.hasNext();) {
          Object o = it.next();
          if (o.equals(dest.getDelegate())) {
            return IdentityFlowFunction.identity();
          }
        }
        return UniversalKillFlowFunction.kill();
      }
      if (DEBUG_LEVEL > 2) {
        Trace.println("Identity getCallToReturn:" + src + "->" + dest);
      }
      return IdentityFlowFunction.identity();
    } else {
      if (DEBUG_LEVEL > 2) {
        Trace.println("UniversalKill getCallToReturn:" + src + "->" + dest);
      }
      return UniversalKillFlowFunction.kill();
    }
  }

  /**
   * @param b
   * @return the CFG to which basic block b belongs
   */
  protected ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> getCFG(BasicBlockInContext<IExplodedBasicBlock> b) {
    // CGNode node = (CGNode) getSupergraph().getProcOf(b);
    return supergraph.getCFG(b);
  }

  /**
   * @param n
   * @return the CFG for node n
   */
  protected ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> getCFG(CGNode n) {
    return supergraph.getICFG().getCFG(n);
  }

  /**
   * @param src
   *          a call node
   * @return true iff we have sliced away some callee of the node. TODO: looks
   *         like this is expensive, can we improve? [EY]
   */
  private boolean slicedAnyCallee(BasicBlockInContext<IExplodedBasicBlock> src) {
    SSAInvokeInstruction srcInvokeInstr = TypeStateFunctionProvider.getLastCallInstruction(getCFG(src), src);
    CGNode callNode = (CGNode) getSupergraph().getProcOf(src);
    int originalCalleeCount = getCallGraph().getNumberOfTargets(callNode, srcInvokeInstr.getCallSite());
    int actualCalleeCount = Iterator2Collection.toSet(getSupergraph().getCalledNodes(src)).size();
    return originalCalleeCount > actualCalleeCount;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.wala.dataflow.IFDS.IUnaryFlowFunctionMap#getNormalFlowFunction(
   * java.lang.Object, java.lang.Object)
   */
  public final IUnaryFlowFunction getNormalFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest) {
    
    //TODO: EY, return this to under the condition
    ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> cfg = supergraph.getCFG(src);
 
    Collection<IExplodedBasicBlock> ns = cfg.getNormalSuccessors(src.getDelegate());
    
 // caution: this is not only a debug printout
    if (!ns.contains(dest.getDelegate())) {
      if (DEBUG_LEVEL > 1) {
        Trace.println("ExceptionalFlow: " + src + "->" + dest);  
      }
      // return universal kill 
      return UniversalKillFlowFunction.kill();
    } else {
      if (DEBUG_LEVEL > 1) {
        Trace.println("NormalFlow: " + src + "->" + dest);
        Trace.printCollection("NormalSucc: ",cfg.getNormalSuccessors(src.getDelegate()));
        Trace.printCollection("ExceptionalSucc: ",cfg.getExceptionalSuccessors(src.getDelegate()));
      }
      
    }
    
    if (exceptionalFlowKills) {
      for (Iterator<IExplodedBasicBlock> it = cfg.getNormalSuccessors(src.getDelegate()).iterator(); it.hasNext();) {
        Object o = it.next();
        if (o.equals(dest.getDelegate())) {
          return composeNormalFlowFunction(src, dest);
        }
      }
      return UniversalKillFlowFunction.kill();
    }
    return composeNormalFlowFunction(src, dest);
  }

  private IUnaryFlowFunction composeNormalFlowFunction(BasicBlockInContext<IExplodedBasicBlock> s,
      BasicBlockInContext<IExplodedBasicBlock> d) {
    if (NORMAL_LIVE_KILLS && getLiveObjectAnalysis() != null && s.getLastInstructionIndex() >= 0) {

      IUnaryFlowFunction deadKiller = getObjectDeathFlowFunction(s, d);
      AggregateFlowFunction result = new AggregateFlowFunction();
      result.composeFunction(deadKiller);
      result.composeFunction(makeNormalFlowFunction(s, d));
      return result;
    } else {
      return makeNormalFlowFunction(s, d);
    }
  }

  /**
   * @return a flow function that kills all factoids related to any instance
   *         that cannot be live when traversing the control-flow edge s->d
   */
  private IUnaryFlowFunction getObjectDeathFlowFunction(BasicBlockInContext<IExplodedBasicBlock> s,
      BasicBlockInContext<IExplodedBasicBlock> d) {
    if (getDFA().observesObjectDeath()) {
      return new ObjectDeathTransitionFlow(makeDeadObjectFilter(s));
    } else {
      return FilterKillFunction.make(makeDeadObjectFilter(s));
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.wala.dataflow.IFDS.IUnaryFlowFunctionMap#getNormalFlowFunction(
   * java.lang.Object, java.lang.Object)
   */
  protected IUnaryFlowFunction makeNormalFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest) {
    return IdentityFlowFunction.identity();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.wala.dataflow.IFDS.IUnaryFlowFunctionMap#getReturnFlowFunction(
   * java.lang.Object, java.lang.Object)
   */
  public final IFlowFunction getReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> call,
      BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest) {
    if (exceptionalFlowKills) {
      if (src.getDelegate().isCatchBlock()) {
        return UniversalKillFlowFunction.kill();
      }
    }

    Trace.print("Getting return flow from " + src + " to " + dest);
    
    if (FakeRootMethod.isFromFakeRoot(dest)) {
      Trace.println("Return from FakeRootMethod");
      return getProgramExitFlowFunction(src, dest);
    } else {
      if (getLiveObjectAnalysis() == null) {
        return getNonExitReturnFlowFunction(call, src, dest);
      } else {
        IUnaryFlowFunction deadKiller = getObjectDeathFlowFunction(call, dest);
        AggregateFlowFunction result = new AggregateFlowFunction();
        result.composeFunction(deadKiller);
        result.composeFunction(getNonExitReturnFlowFunction(call, src, dest));
        return result;
      }
    }
  }

  private class ObjectDeathTransitionFlow implements IUnaryFlowFunction {

    private final IntFilter deadObjectFilter;

    ObjectDeathTransitionFlow(IntFilter deadObjectFilter) {
      this.deadObjectFilter = deadObjectFilter;
    }

    public SparseIntSet getTargets(int d1) {
      if (DEBUG_LEVEL > 1) {
        Trace.println("ObjectDeathTransitionFlow getTarget " + d1);
      }
      if (d1 == 0 || !deadObjectFilter.accepts(d1)) {
        // the object is not dying. just pass it on.
        return SparseIntSet.singleton(d1);
      } else {
        BaseFactoid inputFact = (BaseFactoid) domain.getMappedObject(d1);
        IDFAState succState = dfa.successor(inputFact.state, IObjectDeathEventImpl.singleton());
        // if moved from non-accepting to accepting state, record a message
        // indicating a finding
        boolean transitionToAccept = (!inputFact.state.isAccepting()) && succState.isAccepting();
        if (transitionToAccept) {
          if (getDFA() instanceof TypeStateProperty) {
            Assertions.UNREACHABLE("implement me");
          } else {
            traceReporter.record(inputFact);

          }
        }
        // the object is dead.
        return SparseIntSet.singleton(0);
      }
    }

  }

  /**
   * @return a filter that accepts a factoid only if it refers to a factoid that
   *         is not live when exiting a basic block
   */
  private IntFilter makeDeadObjectFilter(final BasicBlockInContext b) {

    // a filter that accepts a factoid only if it refers to a factoid that
    // is not live after the return
    IntFilter shouldKill = new IntFilter() {
      public boolean accepts(int i) {
        if (i == 0) {
          // this is the universal dummy fact. exclude it from the kill.
          return false;
        } else {
          Object factoid = getDomain().getMappedObject(i);
          if (factoid instanceof BaseFactoid) {
            BaseFactoid f = (BaseFactoid) factoid;
            InstanceKey ik = f.instance;
            if (ik instanceof AllocationSite) {
              AllocationSite ak = (AllocationSite) ik;
              try {
                return !getLiveObjectAnalysis().mayBeLive(ak, b.getNode(), b.getLastInstructionIndex());
              } catch (WalaException e) {
                e.printStackTrace();
                return false;
              }
            } else {
              return false;
            }
          } else {
            return false;
          }
        }
      }
    };

    return shouldKill;
  }

  /**
   * By default, the flow function for a return is identity. subclasses should
   * override as desired.
   * 
   */
  public IFlowFunction getNonExitReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> call,
      BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest) {
    return IdentityFlowFunction.identity();
  }

  /**
   * Special treatment for return edge to fake-root method.
   * 
   * @return flow function
   */
  public abstract IUnaryFlowFunction getProgramExitFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest);

  /**
   * Compute a map: CGNode -> IEvent
   * 
   * TODO: could reduce the work here by consulting ptr analysis
   * 
   * @param cg
   *          governing call graph
   */
  protected void computeEventMap(CallGraph cg) {
    for (Iterator<CGNode> it = cg.iterator(); it.hasNext();) {
      CGNode n = it.next();
      // TODO: support static methods and other more general events
      if (!n.getMethod().isStatic()) {
        PointerKey receiver = getPointerAnalysis().getHeapModel().getPointerKeyForLocal(n, 1);
        OrdinalSet<InstanceKey> pointsTo = getPointerAnalysis().getPointsToSet(receiver);
        if (pointsTo.containsAny(getTrackedInstanceSet())) {
          String sig = n.getMethod().getSignature();
          IEvent event = dfa.matchDispatchEvent(sig);
          if (event != null) {
            node2event.put(n, event);
          }
        }
      }
    }
  }

  /**
   * @return information on the type-state problem
   */
  public ITypeStateDFA getDFA() {
    return dfa;
  }

  /**
   * @return information on the type-state problem
   */
  public TypeStateProperty getDFAAsProperty() {

    assert (dfa instanceof TypeStateProperty);

    return (TypeStateProperty) dfa;
  }

  /**
   * @return Domain of dataflow facts
   */
  public TypeStateDomain getDomain() {
    return (TypeStateDomain) domain;
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.ibm.wala.dataflow.IFDS.IUnaryFlowFunctionMap#
   * getCallNoneToReturnFlowFunction(java.lang.Object, java.lang.Object)
   */
  public IUnaryFlowFunction getCallNoneToReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest) {
    // although this is a call-to-return, we've sliced away
    // the callee, so treat it as a no-op
    // still use the normal flow to get the statements from the block
    return getNormalFlowFunction(src, dest);
  }

  /**
   * @return Returns the pointerAnalysis.
   */
  public PointerAnalysis getPointerAnalysis() {
    return pointerAnalysis;
  }

  /**
   * @return Returns the supergraph.
   */
  public ICFGSupergraph getSupergraph() {
    return supergraph;
  }

  /**
   * Initialize the ordinal set which identifies tracked instances.
   * 
   * @param pointerAnalysis
   * @param trackedInstances
   */
  private void initTrackedInstanceSet(PointerAnalysis pointerAnalysis, Collection<InstanceKey> trackedInstances) {
    BitVectorIntSet s = new BitVectorIntSet();
    for (Iterator<InstanceKey> it = trackedInstances.iterator(); it.hasNext();) {
      InstanceKey ik = it.next();
      s.add(pointerAnalysis.getInstanceKeyMapping().getMappedIndex(ik));
    }
    trackedInstanceSet = new OrdinalSet<InstanceKey>(s, pointerAnalysis.getInstanceKeyMapping());
  }

  /**
   * @return Returns the trackedInstanceSet.
   */
  public OrdinalSet<InstanceKey> getTrackedInstanceSet() {
    return trackedInstanceSet;
  }

  public IEvent getEventForNode(CGNode node) {
    return node2event.get(node);
  }

  /**
   * @return Returns the callGraph.
   */
  public CallGraph getCallGraph() {
    return callGraph;
  }

  /**
   * @return Returns the liveObjectAnalysis.
   */
  protected ILiveObjectAnalysis getLiveObjectAnalysis() {
    return liveObjectAnalysis;
  }

  /**
   * @param node
   * @return true iff the caller node is in the Application class loader
   */
  public static boolean nodeInApplication(CGNode node) {
    return node.getMethod().getReference().getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application);
  }

  /**
   * @return Returns the traceReporter.
   */
  protected TraceReporter getTraceReporter() {
    if (dfa instanceof TracingProperty) {
      assert (traceReporter != null);
    }
    return traceReporter;
  }

  /**
   * @param callBlock
   * @return the last instruction in the block, a call
   */
  protected SSAInvokeInstruction getInvokeInstruction(BasicBlockInContext<IExplodedBasicBlock> callBlock) {

    assert (hasLastCallInstruction(getCFG(callBlock), callBlock));

    return TypeStateFunctionProvider.getLastCallInstruction(getCFG(callBlock), callBlock);
  }

  public static boolean hasLastCallInstruction(ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> cfg,
      BasicBlockInContext<IExplodedBasicBlock> block) {
    int lastIndex = block.getLastInstructionIndex();
    SSAInstruction[] instructions = cfg.getInstructions();
    return ((instructions[lastIndex] != null) && (instructions[lastIndex] instanceof SSAInvokeInstruction));
  }

  /**
   * get last call instruction in a basic block
   */
  @SuppressWarnings("unused")
  public static SSAInvokeInstruction getLastCallInstruction(ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> cfg,
      BasicBlockInContext<IExplodedBasicBlock> block) {
    int lastIndex = block.getLastInstructionIndex();
    SSAInstruction[] instructions = cfg.getInstructions();

    // this method is called so frequently, that this should only happen at
    // debug
    if (DEBUG_LEVEL > 0) {
      if (SafeAssertions.verifyAssertions) {
        if (!(instructions[lastIndex] instanceof SSAInvokeInstruction)) {
          int first = block.getFirstInstructionIndex();
          int last = block.getLastInstructionIndex();
          for (int i = first; i <= last; i++) {
            SSAInstruction s = (SSAInstruction) instructions[i];
            if (s != null) {
              Trace.println(s);
            }
          }
          assert false : "unexpected " + instructions[lastIndex] + " in " + block;
        }
      }
    }
    return (SSAInvokeInstruction) instructions[lastIndex];
  }
}