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

package com.ibm.safe.typestate.ap;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.accesspath.AccessPath;
import com.ibm.safe.accesspath.AccessPathDictionary;
import com.ibm.safe.accesspath.AccessPathSet;
import com.ibm.safe.accesspath.AccessPathSetTransformers;
import com.ibm.safe.accesspath.LocalPathElement;
import com.ibm.safe.accesspath.PathElement;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.typestate.core.TypeStateFunctionProvider;
import com.ibm.safe.typestate.core.UniversalKillFlowFunction;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.mine.TracingProperty;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.quad.AggregateFlowFunction;
import com.ibm.safe.typestate.quad.QuadFunctionProvider;
import com.ibm.safe.typestate.quad.QuadTypeStateDomain;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.cfg.Util;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.dataflow.IFDS.IFlowFunction;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.dataflow.IFDS.IdentityFlowFunction;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.escape.LocalLiveRangeAnalysis;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AbstractPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstruction.Visitor;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.GraphReachability;

/**
 * Function provider for the accesspath solver
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public abstract class AccessPathFunctionProvider extends QuadFunctionProvider {

  /**
   * kill access paths on normal flow, based on live analysis?
   */
  private static final boolean NORMAL_LIVE_KILL = true;

  /**
   * If accessPathKLimit (k) > 0, then we only track must paths of length <= k
   */
  protected final int accessPathKLimit;

  /**
   * graph view of pointer analysis
   */
  private final HeapGraph heapGraph;

  private final AccessPathDictionary APDictionary;

  /**
   * return a new access path set, where any paths in s that are dead after
   * executing b are removed
   */
  protected AccessPathSet removeDeadPaths(AccessPathSet s, CGNode n, ISSABasicBlock b, IR ir, DefUse du) {
    AccessPathSet result = new AccessPathSet(s);
    for (Iterator<AccessPath> it = s.iterator(); it.hasNext();) {
      AccessPath ap = it.next();
      PathElement head = ap.getHead();
      if (head instanceof LocalPathElement) {
        LocalPathElement l = (LocalPathElement) head;
        if (l.getPointerKey() instanceof LocalPointerKey) {
          LocalPointerKey lpk = (LocalPointerKey) l.getPointerKey();
          // note that we do not kill paths including parameters, since these
          // may
          // be useful
          // when transferring information back to a caller
          if (!lpk.isParameter() && lpk.getNode().equals(n)) {
            if (!LocalLiveRangeAnalysis.isLive(lpk.getValueNumber(), b.getLastInstructionIndex(), ir, du)) {
              result.remove(ap);
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * utility path-set transformer
   */
  protected final AccessPathSetTransformers apsTransformer;

  /**
   * Call graph reachability analysis
   */
  private final GraphReachability<CGNode,CGNode> reach;

  /**
   * governing typestate options
   */
  private final TypeStateOptions options;

  /**
   * create an AccessPathsFunctionProvider
   * 
   * @param cg
   *          - underlying callgraph
   * @param pointerAnalysis
   *          - precomputed results of pointer analysis
   * @param supergraph
   *          - precomputed supergraph
   * @param domain
   *          - underlying domain
   * @param dfa
   *          - typestate property to be verified
   * @param trackedInstances
   *          - instances to be tracked
   */
  public AccessPathFunctionProvider(CallGraph cg, PointerAnalysis pointerAnalysis, ICFGSupergraph supergraph,
      QuadTypeStateDomain domain, ITypeStateDFA dfa, Collection<InstanceKey> trackedInstances, AccessPathSetTransformers apst,
      GraphReachability<CGNode,CGNode> reach, TypeStateOptions options, ILiveObjectAnalysis live, TraceReporter traceReporter)
      throws PropertiesException {
    super(cg, pointerAnalysis, supergraph, domain, dfa, trackedInstances, live, traceReporter);
    this.APDictionary = apst.getAPDictionary();
    apsTransformer = apst;
    this.reach = reach;
    this.heapGraph = pointerAnalysis.getHeapGraph();
    this.options = options;
    this.accessPathKLimit = options.getAccessPathKLimit();
    // for now we assert that we use k-limits, since this is our only protection
    // against infinite recursion
    Assertions.productionAssertion(accessPathKLimit > 0);
  }

  public AccessPathSet assign(AccessPathSet s, PathElement x, AccessPath y) {
    return apsTransformer.assign(s, x, y);
  }

  public AccessPathSet assign(AccessPathSet s, PathElement x, Set<AccessPath> ySet) {
    return apsTransformer.assign(s, x, ySet);
  }

  public AccessPathSet gen(AccessPathSet s, PathElement x, AccessPath y) {
    return apsTransformer.gen(s, x, y);
  }

  public AccessPathSet kill(AccessPathSet s, PathElement x) {
    return apsTransformer.kill(s, x);
  }

  public AccessPathSet kill(AccessPathSet s, Set<PathElement> roots) {
    return apsTransformer.kill(s, roots);
  }

  public AccessPathSet rename(AccessPathSet s, PathElement x, Set<? extends PathElement> ySet) {
    return apsTransformer.rename(s, x, ySet);
  }

  /**
   * @param srcInvokeInstr
   * @param caller
   * @return the pointer key corresponding to the receiver of an invocation, or
   *         null if it is static
   */
  protected LocalPointerKey getReceiverPointerKey(SSAInvokeInstruction srcInvokeInstr, CGNode caller) {
    if (srcInvokeInstr.isStatic()) {
      return null;
    } else {
      int rcv = srcInvokeInstr.getReceiver();
      LocalPointerKey p = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(caller, rcv);
      return p;
    }
  }

  @SuppressWarnings("unused")
  protected IUnaryFlowFunction getNonEntryCallFlowFunction(BasicBlockInContext<IExplodedBasicBlock> srcBlock,
      BasicBlockInContext<IExplodedBasicBlock> destBlock) {

    if (DEBUG_LEVEL > 0) {
      assert !srcBlock.iteratePis().hasNext() : "Should not have pis here";
    }
    CGNode caller = srcBlock.getNode();
    CGNode callee = destBlock.getNode();

    // 1) compose the effects of the statements in the src block that come
    // before the call
    IUnaryFlowFunction blockFunction = composeBlockFlowFunction(caller, srcBlock);

    // 2) perform symbolic parameter copying if the successor block is a
    // call block
    SSAInvokeInstruction srcInvokeInstr = TypeStateFunctionProvider.getLastCallInstruction(getCFG(srcBlock), srcBlock);
    for (int p = 0; p < srcInvokeInstr.getNumberOfUses(); p++) {
      if (callee.getMethod().getParameterType(p).isReferenceType()) {
        TemporaryParameterPointerKey def = TemporaryParameterPointerKey.make(p);
        LocalPointerKey use = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(caller,
            srcInvokeInstr.getUse(p));
        IUnaryFlowFunction copy = makeLocalAssignFlowFunction(def, use);
        blockFunction = AggregateFlowFunction.compose(blockFunction, copy);
      }
    }

    // 2a) having done parameter copying, it's ok to kill dead APs
    if (NORMAL_LIVE_KILL && srcBlock.getLastInstructionIndex() >= 0) {
      blockFunction = AggregateFlowFunction.compose(blockFunction, makeDeadAccessPathKiller(caller, srcBlock));
    }

    // 3) compose with the callee-specific flow
    IEvent event = getEventForNode(callee);
    if (event != null && getDFA() instanceof TracingProperty) {
      // a hack: the tracing property may filter events based on the
      // caller. check this. we don't do this in the normal verifier
      // because the following is slow.
      event = getDFA().matchDispatchEvent(caller, callee.getMethod().getSignature());
    }
    if (event != null) {
      if (srcInvokeInstr.isStatic()) {
        Assertions.UNREACHABLE("unexpected event on static call to " + callee);
        blockFunction = AggregateFlowFunction.compose(blockFunction,
            makeCallFlowFunction(event, srcBlock, srcInvokeInstr, caller, callee));
      } else {
        blockFunction = AggregateFlowFunction.compose(blockFunction,
            makeCallFlowFunction(event, srcBlock, srcInvokeInstr, caller, callee));
      }
    } else {
      blockFunction = AggregateFlowFunction.compose(blockFunction,
          makeCallFlowFunction(null, srcBlock, srcInvokeInstr, caller, callee));
    }
    return blockFunction;
  }

  @SuppressWarnings("unused")
  public IFlowFunction getNonExitReturnFlowFunction(BasicBlockInContext<IExplodedBasicBlock> call,
      BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest) {

    CGNode callee = src.getNode();
    CGNode caller = dest.getNode();

    // TODO: EY, this requires fixing as we moved from the TwoExitCFG
    // Also, this way of identifying exceptional returns is barbaric
    // 0. if this is an exceptional return edge, kill flow along it
    boolean isNormalFlow = getCFG(call).getNormalSuccessors(call.getDelegate()).contains(dest.getDelegate());

    if (DEBUG_LEVEL > 1) {
      Trace.println("----------------------------------------------");
      Trace.println("RETURN FROM " + src + " TO " + dest + " WITH CALL " + call);
      Trace.println("is dest normal successor of call? " + isNormalFlow);
    }

    // TODO: barbaric patch until we revive TwoExitCFG [EY]
    if (!isNormalFlow) {
      return UniversalKillFlowFunction.kill();
    }

    if (DEBUG_LEVEL > 0) {
      assert !src.iteratePis().hasNext() : "Should not have pis here";
    }

    // 1. compose the effects of the statements in the return block up to the
    // exit statement
    IUnaryFlowFunction blockFunction = composeBlockFlowFunction(callee, src);

    SSAInvokeInstruction theInvokeInstr = getInvokeInstruction((BasicBlockInContext) call);
    if (theInvokeInstr != null) {
      // 2. compose with symbolic parameter renaming for return value

      // note that if we return exceptionally, we do not define the return
      // value.
      // TODO: EY, what happened to our handling of exceptional return? used to
      // say: (src instanceof ExceptionalExitBlock) ? emptySet

      //Set<PathElement> emptySet = Collections.emptySet();
      // Set<PathElement> retValueElements = (src instanceof
      // ExceptionalExitBlock) ? emptySet : computeReturnValueElements(callee);
      // EY: Avoid looking on whether this is exceptional exit, let the flow in
      // ret val handle that
      Set<PathElement> retValueElements = computeReturnValueElements(callee);
      IUnaryFlowFunction returnEdgeFunction = makeReturnFlowFunction(retValueElements, caller, callee, theInvokeInstr);
      blockFunction = AggregateFlowFunction.compose(blockFunction, returnEdgeFunction);

      if (!retValueElements.isEmpty()) {
        SSAInvokeInstruction callStatement = (SSAInvokeInstruction) call.getLastInstruction();
        if (callStatement.hasDef()) {
          TemporaryParameterPointerKey retValue = TemporaryParameterPointerKey.makeReturnValue();
          LocalPointerKey def = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(caller,
              callStatement.getDef());
          IUnaryFlowFunction copy = makeLocalRenameFlowFunction(def, retValue);
          blockFunction = AggregateFlowFunction.compose(blockFunction, copy);
        }
      }
    }

    if (NORMAL_LIVE_KILL && src.getLastInstructionIndex() >= 0) {
      blockFunction = AggregateFlowFunction.compose(blockFunction, makeDeadAccessPathKiller(caller, dest));
    }

    // 3. compose with effects of phi instructions in destBlock
    blockFunction = composeWithPhiAssignments((BasicBlockInContext) call, dest, blockFunction, caller);
    return blockFunction;
  }

  /**
   * @param node
   *          CGNode of the exit
   * @return Set<PathElement> representing the 0-length access paths which are
   *         the local variables which are returned by this method.
   */
  @SuppressWarnings("unused")
  Set<PathElement> computeReturnValueElements(CGNode node) {
    if (node.getMethod().getReference().getReturnType().isReferenceType()) {
      ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> cfg = getCFG(node);
      IExplodedBasicBlock exit = cfg.exit();
      SSAInstruction[] instructions = cfg.getInstructions();
      Set<PathElement> result = HashSetFactory.make();
      // TODO: EY - check if we can use getNormalPredecessors
      // for now, use getPredNodes
      // TODO: EY, if we are getting back exceptional flow, we must avoid that

      for (Iterator<? extends IBasicBlock> it = cfg.getPredNodes(exit); it.hasNext();) {
        IBasicBlock block = (IBasicBlock) it.next();
        int lastIndex = block.getLastInstructionIndex();
        if (lastIndex >= 0) {
          SSAInstruction inst = (SSAInstruction) instructions[lastIndex];
          if (inst instanceof SSAReturnInstruction) {
            SSAReturnInstruction retInstr = (SSAReturnInstruction) inst;
            if (retInstr != null) {
              int retValue = retInstr.getResult();
              if (retValue != -1) {
                LocalPointerKey retValueKey = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node,
                    retValue);
                result.add(new LocalPathElement(retValueKey));
              }
            }
          }
        }
      }
      if (DEBUG_LEVEL > 1) {
        Trace.println("RetValues: " + result);
      }
      return result;
    } else {
      return Collections.emptySet();
    }
  }

  @SuppressWarnings("unused")
  @Override
  public IUnaryFlowFunction makeNormalFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest) {

    CGNode node = src.getNode();

    IUnaryFlowFunction blockFunction = composeBlockFlowFunction(node, src);
    if (NORMAL_LIVE_KILL && src.getLastInstructionIndex() >= 0) {
      blockFunction = AggregateFlowFunction.compose(blockFunction, makeDeadAccessPathKiller(node, src));
    }

    // now we have to find the right pi-instruction (if such pi exists)
    // and compose it with the effect of other instructions in the block
    int destNumber = dest.getNumber();

    for (Iterator<SSAPiInstruction> piIterator = src.iteratePis(); piIterator.hasNext();) {
      SSAPiInstruction piInst = piIterator.next();
      int piNumber = piInst.getSuccessor();
      if (piNumber == destNumber) {
        if (DEBUG_LEVEL > 2) {
          Trace.println("Matching pi is: " + piInst);
        }
        IUnaryFlowFunction piTransformer = makePiFlowFunction(node, src, piInst);
        blockFunction = AggregateFlowFunction.compose(blockFunction, piTransformer);
      }
    }

    // now we have to compose the flow function with the effects of phi
    // functions in the successor block.
    if (dest.iteratePhis().hasNext()) {
      blockFunction = composeWithPhiAssignments(src, dest, blockFunction, node);
    }

    // if (!blockFunction.isEmpty()) {
    // return blockFunction;
    // } else {
    // return IdentityFlowFunction.identity();
    // }
    return blockFunction;
  }

  /**
   * compose the blockFunction for the src block with the assignments induced by
   * phis in the dest block
   * 
   * @param src
   * @param dest
   * @param blockFunction
   * @param node
   */
  private IUnaryFlowFunction composeWithPhiAssignments(BasicBlockInContext<IExplodedBasicBlock> src,
      BasicBlockInContext<IExplodedBasicBlock> dest, IUnaryFlowFunction blockFunction, CGNode node) {
    // if (dest instanceof ExceptionalExitBlock) {
    // // there are no phis in the exceptional exit block
    // return blockFunction;
    // }

    ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> cfg = getCFG(src);

    int j = Util.whichPred(cfg, src.getDelegate(), dest.getDelegate());

    for (Iterator<SSAPhiInstruction> phiIterator = dest.iteratePhis(); phiIterator.hasNext();) {
      SSAPhiInstruction phi = phiIterator.next();
      if (phi != null) {
        // TODO: why do we allow phi to be null?
        if (j >= phi.getNumberOfUses()) {
          Assertions.UNREACHABLE("invalid whichPred " + j + " for " + dest);
        }
        if (phi.getUse(j) > 0) {
          LocalPointerKey def = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, phi.getDef());
          LocalPointerKey use = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, phi.getUse(j));
          // i assert that it's ok to use a rename and not assign here, because
          // by IR construction, the phi is really a rename. but don't kill
          // params, which we need for return flow.
          IUnaryFlowFunction phiFunction = use.isParameter() ? makeLocalAssignFlowFunction(def, use) : makeLocalRenameFlowFunction(
              def, use);
          blockFunction = AggregateFlowFunction.compose(blockFunction, phiFunction);
        }
      }
    }
    return blockFunction;
  }

  protected ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> getCFG(BasicBlockInContext<IExplodedBasicBlock> src) {
    ICFGSupergraph s = getSupergraph();
    ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> sg = s.getCFG(src);
    // TwoExitCFG tec = new TwoExitCFG(sg);
    // SSACFG cfg = null;
    // if (sg instanceof TwoExitCFG) {
    // cfg = (SSACFG) ((TwoExitCFG) sg).getDelegate();
    // } else {
    // if (Assertions.verifyAssertions) {
    // Assertions._assert(sg instanceof SSACFG);
    // }
    // cfg = (SSACFG) sg;
    // }
    // return cfg;
    // return tec;
    return sg;
  }

  /**
   * handle block instructions. Creates an aggregate flow functions for a
   * sequence of instructions inside a given basic block
   * 
   * @param srcBlock
   *          - basic block to be processed
   * @return an AggregateFlowFunction composing the transformers of block
   *         instructions
   */
  @SuppressWarnings("unused")
  protected IUnaryFlowFunction composeBlockFlowFunction(final CGNode node, final BasicBlockInContext<IExplodedBasicBlock> srcBlock) {

    IUnaryFlowFunction prevFunction = null;

    for (Iterator<SSAInstruction> it = srcBlock.iterator(); it.hasNext();) {
      final SSAInstruction inst = it.next();
      if (inst == null) {
        // TODO: why is it allowed to be null?
        continue;
      }
      if (DEBUG_LEVEL > 2) {
        Trace.println("I:" + inst);
      }

      class StatementVisitor extends Visitor {
        IUnaryFlowFunction currFunction = null;

        public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
          currFunction = makeALoadFlowFunction(node, srcBlock, instruction);
        }

        public void visitArrayStore(SSAArrayStoreInstruction instruction) {
          currFunction = makeAStoreFlowFunction(node, srcBlock, instruction);
        }

        public void visitGet(SSAGetInstruction instruction) {
          currFunction = makeGetFlowFunction(node, srcBlock, instruction);
        }

        public void visitPut(SSAPutInstruction instruction) {
          currFunction = makePutFlowFunction(node, srcBlock, instruction);
        }

        public void visitNew(SSANewInstruction instruction) {
          currFunction = makeAllocFlowFunction(node, srcBlock, instruction);
        }

        public void visitThrow(SSAThrowInstruction instruction) {
          // TODO
        }

        public void visitCheckCast(SSACheckCastInstruction instruction) {
          currFunction = makeCheckCastFlowFunction(node, srcBlock, instruction);
        }

        public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
          // TODO
        }
      }
      ;
      StatementVisitor v = new StatementVisitor();
      inst.visit(v);
      if (v.currFunction != null) {
        if (prevFunction == null) {
          prevFunction = v.currFunction;
        } else {
          prevFunction = AggregateFlowFunction.compose(prevFunction, v.currFunction);
        }
      }
    }

    prevFunction = (prevFunction != null) ? prevFunction : IdentityFlowFunction.identity();

    return prevFunction;
  }

  /**
   * @param node
   *          a node in the call graph
   * @param b
   *          a basic block in the node
   * @return a flow function that kills access paths that are dead after b
   */
  protected abstract IUnaryFlowFunction makeDeadAccessPathKiller(CGNode node, ISSABasicBlock b);

  /**
   * @param retValElements
   *          Set<PathElement>, 0-level access paths which represent the return
   *          value in the callee
   * @param caller
   * @return flow function which encodes the dataflow of a return edge
   */
  protected abstract IUnaryFlowFunction makeReturnFlowFunction(Set<PathElement> retValElements, CGNode caller, CGNode callee,
      SSAInvokeInstruction call);

  /**
   * @param node
   *          call-graph node of bb
   * @param bb
   *          basic block of pi instruction
   * @param pi
   *          the pi instruction
   * @return flow function which encodes the dataflow of a pi instruction
   */
  protected abstract IUnaryFlowFunction makePiFlowFunction(CGNode node, ISSABasicBlock bb, SSAPiInstruction pi);

  /**
   * @param x
   *          def of a phi instruction
   * @param y
   *          a use in the phi instruction
   * @return flow function which encodes the dataflow of a local assignment
   */
  protected abstract IUnaryFlowFunction makeLocalAssignFlowFunction(AbstractPointerKey x, AbstractPointerKey y);

  /**
   * @param x
   *          def of a phi instruction
   * @param y
   *          a use in the phi instruction
   * @return flow function which encodes the dataflow of an assignment x = (T)
   *         y;
   */
  protected abstract IUnaryFlowFunction makeCheckCastFlowFunction(LocalPointerKey x, LocalPointerKey y, TypeReference T);

  /**
   * @param ik
   *          instance key allocated by the flow function
   * @param initial
   *          initial state of instance according to typestate property
   * @param pk
   *          local pointer key for lhs of statement
   * @return flow function which encodes the dataflow of a "new" statement
   */
  protected abstract IUnaryFlowFunction makeAllocFlowFunction(InstanceKey ik, IDFAState initial, LocalPointerKey pk);

  /**
   * is a new instruction allocating an individual of the tracked type?
   */
  protected boolean allocatesTrackedInstance(CGNode node, SSANewInstruction newInst) {
    InstanceKey ik = getPointerAnalysis().getHeapModel().getInstanceKeyForAllocation(node, newInst.getNewSite());
    return getTrackedInstanceSet().contains(ik);
  }

  /**
   * handle allocation instruction
   */
  protected IUnaryFlowFunction makeAllocFlowFunction(CGNode node, ISSABasicBlock block, SSANewInstruction newInst) {
    if (allocatesTrackedInstance(node, newInst)) {
      if (DEBUG_LEVEL > 1) {
        Trace.println("new-inst:" + newInst);
      }
      InstanceKey ik = getPointerAnalysis().getHeapModel().getInstanceKeyForAllocation(node, newInst.getNewSite());
      LocalPointerKey pk = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, newInst.getDef());

      return makeAllocFlowFunction(ik, getDFA().initial(), pk);
    } else {
      return IdentityFlowFunction.identity();
    }
  }

  /**
   * @return Returns the apsTransformer.
   */
  protected AccessPathSetTransformers getApsTransformer() {
    return apsTransformer;
  }

  /**
   * x.f = y or x.f = null
   */
  @SuppressWarnings("unused")
  protected IUnaryFlowFunction makePutFlowFunction(CGNode node, ISSABasicBlock block, SSAPutInstruction putInst) {

    if (DEBUG_LEVEL > 1) {
      Trace.println("put-inst:" + putInst);
    }

    int rhs = putInst.getVal();
    LocalPointerKey y = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, rhs);

    if (putInst.isStatic()) {
      FieldReference fieldRef = putInst.getDeclaredField();
      if (fieldRef.getFieldType().isReferenceType()) {
        IField fld = getCallGraph().getClassHierarchy().resolveField(fieldRef);
        if (fld != null) {
          StaticFieldKey X_f = (StaticFieldKey) getPointerAnalysis().getHeapModel().getPointerKeyForStaticField(fld);
          return makePutStaticFlowFunction(X_f, y);
        } else {
          // SafeLogger.warning("Field cannot be resolved: " + fieldRef + " for
          // instruction " + putInst);
          return IdentityFlowFunction.identity();
        }
      }
    } else {
      int ref = putInst.getRef();
      FieldReference fieldReference = putInst.getDeclaredField();
      IField fld = getCallGraph().getClassHierarchy().resolveField(fieldReference);
      if (fld == null) {
        // SafeLogger.warning("Field cannot be resolved: " + fieldReference + "
        // for instruction " + putInst);
        return IdentityFlowFunction.identity();
      } else {
        LocalPointerKey x = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, ref);
        return makePutFieldFlowFunction(x, y, fld);
      }
    }
    return IdentityFlowFunction.identity();
  }

  /**
   * X.f = y
   */
  protected abstract IUnaryFlowFunction makePutStaticFlowFunction(StaticFieldKey X_f, LocalPointerKey y);

  /**
   * x.f = y
   */
  protected abstract IUnaryFlowFunction makePutFieldFlowFunction(LocalPointerKey x, LocalPointerKey y, IField f);

  /**
   * x = Y.f
   */
  protected abstract IUnaryFlowFunction makeGetStaticFlowFunction(LocalPointerKey x, StaticFieldKey Y_f);

  /**
   * x = y.f
   */
  protected abstract IUnaryFlowFunction makeGetFieldFlowFunction(LocalPointerKey x, LocalPointerKey y, IField f);

  /**
   * x = y[i]
   */
  protected abstract IUnaryFlowFunction makeALoadFlowFunction(LocalPointerKey x, LocalPointerKey y);

  /**
   * x[i] = y
   */
  protected abstract IUnaryFlowFunction makeAStoreFlowFunction(LocalPointerKey x, LocalPointerKey y);

  public AccessPathSet updateMust(AccessPathSet s, AccessPath x_f, PathElement y) {
    return getApsTransformer().updateMust(s, x_f, y, accessPathKLimit);
  }

  /**
   * x = (T) y
   */
  protected IUnaryFlowFunction makeCheckCastFlowFunction(CGNode node, ISSABasicBlock block, SSACheckCastInstruction inst) {
    LocalPointerKey x = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, inst.getDef());
    LocalPointerKey y = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, inst.getVal());
    TypeReference t = inst.getDeclaredResultType();
    return makeCheckCastFlowFunction(x, y, t);
  }

  /**
   * x = y[i]
   */
  protected IUnaryFlowFunction makeALoadFlowFunction(CGNode node, ISSABasicBlock block, SSAArrayLoadInstruction aload) {
    LocalPointerKey x = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, aload.getDef());
    LocalPointerKey y = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, aload.getArrayRef());
    return makeALoadFlowFunction(x, y);
  }

  /**
   * x[i] = y
   */
  protected IUnaryFlowFunction makeAStoreFlowFunction(CGNode node, ISSABasicBlock block, SSAArrayStoreInstruction astore) {
    LocalPointerKey x = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, astore.getArrayRef());
    LocalPointerKey y = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, astore.getValue());
    return makeAStoreFlowFunction(x, y);
  }

  /**
   * x = y.f
   */
  protected IUnaryFlowFunction makeGetFlowFunction(CGNode node, ISSABasicBlock block, SSAGetInstruction getInst) {
    LocalPointerKey x = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, getInst.getDef());

    if (getInst.isStatic()) {
      FieldReference fieldRef = getInst.getDeclaredField();
      if (fieldRef.getFieldType().isReferenceType()) {
        IField fld = getCallGraph().getClassHierarchy().resolveField(fieldRef);
        if (fld != null) {
          StaticFieldKey Y_f = (StaticFieldKey) getPointerAnalysis().getHeapModel().getPointerKeyForStaticField(fld);
          return makeGetStaticFlowFunction(x, Y_f);
        } else {
          // SafeLogger.warning("Field cannot be resolved: " + fieldRef + " for
          // instruction " + getInst);
          return IdentityFlowFunction.identity();
        }
      } else {
        return IdentityFlowFunction.identity();
      }
    } else {
      IField fld = getCallGraph().getClassHierarchy().resolveField(getInst.getDeclaredField());
      LocalPointerKey y = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(node, getInst.getRef());
      if (fld == null) {
        // SafeLogger.warning("Field cannot be resolved: " +
        // getInst.getDeclaredField() + " for instruction " + getInst);
        return IdentityFlowFunction.identity();
      } else {
        return makeGetFieldFlowFunction(x, y, fld);
      }
    }
  }

  /**
   * @param event
   *          the event this call represents, or null if none
   * @param src
   *          basic block of call instruction
   * @param srcInvokeInstruction
   *          the call instruction
   * @param caller
   *          CGNode of the calling node
   * @param callee
   *          CGNode of callee
   * @return a flow function which encodes the dataflow of a call edge
   */
  protected IUnaryFlowFunction makeCallFlowFunction(IEvent event, BasicBlockInContext<IExplodedBasicBlock> src,
      SSAInvokeInstruction srcInvokeInstruction, CGNode caller, CGNode callee) {
    IUnaryFlowFunction callFlow = makeCallFlowFunction(caller, callee, srcInvokeInstruction);
    if (event == null) {
      return callFlow;
    } else {
      return AggregateFlowFunction.compose(callFlow, makeEventFlowFunction(event, src, srcInvokeInstruction, caller, callee));
    }
  }

  /**
   * @param event
   *          event label in the DFA
   * @param block
   *          basic block of the call instruction
   * @param invokeInstr
   *          invoke instruction which induces the event
   * @param caller
   *          calling node
   */
  protected abstract IUnaryFlowFunction makeEventFlowFunction(IEvent event, BasicBlockInContext<IExplodedBasicBlock> block,
      SSAInvokeInstruction invokeInstr, CGNode caller, CGNode callee);

  /**
   * @param callee
   *          CGNode of callee
   * @return a flow function which encodes the dataflow induced by a call edge,
   *         not counting typestate events
   */
  protected abstract IUnaryFlowFunction makeCallFlowFunction(CGNode caller, CGNode callee, SSAInvokeInstruction call);

  public AccessPathSet updateMay(AccessPathSet s, AccessPath x_f, PathElement y) {
    return getApsTransformer().updateMay(s, x_f, y, accessPathKLimit);
  }

  /**
   * Use call graph reachability to prune irrelevant access paths
   * 
   * @param s
   * @param n
   * @return s \ any access-paths \in s that contain locals that cannot be live
   *         when an activation for node is on the top of the stack.
   */
  protected AccessPathSet killOutOfScopeLocals(AccessPathSet s, CGNode n) {
    AccessPathSet result = new AccessPathSet(APDictionary);
    for (Iterator<AccessPath> it = s.iterator(); it.hasNext();) {
      AccessPath ap = it.next();
      PathElement head = ap.getHead();
      if (head instanceof LocalPathElement) {
        LocalPathElement lp = (LocalPathElement) head;
        AbstractPointerKey pk = (AbstractPointerKey) lp.getPointerKey();
        if (pk instanceof LocalPointerKey) {
          LocalPointerKey lpk = (LocalPointerKey) pk;
          if (reach.getReachableSet(lpk.getNode()).contains(n)) {
            result.add(ap);
          }
        } else {
          result.add(ap);
        }
      } else {
        result.add(ap);
      }
    }
    return result;
  }

  /**
   * Kill any locals from the node n in the set s
   * 
   * @param s
   * @param n
   * @return s \ any access-paths \in s that contain locals from node n
   */
  protected AccessPathSet killLocals(AccessPathSet s, CGNode n) {
    AccessPathSet result = new AccessPathSet(APDictionary);
    for (Iterator<AccessPath> it = s.iterator(); it.hasNext();) {
      AccessPath ap = it.next();
      PathElement head = ap.getHead();
      if (head instanceof LocalPathElement) {
        LocalPathElement lp = (LocalPathElement) head;
        AbstractPointerKey pk = (AbstractPointerKey) lp.getPointerKey();
        if (pk instanceof LocalPointerKey) {
          LocalPointerKey lpk = (LocalPointerKey) pk;
          if (!lpk.getNode().equals(n)) {
            result.add(ap);
          }
        } else {
          result.add(ap);
        }
      } else {
        result.add(ap);
      }
    }
    return result;
  }

  protected abstract IUnaryFlowFunction makeLocalRenameFlowFunction(AbstractPointerKey x, AbstractPointerKey y);

  /**
   * @return Returns the options.
   */
  protected TypeStateOptions getOptions() {
    return options;
  }

  /**
   * @return Returns the heapGraph.
   */
  protected HeapGraph getHeapGraph() {
    return heapGraph;
  }

  /**
   * @return Returns the reach.
   */
  protected GraphReachability<CGNode,CGNode> getReach() {
    return reach;
  }

  /**
   * @return Returns the aPDictionary.
   */
  public AccessPathDictionary getAPDictionary() {
    return APDictionary;
  }
}