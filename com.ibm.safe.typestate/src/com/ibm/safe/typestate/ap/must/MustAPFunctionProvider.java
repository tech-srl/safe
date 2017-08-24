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
package com.ibm.safe.typestate.ap.must;

import java.util.Collection;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.accesspath.AccessPath;
import com.ibm.safe.accesspath.AccessPathSet;
import com.ibm.safe.accesspath.AccessPathSetTransformers;
import com.ibm.safe.accesspath.LocalPathElement;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.typestate.core.AbstractWholeProgramSolver;
import com.ibm.safe.typestate.core.TypeStateMessage;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.quad.QuadFactoid;
import com.ibm.safe.typestate.quad.QuadTypeStateDomain;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.typestate.unique.UniqueCallFlowFunction;
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
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.graph.GraphReachability;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * @author sfink
 * @author yahave
 * 
 */
public class MustAPFunctionProvider extends AbstractMustAPFunctionProvider {

  public MustAPFunctionProvider(CallGraph cg, PointerAnalysis pointerAnalysis, ICFGSupergraph supergraph,
      QuadTypeStateDomain domain, ITypeStateDFA dfa, Collection<InstanceKey> trackedInstances, AccessPathSetTransformers apst,
      GraphReachability<CGNode,CGNode> reach, TypeStateOptions options, ILiveObjectAnalysis live, TraceReporter traceReporter)
      throws PropertiesException {
    super(cg, pointerAnalysis, supergraph, domain, dfa, trackedInstances, apst, reach, options, live, traceReporter);
  }

  /**
   * a flow function that changes the state of an instance in response to an
   * event
   */
  public class EventFlowFunction implements IUnaryFlowFunction {

    /**
     * true iff the FI pointer analysis has a unique possible receiver for this
     * call
     */
    private boolean uniqueMayTarget;

    /**
     * event label in the DFA
     */
    private final IEvent automatonLabel;

    /**
     * invoke instruction which induces the event
     */
    private final SSAInvokeInstruction invokeInstr;

    /**
     * basic block of the call instruction
     */
    private final BasicBlockInContext<IExplodedBasicBlock> block;

    /**
     * calling node
     */
    private final CGNode caller;

    /**
     * callee node
     */
    private final CGNode callee;

    /**
     * @param event
     *            event label in the DFA
     * @param block
     *            basic block of the call instruction
     * @param invokeInstr
     *            invoke instruction which induces the event
     * @param caller
     *            calling node
     */
    public EventFlowFunction(IEvent event, BasicBlockInContext<IExplodedBasicBlock> block, SSAInvokeInstruction invokeInstr, CGNode caller, CGNode callee) {
      this.block = block;
      this.automatonLabel = event;
      this.invokeInstr = invokeInstr;
      this.caller = caller;
      this.callee = callee;

      if (invokeInstr.isStatic()) {
        uniqueMayTarget = false;
      } else {
        HeapModel hm = getPointerAnalysis().getHeapModel();
        OrdinalSet<InstanceKey> pointsTo = getPointerAnalysis().getPointsToSet(
            hm.getPointerKeyForLocal(caller, invokeInstr.getReceiver()));
        uniqueMayTarget = UniqueCallFlowFunction.existsUniqueReceiver(pointsTo, callee, getCallGraph().getClassHierarchy());
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.wala.dataflow.IFDS.IFlowFunction#getTargets(int)
     */
    public SparseIntSet getTargets(int d1) {

      if (d1 == 0) {
        return SparseIntSet.singleton(0);
      }

      MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();

      QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d1);
      MustAuxiliary must = (MustAuxiliary) tuple.aux;
      AccessPathSet mup = must.getMustPaths();

      LocalPointerKey thisPtr = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(getCallee(), 1);
      AccessPath thisPath = getAPDictionary().findOrCreate(new LocalPathElement(thisPtr));
      boolean strongUpdate = mup.contains(thisPath);
      boolean weakUpdate = false;
      if (strongUpdate && AccessPathSetTransformers.containsArrayPath(mup)) {
        // an array path element precludes the possibility of strong updates.
        strongUpdate = false;
        weakUpdate = true;
      } else {
        OrdinalSet<InstanceKey> possibleReceivers = getPointerAnalysis().getPointsToSet(getReceiverPointerKey(invokeInstr, caller));
        if (possibleReceivers.contains(tuple.instance)) {
          PointerKey r = getPointerAnalysis().getHeapModel().getPointerKeyForLocal(getCallee(), 1);
          possibleReceivers = getPointerAnalysis().getPointsToSet(r);
          weakUpdate = !must.isComplete() && possibleReceivers.contains(tuple.instance);
        }
      }
      if (weakUpdate && strongUpdateBasedOnUnique(tuple, getReceiverPointerKey(invokeInstr, caller))) {
        strongUpdate = true;
        weakUpdate = false;
      }

      if (strongUpdate || weakUpdate) {
        IDFAState succState = getDFA().successor(tuple.state, automatonLabel);
        if ((!tuple.state.isAccepting()) && succState.isAccepting()) {
          if (AbstractWholeProgramSolver.NO_LIBRARY_ERRORS && !nodeInApplication(caller)) {
            // don't report the error.
          } else {
            getQuadDomain().addMessage(makeMessage(tuple));
          }
          // don't propagate accepting state ... use old state instead
          succState = tuple.state;
        }
        int newStateIndex = getQuadDomain().findOrCreate(tuple.instance, succState, tuple.isUnique(), tuple.aux);
        result.add(newStateIndex);
        if (weakUpdate) {
          result.add(d1);
        }
      } else {
        result.add(d1);
      }
      return result;
    }

    protected boolean strongUpdateBasedOnUnique(QuadFactoid tuple, PointerKey x) {
      if (uniqueMayTarget && tuple.isUnique()) {
        OrdinalSet<InstanceKey> possibleReceivers = getPointerAnalysis().getPointsToSet(x);
        return possibleReceivers.contains(tuple.instance);
      } else {
        return false;
      }
    }

    protected TypeStateMessage makeMessage(QuadFactoid tuple) {
      return new TypeStateMessage(getDFAAsProperty(), tuple, caller, block, invokeInstr, caller, tuple.aux);
    }

    /**
     * @return Returns the automatonLabel.
     */
    protected IEvent getAutomatonLabel() {
      return automatonLabel;
    }

    /**
     * @return Returns the caller.
     */
    protected CGNode getCaller() {
      return caller;
    }

    /**
     * @return Returns the callee.
     */
    protected CGNode getCallee() {
      return callee;
    }

    protected SSAInvokeInstruction getCall() {
      return invokeInstr;
    }
  }

  /**
   * @param event
   *            event label in the DFA
   * @param block
   *            basic block of the call instruction
   * @param invokeInstr
   *            invoke instruction which induces the event
   * @param caller
   *            calling node
   */
  protected IUnaryFlowFunction makeEventFlowFunction(IEvent event, BasicBlockInContext<IExplodedBasicBlock> block, SSAInvokeInstruction invokeInstr,
      CGNode caller, CGNode callee) {
    return new EventFlowFunction(event, block, invokeInstr, caller, callee);
  }
}
