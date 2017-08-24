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
import java.util.Iterator;

import com.ibm.safe.accesspath.AccessPath;
import com.ibm.safe.accesspath.AccessPathDictionary;
import com.ibm.safe.accesspath.AccessPathSet;
import com.ibm.safe.accesspath.AccessPathSetTransformers;
import com.ibm.safe.accesspath.LocalPathElement;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.typestate.ap.AccessPathFunctionProvider;
import com.ibm.safe.typestate.ap.must.mustnot.MustMustNotAPFunctionProvider;
import com.ibm.safe.typestate.ap.must.mustnot.MustMustNotAuxiliary;
import com.ibm.safe.typestate.core.TypeStateFunctionProvider;
import com.ibm.safe.typestate.core.TypeStateMessage;
import com.ibm.safe.typestate.quad.QuadFactoid;
import com.ibm.safe.typestate.quad.QuadTypeStateDomain;
import com.ibm.safe.typestate.unique.UniqueCallFlowFunction;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.collections.FilterIterator;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * @TODO: make stronger assumptions about aliasing induced by callees.
 * 
 * @author sfink
 * @author yahave
 */
public class LocalMustMustNotFlowFunctions extends SingleProcedureFlowFunctions {

  private final CallGraph callGraph;

  private final PointerAnalysis pointerAnalysis;

  /**
   * @param node
   * @param cfg
   * @param delegate
   * @param nodesThatMatter
   */
  public LocalMustMustNotFlowFunctions(CGNode node, ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> cfg, TypeStateFunctionProvider delegate,
      Collection<CGNode> nodesThatMatter, Collection<InstanceKey> instances, CallGraph callGraph, PointerAnalysis pointerAnalysis,
      ILiveObjectAnalysis live) {
    super(node, cfg, delegate, nodesThatMatter, instances, live, callGraph);
    this.callGraph = callGraph;
    this.pointerAnalysis = pointerAnalysis;
  }

  protected IUnaryFlowFunction makeWorstCaseFlowFunction() {
    return new CallPollution();
  }

  private MustMustNotAPFunctionProvider getMMNFunctions() {
    return (MustMustNotAPFunctionProvider) getDelegate();
  }

  private QuadTypeStateDomain getQuadDomain() {
    return getMMNFunctions().getQuadDomain();
  }

  private AccessPathDictionary getAPDictionary() {
    return ((AccessPathFunctionProvider) getDelegate()).getAPDictionary();
  }

  /**
   * a filter which accepts only non-accepting states
   */
  private final static Predicate<Object> nonErr = new Predicate<Object>() {
    public boolean test(Object o) {
      IDFAState s = (IDFAState) o;
      return !s.isAccepting();
    }
  };

  /**
   * @return Iterator<IState>
   */
  private Iterator<IDFAState> getNonErrStates() {
    return new FilterIterator<IDFAState>(getDelegate().getDomain().getDFAAsProperty().statesIterator(), nonErr);
  }

  /**
   * Effect of an unknown call.
   * 
   * TODO: enhance this with mod-ref info to be less conservative about aliasing
   * side effects
   */
  private class CallPollution implements IUnaryFlowFunction {

    public SparseIntSet getTargets(int d1) {
      MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
      if (d1 == 0) {
        result.add(0);
      } else {
        QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d1);
        MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
        AccessPathSet must = aux.getMustPaths();
        // we know that any locals that point to this instance, must still point
        // to the same instance after the call. That's all we know about
        // aliasing after
        // the call.
        AccessPathSet newMust = getMMNFunctions().kLimit(must, 1);
        MustMustNotAuxiliary newAux = new MustMustNotAuxiliary(newMust, new AccessPathSet(getAPDictionary()), false);

        for (Iterator<IDFAState> it = getNonErrStates(); it.hasNext();) {
          IDFAState s = it.next();
          int d2 = getQuadDomain().findOrCreate(tuple.instance, s, false, newAux);
          result.add(d2);
        }
      }

      // for now we must assume any instance can return in any state :(
      // really need to enhance this logic soon. I know this defeats the
      // previous
      // loop; but keep the previous loop around for when this gets fixed.
      MustMustNotAuxiliary badAux = new MustMustNotAuxiliary(new AccessPathSet(getAPDictionary()), new AccessPathSet(
          getAPDictionary()), false);
      for (Iterator<InstanceKey> it = getInstances().iterator(); it.hasNext();) {
        InstanceKey ik = it.next();
        for (Iterator<IDFAState> it2 = getNonErrStates(); it2.hasNext();) {
          IDFAState s = it2.next();
          int d2 = getQuadDomain().findOrCreate(ik, s, false, badAux);
          result.add(d2);
        }
      }
      return result;
    }
  }

  protected IUnaryFlowFunction makeEventFlowFunction(IEvent event, BasicBlockInContext<IExplodedBasicBlock> block, CGNode callee) {
    SSAInvokeInstruction s = TypeStateFunctionProvider.getLastCallInstruction(getCfg(), block);
    LocalPointerKey x = (LocalPointerKey) pointerAnalysis.getHeapModel().getPointerKeyForLocal(getNode(), s.getReceiver());
    return new EventFlow(event, x, block, s, callee);
  }

  /**
   * Effect of a call which causes a typestate transition. This flow models the
   * focus / MMN logic; along with other unknown side effects.
   * 
   * We make the following assumptions about callees that are events:
   * <ul>
   * <li> event calls will NOT allocate new instances we care about. So, we can
   * assume that event calls will not GEN factoids for any possible instance;
   * instead, they will at worst change the typestate of incoming factoids.
   * Furthermore event calls will not change uniqueness properties of factoids.
   * <li> if we make a call x.foo() on an instance in state S_1, and foo moves
   * typestate S_1 -> S_2, then we guarantee that when foo returns, x will point
   * to I in state S_2. This holds even if S_1 == S_2.
   * </ul>
   */
  private class EventFlow implements IUnaryFlowFunction {

    /**
     * true iff the FI pointer analysis has a unique possible receiver for this
     * call
     */
    private boolean uniqueMayTarget;

    private final LocalPointerKey x;

    /**
     * receiver of the call
     */
    private final AccessPath xPath;

    private final IEvent event;

    private final SSAInvokeInstruction invoke;

    private final ISSABasicBlock block;

    private final CGNode callee;

    /**
     * @param x
     *            the receiver of the call.
     */
    EventFlow(IEvent event, LocalPointerKey x, ISSABasicBlock block, SSAInvokeInstruction s, CGNode callee) {
      this.event = event;
      this.x = x;
      this.xPath = ((AccessPathFunctionProvider) getDelegate()).getAPDictionary().findOrCreate(new LocalPathElement(x));
      this.invoke = s;
      this.block = block;
      this.callee = callee;
      if (s.isStatic()) {
        uniqueMayTarget = false;
      } else {
        HeapModel hm = pointerAnalysis.getHeapModel();
        OrdinalSet<InstanceKey> pointsTo = pointerAnalysis.getPointsToSet(hm.getPointerKeyForLocal(getNode(), s.getReceiver()));
        uniqueMayTarget = UniqueCallFlowFunction.existsUniqueReceiver(pointsTo, callee, callGraph.getClassHierarchy());
      }
    }

    public SparseIntSet getTargets(int d1) {

      if (d1 == 0) {
        return SparseIntSet.singleton(0);
      }

      QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d1);
      MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
      AccessPathSet must = aux.getMustPaths();
      AccessPathSet mustNot = aux.getMustNotPaths();

      if (mustNot.contains(xPath)) {
        // the must-not information tells us that this call cannot possibly
        // apply. so just return the old factoid.
        return SparseIntSet.singleton(d1);
      }

      boolean strongUpdate = must.contains(xPath);
      boolean weakUpdate = false;
      if (strongUpdate && AccessPathSetTransformers.containsArrayPath(must)) {
        // an array path element precludes the possibility of strong updates.
        strongUpdate = false;
        weakUpdate = true;
      } else if (!strongUpdate) {
        OrdinalSet<InstanceKey> possibleReceivers = pointerAnalysis.getPointsToSet(x);
        if (possibleReceivers.contains(tuple.instance)) {
          PointerKey r = pointerAnalysis.getHeapModel().getPointerKeyForLocal(callee, 1);
          possibleReceivers = pointerAnalysis.getPointsToSet(r);
          weakUpdate = !aux.isComplete() && possibleReceivers.contains(tuple.instance);
        }
      }
      if (weakUpdate && strongUpdateBasedOnUnique(tuple, x)) {
        strongUpdate = true;
        weakUpdate = false;
      }

      if (strongUpdate || weakUpdate) {
        IDFAState succState = getDelegate().getDFA().successor(tuple.state, event);
        succState = updateForAcceptState(d1, tuple, succState);
        int newStateIndex = getQuadDomain().findOrCreate(tuple.instance, succState, tuple.isUnique(), tuple.aux);
        if (weakUpdate && (!succState.equals(tuple.state))) {
          // FOCUS!! we took a weak update and changed state. generate 2
          // factoids based
          // on the focus operation!!!

          // if we take the state change, this must point to the instance.
          AccessPathSet positiveMust = new AccessPathSet(must);
          positiveMust.add(xPath);
          MustMustNotAuxiliary positiveAux = new MustMustNotAuxiliary(positiveMust, mustNot, aux.isComplete());
          int focusPositive = getQuadDomain().findOrCreate(tuple.instance, succState, tuple.isUnique(), positiveAux);

          // if we don't take the state change, this must not point to the
          // instance.
          AccessPathSet negativeMustNot = new AccessPathSet(getAPDictionary(), xPath);
          MustMustNotAuxiliary negativeAux = new MustMustNotAuxiliary(must, negativeMustNot, aux.isComplete());
          int focusNegative = getQuadDomain().findOrCreate(tuple.instance, succState, tuple.isUnique(), negativeAux);

          // generate both factoids from the focus
          return SparseIntSet.pair(focusPositive, focusNegative);
        } else {
          // did not focus. just update the state.
          return SparseIntSet.singleton(newStateIndex);
        }
      } else {
        // if we don't take the state change, x must not point to the
        // instance.
        AccessPathSet negativeMustNot = new AccessPathSet(getAPDictionary(), xPath);
        MustMustNotAuxiliary negativeAux = new MustMustNotAuxiliary(must, negativeMustNot, aux.isComplete());
        int focusNegative = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(), negativeAux);
        return SparseIntSet.singleton(focusNegative);
      }
    }

    protected boolean strongUpdateBasedOnUnique(QuadFactoid tuple, PointerKey x) {
      if (uniqueMayTarget && tuple.isUnique()) {
        OrdinalSet<InstanceKey> possibleReceivers = pointerAnalysis.getPointsToSet(x);
        return possibleReceivers.contains(tuple.instance);
      } else {
        return false;
      }
    }

    private IDFAState updateForAcceptState(int d1, QuadFactoid tuple, IDFAState succState) {
      if ((!tuple.state.isAccepting()) && succState.isAccepting()) {
        getQuadDomain().addMessage(makeMessage(tuple));
        // don't propagate accepting state ... use old state instead
        succState = tuple.state;
      }
      return succState;
    }

    protected TypeStateMessage makeMessage(QuadFactoid tuple) {
      return new TypeStateMessage(getDelegate().getDFAAsProperty(), tuple, getNode(), block, invoke, getNode(), tuple.aux);
    }
  }

  public IUnaryFlowFunction getCallFlowFunction(BasicBlockInContext<IExplodedBasicBlock> src, BasicBlockInContext<IExplodedBasicBlock> dest, BasicBlockInContext<IExplodedBasicBlock> ret) {
    // TODO Auto-generated method stub
    return null;
  }

}
