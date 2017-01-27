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

package com.ibm.safe.typestate.ap.must.mustnot;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.accesspath.AccessPath;
import com.ibm.safe.accesspath.AccessPathSet;
import com.ibm.safe.accesspath.AccessPathSetTransformers;
import com.ibm.safe.accesspath.InstanceFieldPathElement;
import com.ibm.safe.accesspath.LocalPathElement;
import com.ibm.safe.accesspath.PathElement;
import com.ibm.safe.accesspath.PointerPathElement;
import com.ibm.safe.accesspath.StaticFieldPathElement;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.typestate.ap.TemporaryParameterPointerKey;
import com.ibm.safe.typestate.ap.must.MustAPFunctionProvider;
import com.ibm.safe.typestate.ap.must.MustAuxiliary;
import com.ibm.safe.typestate.core.AbstractWholeProgramSolver;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.quad.QuadFactoid;
import com.ibm.safe.typestate.quad.QuadTypeStateDomain;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AbstractPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSite;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.GraphReachability;
import com.ibm.wala.util.graph.traverse.BFSPathFinder;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * @author sfink
 * @author yahave
 */
public class MustMustNotAPFunctionProvider extends MustAPFunctionProvider {
  final static boolean PARANOID = false;

  private final boolean DEBUG_STACK_TRACE = false;

  private int nMessages;

  public MustMustNotAPFunctionProvider(CallGraph cg, PointerAnalysis pointerAnalysis, ICFGSupergraph supergraph,
      QuadTypeStateDomain domain, ITypeStateDFA dfa, Collection<InstanceKey> trackedInstances, AccessPathSetTransformers apst,
      GraphReachability<CGNode,CGNode> reach, TypeStateOptions options, ILiveObjectAnalysis live, TraceReporter traceReporter)
      throws PropertiesException {
    super(cg, pointerAnalysis, supergraph, domain, dfa, trackedInstances, apst, reach, options, live, traceReporter);
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
  protected IUnaryFlowFunction makeEventFlowFunction(IEvent event, BasicBlockInContext<IExplodedBasicBlock> block,
      SSAInvokeInstruction invokeInstr, CGNode caller, CGNode callee) {
    return new EventFlow(event, block, invokeInstr, caller, callee);
  }

  protected IUnaryFlowFunction makeLocalRenameFlowFunction(AbstractPointerKey x, AbstractPointerKey y) {
    return new LocalRenameFlow(new LocalPathElement(x), new LocalPathElement(y));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.ap.AccessPathFunctionProvider#makeAStoreFlowFunction
   * (com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey,
   * com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey)
   */
  protected IUnaryFlowFunction makeAStoreFlowFunction(LocalPointerKey x, LocalPointerKey y) {
    return new AStoreFlow(x, y);
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.ibm.safe.typestate.ap.AccessPathFunctionProvider#
   * makeNonEventCallFlowFunction(com.ibm.wala.ssa.SSAInvokeInstruction,
   * com.ibm.wala.ipa.callgraph.CGNode, com.ibm.wala.ipa.callgraph.CGNode)
   */
  protected IUnaryFlowFunction makeCallFlowFunction(CGNode caller, CGNode callee, SSAInvokeInstruction call) {
    return new CallFlow(caller, callee, call);
  }

  protected IUnaryFlowFunction makeReturnFlowFunction(Set<PathElement> retValElements, CGNode caller, CGNode callee,
      SSAInvokeInstruction call) {
    return new ReturnFlow(retValElements, caller, callee, call);
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.ibm.safe.typestate.ap.must.AbstractMustAPFunctionProvider#
   * makeAllocFlowFunction(com.ibm.wala.ipa.callgraph.propagation.InstanceKey,
   * com.ibm.safe.typestate.IDFAState,
   * com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey)
   */
  protected IUnaryFlowFunction makeAllocFlowFunction(InstanceKey ik, IDFAState initial, LocalPointerKey pk) {
    return new AllocFlow(ik, initial, pk);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.ap.AccessPathFunctionProvider#makePutFieldFlowFunction
   * (com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey,
   * com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey,
   * com.ibm.wala.classLoader.IField)
   */
  protected IUnaryFlowFunction makePutFieldFlowFunction(LocalPointerKey x, LocalPointerKey y, IField f) {
    return new PutFieldFlow(x, y, f);
  }

  /**
   * x = y.f
   */
  private class GetFieldFlow extends LocalAssignFlow {
    public GetFieldFlow(LocalPointerKey x, LocalPointerKey y, IField f) {
      super(new LocalPathElement(x), getAPDictionary().concat(new LocalPathElement(y), new InstanceFieldPathElement(f)));
    }
  }

  /**
   * x = Y.f
   */
  private class GetStaticFlow extends LocalAssignFlow {
    public GetStaticFlow(LocalPointerKey x, StaticFieldKey Y_f) {
      super(new LocalPathElement(x), getAPDictionary().findOrCreate(new StaticFieldPathElement(Y_f)));
      assert x != null && Y_f != null : "cannot create flow function with null receiver or static field";
    }
  }

  void paranoidCheck(int fIndex) {
    QuadFactoid f = (QuadFactoid) getQuadDomain().getMappedObject(fIndex);
    paranoidCheck(f);
  }

  void paranoidCheck(QuadFactoid f) {
    MustMustNotAuxiliary aux = (MustMustNotAuxiliary) f.aux;
    AccessPathSet must = aux.getMustPaths();
    if (must.isEmpty() && aux.isComplete()) {
      Assertions.UNREACHABLE("empty-complete " + f);
    }
    AccessPathSet mustNot = aux.getMustNotPaths();
    for (Iterator<AccessPath> it = mustNot.iterator(); it.hasNext();) {
      AccessPath ap = it.next();
      if (ap.length() == 1) {
        PointerPathElement p = (PointerPathElement) ap.getHead();
        if (!(p.getPointerKey() instanceof TemporaryParameterPointerKey)) {
          OrdinalSet<InstanceKey> ptsTo = getPointerAnalysis().getPointsToSet(p.getPointerKey());
          if (!ptsTo.contains(f.instance)) {
            Assertions.UNREACHABLE("p " + p + " ik " + f.instance);
          }
        }
      }
    }
  }

  /**
   * a flow function that changes the state of an instance in response to an
   * event
   */
  private class EventFlow extends EventFlowFunction {

    public EventFlow(IEvent event, BasicBlockInContext<IExplodedBasicBlock> block, SSAInvokeInstruction invokeInstr, CGNode caller,
        CGNode callee) {
      super(event, block, invokeInstr, caller, callee);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction#getTargets(int)
     */
    public SparseIntSet getTargets(int d1) {

      if (d1 == 0) {
        return SparseIntSet.singleton(0);
      }

      QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d1);
      MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
      AccessPathSet must = aux.getMustPaths();
      AccessPathSet mustNot = aux.getMustNotPaths();

      LocalPointerKey thisPtr = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(getCallee(), 1);
      AccessPath thisPath = getAPDictionary().findOrCreate(new LocalPathElement(thisPtr));

      if (DEBUG_LEVEL > 2) {
        Trace.print("EventFlow for " + d1 + ":" + tuple);
      }

      if (mustNot.contains(thisPath)) {
        // the must-not information tells us that this call cannot possibly
        // apply. so just return the old factoid.
        return SparseIntSet.singleton(d1);
      }

      boolean strongUpdate = must.contains(thisPath);
      boolean weakUpdate = false;
      if (strongUpdate && AccessPathSetTransformers.containsArrayPath(must)) {
        // an array path element precludes the possibility of strong updates.
        strongUpdate = false;
        weakUpdate = true;
      } else if (!strongUpdate) {
        OrdinalSet<InstanceKey> possibleReceivers = getPointerAnalysis().getPointsToSet(
            getReceiverPointerKey(getCall(), getCaller()));
        if (possibleReceivers.contains(tuple.instance)) {
          PointerKey r = getPointerAnalysis().getHeapModel().getPointerKeyForLocal(getCallee(), 1);
          possibleReceivers = getPointerAnalysis().getPointsToSet(r);
          weakUpdate = !aux.isComplete() && possibleReceivers.contains(tuple.instance);
        }
      }
      if (weakUpdate && strongUpdateBasedOnUnique(tuple, thisPtr)) {
        strongUpdate = true;
        weakUpdate = false;
      }

      if (strongUpdate || weakUpdate) {
        IDFAState succState = getDFA().successor(tuple.state, getAutomatonLabel());
        succState = updateForAcceptState(d1, tuple, succState);
        int newStateIndex = getQuadDomain().findOrCreate(tuple.instance, succState, tuple.isUnique(), tuple.aux);
        if (PARANOID) {
          paranoidCheck(newStateIndex);
        }
        if (weakUpdate && (!succState.equals(tuple.state))) {
          // FOCUS!! we took a weak update and changed state. generate 2
          // factoids based
          // on the focus operation!!!

          // if we take the state change, this must point to the instance.
          AccessPathSet positiveMust = new AccessPathSet(must);
          positiveMust.add(thisPath);
          MustMustNotAuxiliary positiveAux = new MustMustNotAuxiliary(positiveMust, mustNot, aux.isComplete());
          int focusPositive = getQuadDomain().findOrCreate(tuple.instance, succState, tuple.isUnique(), positiveAux);

          // if we don't take the state change, this must not point to the
          // instance
          AccessPathSet negativeMustNot = new AccessPathSet(getAPDictionary(), thisPath);
          // retain static fields from old mustnot
          for (Iterator<AccessPath> it = mustNot.iterator(); it.hasNext();) {
            AccessPath ap = it.next();
            if (ap.length() == 1) {
              if (ap.getHead() instanceof StaticFieldPathElement) {
                negativeMustNot.add(ap);
              }
            }
          }
          MustMustNotAuxiliary negativeAux = new MustMustNotAuxiliary(must, negativeMustNot, aux.isComplete());
          int focusNegative = getQuadDomain().findOrCreate(tuple.instance, succState, tuple.isUnique(), negativeAux);

          if (DEBUG_LEVEL > 2) {
            Trace.println("Focued to " + focusPositive + " and " + focusNegative);
          }

          // generate both factoids from the focus
          return SparseIntSet.pair(focusPositive, focusNegative);
        } else {

          if (DEBUG_LEVEL > 2) {
            Trace.println("Updated to " + newStateIndex);
          }

          // did not focus. just update the state.
          return SparseIntSet.singleton(newStateIndex);
        }
      } else {
        if (DEBUG_LEVEL > 2) {
          Trace.println("No change in state " + d1);
        }
        // applied neither a strong update nor a weak update.
        // no change in state.
        return SparseIntSet.singleton(d1);
      }
    }

    private IDFAState updateForAcceptState(int d1, QuadFactoid tuple, IDFAState succState) {
      if ((!tuple.state.isAccepting()) && succState.isAccepting()) {
        if (AbstractWholeProgramSolver.NO_LIBRARY_ERRORS && !nodeInApplication(getCaller())) {
          // don't report the error.
        } else {
          if (DEBUG_LEVEL > 2) {
            Trace.println("Error reported for " + succState + " from " + d1);
          }
          debugStackTrace(d1, tuple);
          getQuadDomain().addMessage(makeMessage(tuple));
        }
        // don't propagate accepting state ... use old state instead
        succState = tuple.state;
      }
      return succState;
    }

    private void debugStackTrace(int d1, QuadFactoid tuple) {
      // System.err.println("Generating message for " + d1 + " " + tuple +
      // " " + makeMessage(tuple));
      // Trace.println("Generating message for " + d1 + " " + tuple + " "
      // + makeMessage(tuple));
      nMessages++;
      if (DEBUG_STACK_TRACE && nMessages == 1) {
        System.err.println("STACK TRACE");
        Trace.println("STACK TRACE");
        System.err.println(d1 + " " + makeMessage(tuple));
        Trace.println(d1 + " " + makeMessage(tuple));
        BFSPathFinder<CGNode> bfs = new BFSPathFinder<CGNode>(getCallGraph(), getCallGraph().getFakeRootNode(), getCallee());
        List<CGNode> L = bfs.find();
        for (Iterator<CGNode> it = L.iterator(); it.hasNext();) {
          Object o = it.next();
          System.err.println(o);
          Trace.println(o);
        }
      }
    }
  }

  /**
   * flow function which filters assignments based on a declared type (e.g. for
   * checkcast)
   */
  private class FilteredAssignFlow extends CheckCastFlowFunction {

    public FilteredAssignFlow(LocalPathElement lhs, LocalPathElement rhs, TypeReference T) {
      super(lhs, rhs, T);
    }

    /*
     * (non-Javadoc)
     * 
     * @seecom.ibm.safe.typestate.ap.must.AbstractMustAPFunctionProvider.
     * LocalAssignFlowFunction#getTargets(int)
     */
    public SparseIntSet getTargets(int d1) {
      if (d1 == 0) {
        return SparseIntSet.singleton(0);
      } else {
        QuadFactoid tuple = (QuadFactoid) getDomain().getMappedObject(d1);
        MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
        AccessPathSet tempMust = assign(aux.getMustPaths(), getLhs(), getRhsPath());
        // filter out paths where the first instance on the path cannot be
        // proved to be of the correct type.
        // currently our access paths don't carry type information, so we have
        // to give up on any paths
        // that have length>1, since our only type information is for paths of
        // length 1 which point directly
        // to the instance.
        AccessPathSet newMust = new AccessPathSet(getAPDictionary());

        boolean complete = aux.isComplete();
        for (Iterator<AccessPath> it = tempMust.iterator(); it.hasNext();) {
          AccessPath ap = it.next();
          if (ap.getHead().equals(getLhs())) {
            if (ap.length() == 1 && getKlass() != null) {
              IClass concreteType = tuple.instance.getConcreteType();
              if (getKlass().isInterface() && getCallGraph().getClassHierarchy().implementsInterface(concreteType, getKlass())) {
                newMust.add(ap);
              } else {
                // ! klass.isInterface
                if (getCallGraph().getClassHierarchy().isSubclassOf(concreteType, getKlass())) {
                  newMust.add(ap);
                }
              }
            } else {
              // we've given up on maintaining a must alias because we did not
              // have enough information.
              // but the may alias might still exist
              complete = false;
            }
          } else {
            // an access path which does not start with the lhs. propagate it.
            newMust.add(ap);
          }
        }

        AccessPathSet tempMustNot = assign(aux.getMustNotPaths(), getLhs(), getRhsPath());
        AccessPathSet newMustNot = new AccessPathSet(getAPDictionary());
        for (Iterator<AccessPath> it = tempMustNot.iterator(); it.hasNext();) {
          AccessPath ap = it.next();
          if (ap.getHead().equals(getLhs())) {
            if (ap.length() == 1 && getKlass() != null) {
              IClass concreteType = tuple.instance.getConcreteType();
              if (getKlass().isInterface() && getCallGraph().getClassHierarchy().implementsInterface(concreteType, getKlass())) {
                newMustNot.add(ap);
              } else {
                // ! klass.isInterface
                if (getCallGraph().getClassHierarchy().isSubclassOf(concreteType, getKlass())) {
                  newMustNot.add(ap);
                }
              }
            } else {
              // we've given up on maintaining a must alias because we did not
              // have enough information.
              // but the may alias might still exist
              complete = false;
            }
          } else {
            // an access path which does not start with the lhs. propagate it.
            newMustNot.add(ap);
          }
        }

        MustMustNotAuxiliary newAux = new MustMustNotAuxiliary(newMust, newMustNot, complete);
        int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(), newAux);
        if (PARANOID) {
          paranoidCheck(newTupleIndex);
        }
        return SparseIntSet.singleton(newTupleIndex);
      }
    }
  }

  /**
   * General flow function for an assignment to a local*
   * 
   * @author Stephen Fink
   * @author yahave
   */
  private class LocalAssignFlow extends LocalAssignFlowFunction {
    public LocalAssignFlow(LocalPathElement lhs, AccessPath rhs) {
      super(lhs, rhs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction#getTargets(int)
     */
    public SparseIntSet getTargets(int d1) {
      if (d1 == 0) {
        return SparseIntSet.singleton(0);
      } else {
        QuadFactoid tuple = (QuadFactoid) getDomain().getMappedObject(d1);
        MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
        AccessPathSet newMust = assign(aux.getMustPaths(), getLhs(), getRhs());
        AccessPathSet newMustNot = assign(aux.getMustNotPaths(), getLhs(), getRhs());
        // if the must set for the rhs was complete, then the new must set is
        // also complete.
        MustMustNotAuxiliary newAux = new MustMustNotAuxiliary(newMust, newMustNot, aux.isComplete());
        int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(), newAux);
        if (PARANOID) {
          paranoidCheck(newTupleIndex);
        }
        return SparseIntSet.singleton(newTupleIndex);
      }
    }

  }

  /**
   * General flow function for an assignment to a local, when the rhs is dead
   * after the assignment
   */
  private class LocalRenameFlow extends LocalRenameFlowFunction {

    public LocalRenameFlow(LocalPathElement lhs, PathElement rhs) {
      super(lhs, rhs);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction#getTargets(int)
     */
    public SparseIntSet getTargets(int d1) {
      if (d1 == 0) {
        return SparseIntSet.singleton(0);
      } else {
        QuadFactoid tuple = (QuadFactoid) getDomain().getMappedObject(d1);
        MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
        AccessPathSet newMust = rename(aux.getMustPaths(), getLhs(), Collections.singleton(getRhs()));
        AccessPathSet newMustNot = rename(aux.getMustNotPaths(), getLhs(), Collections.singleton(getRhs()));
        // if the must set for the rhs was complete, then the new must set is
        // also complete.
        MustMustNotAuxiliary newAux = new MustMustNotAuxiliary(newMust, newMustNot, aux.isComplete());
        int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(), newAux);
        if (PARANOID) {
          paranoidCheck(newTupleIndex);
        }
        return SparseIntSet.singleton(newTupleIndex);
      }
    }
  }

  /**
   * Flow Function for a "new" statement
   */
  private class AllocFlow extends AllocFlowFunction {

    /*
     * (non-Javadoc)
     * 
     * @seecom.ibm.safe.typestate.ap.must.AbstractMustAPFunctionProvider.
     * AllocFlowFunction#getTargets(int)
     */
    public SparseIntSet getTargets(int d1) {
      MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();

      if (d1 != 0) {
        Object domainItem = getQuadDomain().getMappedObject(d1);
        QuadFactoid tuple = (QuadFactoid) domainItem;

        MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
        AccessPathSet newMup = kill(aux.getMustPaths(), getLhsElement());

        // if the old must set was complete, it's still complete since we've
        // only killed the x path. In either case, the unique attribute must now
        // be false.

        // additionally, we can add must-not to the old fact.
        AccessPathSet mustNot = aux.getMustNotPaths();
        if (!aux.isComplete()) {
          mustNot = new AccessPathSet(mustNot);
          mustNot.add(getAPDictionary().findOrCreate(getLhsElement()));
        }

        int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, false,
            new MustMustNotAuxiliary(newMup, mustNot, aux.isComplete()));
        result.add(newTupleIndex);
        result.add(getNotUniqueAlloc());
      } else {
        result.add(getUniqueAlloc());
        result.add(0);
      }
      return result;
    }

    public AllocFlow(InstanceKey ik, IDFAState initial, LocalPointerKey pk) {
      super(ik, initial, pk);
    }
  }

  /**
   * x[i] = y
   */
  private class AStoreFlow extends AStoreFlowFunction {

    public AStoreFlow(LocalPointerKey x, LocalPointerKey y) {
      super(x, y);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction#getTargets(int)
     */
    public SparseIntSet getTargets(int d1) {

      if (d1 == 0) {
        return SparseIntSet.singleton(0);
      }
      MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();

      QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d1);
      MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
      AccessPathSet must = aux.getMustPaths();
      AccessPathSet newMust = null;
      if (!aux.isComplete()) {
        // no point adding a must array path to a not-complete factoid, is
        // there?
        // just ignore it.
        newMust = must;
      } else {
        newMust = updateMust(must, getXContentsPath(), getYPathElement());
      }

      boolean complete = false;
      if (aux.isComplete()) {
        complete = containsAllMayAliasPaths(getNode(), newMust, getX(), getAPDictionary().findOrCreate(
            getXContentsPath().getSuffix(1)));
      }

      int size = newMust.size();
      newMust = apsTransformer.kLimit(newMust, accessPathKLimit);
      complete = (newMust.size() == size) ? complete : false;

      if (!complete) {
        AccessPathSetTransformers.removeArrayPaths(newMust);
      }

      int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(),
          new MustMustNotAuxiliary(newMust, aux.getMustNotPaths(), complete));
      if (PARANOID) {
        paranoidCheck(newTupleIndex);
      }
      result.add(newTupleIndex);

      return result;
    }
  }

  /**
   * return x;
   */
  private class ReturnFlow extends ReturnFlowFunction {

    ReturnFlow(Set<PathElement> retValElements, CGNode caller, CGNode callee, SSAInvokeInstruction call) {
      super(call, retValElements, caller, callee);
    }

    /**
     * @param d1
     *          - factoid number
     * @return target factoids
     */
    public SparseIntSet getTargets(int d1) {

      if (d1 == 0) {
        return SparseIntSet.singleton(0);
      } else {
        QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d1);
        if (getLiveObjectAnalysis() != null) {
          // kill factoids based on liveness analysis
          if (tuple.instance instanceof AllocationSite) {
            AllocationSite ak = (AllocationSite) tuple.instance;
            try {
              if (!getLiveObjectAnalysis().mayBeLive(ak, getCaller(), -1)) {
                return SparseIntSet.singleton(0);
              }
            } catch (WalaException e) {
              e.printStackTrace();
              Assertions.UNREACHABLE(tuple.instance.toString());
            }
          } else {
            // this is OK. just continue with normal logic
            // Assertions.UNREACHABLE(tuple.instance.getClass().toString());
          }
        }

        if (DEBUG_LEVEL > 1) {
          Trace.println("-----------------------------------------------------");
          Trace.println("(APMMN) ReturnEdge for " + d1);
        }

        MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
        AccessPathSet must = aux.getMustPaths();
        AccessPathSet mustNot = aux.getMustNotPaths();

        if (!getRetValElements().isEmpty()) {
          // add assignment of return value to invoke-instruction LHS
          PathElement symRetVal = new LocalPathElement(TemporaryParameterPointerKey.makeReturnValue());
          must = rename(must, symRetVal, getRetValElements());
          mustNot = rename(mustNot, symRetVal, getRetValElements());
        }
        // re-establish must, mustNot for parameters!
        IMethod calleeMethod = getCallee().getMethod();
        int numOfParams = calleeMethod.getNumberOfParameters();
        for (int i = 0; i < numOfParams; i++) {
          // we rely on the invariant that the ith formal parameter has value
          // number i+1
          int formal = i + 1;
          if (calleeMethod.getParameterType(i).isReferenceType()) {
            LocalPointerKey formalPointerKey = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(
                getCallee(), formal);
            PathElement formalPathElement = new LocalPathElement(formalPointerKey);

            LocalPointerKey act = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(getCaller(),
                call.getUse(i));
            mustNot = rename(mustNot, new LocalPathElement(act), Collections.singleton(formalPathElement));
            must = rename(must, new LocalPathElement(act), Collections.singleton(formalPathElement));

            // if we can refine the must-not set from this calling context, do
            // it with a focus.
            // Note that there's no point in focusing if the factoid is
            // complete, since a complete factoid already
            // holds all possible information.
            if (!aux.isComplete()) {
              OrdinalSet<InstanceKey> actualPointsTo = getPointerAnalysis().getPointsToSet(act);
              if (actualPointsTo.contains(tuple.instance)) {
                OrdinalSet<InstanceKey> formalPointsTo = getPointerAnalysis().getPointsToSet(formalPointerKey);
                if (!formalPointsTo.contains(tuple.instance)) {
                  // in this calling context, it's impossible for the actual to
                  // point to the instance.
                  // mustNot.add(new AccessPath(formalPathElement));
                  // to limit blow-up, chuck the old information and stick with
                  // the
                  // new.

                  AccessPathSet newMustNot = new AccessPathSet(getAPDictionary(), getAPDictionary().findOrCreate(
                      new LocalPathElement(act)));
                  // retain mustNot for statics
                  for (Iterator<AccessPath> it = mustNot.iterator(); it.hasNext();) {
                    AccessPath ap = it.next();
                    if (ap.length() == 1) {
                      if (ap.getHead() instanceof StaticFieldPathElement) {
                        newMustNot.add(ap);
                      }
                    }
                  }
                  mustNot = newMustNot;
                }
              } else {
                // actual cannot point to this instance.
                mustNot = kill(mustNot, new LocalPathElement(act));
              }
            }
          }
        }

        must = killOutOfScopeLocals(must, getCaller());
        // we purposely give up if there's recursion, to avoid an
        // explosion of propagation
        must = killLocals(must, getCallee());
        mustNot = killOutOfScopeLocals(mustNot, getCaller());
        mustNot = killLocals(mustNot, getCallee());

        boolean unique = tuple.isUnique();
        if (!unique && canForceToUnique(tuple)) {
          unique = true;
        }
        if (aux.isComplete() && must.isEmpty()) {
          // the instance must be dead
          return SparseIntSet.singleton(0);
        } else {
          // we've only transferred must aliases from return value to the
          // caller.
          // the must information completeness has not changed.
          int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, unique,
              new MustMustNotAuxiliary(must, mustNot, aux.isComplete()));
          if (PARANOID) {
            paranoidCheck(newTupleIndex);
          }
          return SparseIntSet.singleton(newTupleIndex);
        }
      }
    }
  }

  /**
   * X.f = y
   * 
   * @author Eran Yahav (yahave)
   * @author Stephen Fink
   */
  public class PutStaticFlow extends PutStaticFlowFunction {

    /**
     * @param X_f
     * @param y
     */
    public PutStaticFlow(StaticFieldKey X_f, LocalPointerKey y) {
      super(X_f, y);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction#getTargets(int)
     */
    public SparseIntSet getTargets(int d1) {

      if (d1 == 0) {
        return SparseIntSet.singleton(0);
      } else {
        QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d1);
        MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
        AccessPathSet must = aux.getMustPaths();
        must = updateMust(must, getX_fPath(), getYPathElement());
        AccessPathSet mustNot = aux.getMustNotPaths();
        mustNot = updateMust(mustNot, getX_fPath(), getYPathElement());
        if (aux.isComplete() && must.size() == 0) {
          return SparseIntSet.singleton(0);
        } else {

          int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(),
              new MustMustNotAuxiliary(must, mustNot, aux.isComplete()));
          if (PARANOID) {
            paranoidCheck(newTupleIndex);
          }
          return SparseIntSet.singleton(newTupleIndex);
        }
      }
    }
  }

  /**
   * x.f = y
   */
  private class PutFieldFlow extends PutFieldFlowFunction {

    public PutFieldFlow(LocalPointerKey x, LocalPointerKey y, IField f) {
      super(x, y, f);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction#getTargets(int)
     */
    public SparseIntSet getTargets(int d1) {

      if (d1 == 0) {
        return SparseIntSet.singleton(0);
      }
      MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
      QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d1);
      MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
      AccessPathSet must = aux.getMustPaths();
      AccessPathSet newMust = updateMust(must, getX_fPath(), getYPathElement());
      AccessPathSet mustNot = aux.getMustNotPaths();
      AccessPathSet newMustNot = updateMust(mustNot, getX_fPath(), getYPathElement());

      boolean complete = false;
      if (aux.isComplete()) {
        complete = containsAllMayAliasPaths(getNode(), newMust, getX(), getAPDictionary().findOrCreate(getX_fPath().getSuffix(1)));
      }

      int size = newMust.size();
      newMust = apsTransformer.kLimit(newMust, accessPathKLimit);
      complete = (newMust.size() == size) ? complete : false;
      newMustNot = apsTransformer.kLimit(newMustNot, accessPathKLimit);

      int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(),
          new MustMustNotAuxiliary(newMust, newMustNot, complete));
      if (PARANOID) {
        paranoidCheck(newTupleIndex);
      }
      result.add(newTupleIndex);

      return result;
    }
  }

  /**
   * x.foo()
   */
  private class CallFlow extends CallFlowFunction {

    private final SSAInvokeInstruction call;

    private final CGNode caller;

    public CallFlow(CGNode caller, CGNode callee, SSAInvokeInstruction call) {
      super(callee, call.getCallSite());
      this.call = call;
      this.caller = caller;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction#getTargets(int)
     */
    public SparseIntSet getTargets(int d1) {

      if (d1 == 0) {
        return SparseIntSet.singleton(0);
      } else {
        QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d1);
        if (!isFeasibleCall(tuple)) {
          // really we'd like to just ignore the callee.
          // for now, just propagate zero into it.
          // TODO: support flow functions that don't even propagate zero
          // factoid,
          // to indicate unreachable code?
          return SparseIntSet.singleton(0);
        }
        IMethod calleeMethod = getCallee().getMethod();
        int numOfParams = calleeMethod.getNumberOfParameters();
        MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
        AccessPathSet must = aux.getMustPaths();
        AccessPathSet mustNot = aux.getMustNotPaths();

        // in case of recursion, clean up the must and must not sets to
        // be safe.
        boolean newFactIsComplete = aux.isComplete();
        mustNot = killLocals(mustNot, getCallee());
        int oldMustSize = must.size();
        must = killLocals(must, getCallee());
        if (must.size() < oldMustSize) {
          // we killed some must alias to be safe for recursion. we must
          // sacrifice
          // completeness.
          newFactIsComplete = false;
        }

        // note:
        // for InvokeInstr - params[0] is this (for non static methods)
        // for calleeIR, the 0th parameter is this (for non static methods)
        // for IMethod, the 0-th parameter is (again) this (for non static
        // methods)
        for (int i = 0; i < numOfParams; i++) {
          // we rely on the invariant that the ith formal parameter has value
          // number i+1
          int formal = i + 1;
          if (calleeMethod.getParameterType(i).isReferenceType()) {
            TemporaryParameterPointerKey actualPointerKey = TemporaryParameterPointerKey.make(i);
            LocalPointerKey formalPointerKey = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(
                getCallee(), formal);
            PathElement formalPathElement = new LocalPathElement(formalPointerKey);
            must = rename(must, formalPathElement, Collections.singleton(new LocalPathElement(actualPointerKey)));
            mustNot = rename(mustNot, formalPathElement, Collections.singleton(new LocalPathElement(actualPointerKey)));

            // if we can refine the must-not set from this calling context, do
            // it with a focus.
            // Note that there's no point in focusing if the factoid is
            // complete, since a complete factoid already
            // holds all possible information.
            if (!newFactIsComplete) {
              OrdinalSet<InstanceKey> formalPointsTo = getPointerAnalysis().getPointsToSet(formalPointerKey);
              if (formalPointsTo.contains(tuple.instance)) {
                LocalPointerKey act = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(caller,
                    call.getUse(i));
                OrdinalSet<InstanceKey> actualPointsTo = getPointerAnalysis().getPointsToSet(act);
                if (!actualPointsTo.contains(tuple.instance)) {
                  // in this calling context, it's impossible for the formal to
                  // point to the instance.
                  // mustNot.add(new AccessPath(formalPathElement));
                  // to limit blow-up, chuck the old information and stick with
                  // the
                  // new.

                  AccessPathSet newMustNot = new AccessPathSet(getAPDictionary(), getAPDictionary().findOrCreate(formalPathElement));
                  // but keep static fields in must not
                  for (Iterator<AccessPath> it = mustNot.iterator(); it.hasNext();) {
                    AccessPath ap = it.next();
                    if (ap.length() == 1) {
                      if (ap.getHead() instanceof StaticFieldPathElement) {
                        newMustNot.add(ap);
                      }
                    }
                  }
                  mustNot = newMustNot;
                }
              } else {
                // formal cannot point to this instance.
                mustNot = kill(mustNot, formalPathElement);
              }
            }

            // kill the actuals from must not. we'll re-establish them
            // during return if appropriate
            LocalPointerKey act = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(caller,
                call.getUse(i));
            mustNot = kill(mustNot, new LocalPathElement(act));

            // kill the actuals from must. we'll re-establish them
            // during return if appropriate. note that we only do this if we've
            // given up on complete, so it's sound.
            // TODO: forcibly drops musts and complete when it's profitable
            if (!newFactIsComplete) {
              must = kill(must, new LocalPathElement(act));
            }
          }
        }

        mustNot = pruneMustNotByPointerAnalysis(tuple.instance, mustNot);

        // we've done a bunch of local assignments to the formal parameters. If
        // the must paths before these assignments were complete,
        // then they're still complete
        int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(),
            new MustMustNotAuxiliary(must, mustNot, newFactIsComplete));
        if (PARANOID) {
          paranoidCheck(newTupleIndex);
        }
        return SparseIntSet.singleton(newTupleIndex);
      }
    }

    /**
     * Throw out must-not paths that can't happen according to pointer analysis.
     * TODO: enhance this.
     */
    private AccessPathSet pruneMustNotByPointerAnalysis(InstanceKey instance, AccessPathSet mustNot) {
      AccessPathSet result = new AccessPathSet(mustNot);
      for (Iterator<AccessPath> it = mustNot.iterator(); it.hasNext();) {
        AccessPath ap = it.next();
        if (ap.length() == 1) {
          PointerPathElement p = (PointerPathElement) ap.getHead();
          if (!(p.getPointerKey() instanceof TemporaryParameterPointerKey)) {
            OrdinalSet<InstanceKey> ptsTo = getPointerAnalysis().getPointsToSet(p.getPointerKey());
            if (!ptsTo.contains(instance)) {
              result.remove(ap);
            }
          }
        }
      }
      return result;
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.ap.must.AbstractMustAPFunctionProvider#makeMustAuxiliary
   * (com.ibm.safe.accesspath.AccessPathSet, boolean)
   */
  protected MustAuxiliary makeMustAuxiliary(AccessPathSet mustSet, boolean complete) {
    return new MustMustNotAuxiliary(mustSet, new AccessPathSet(getAPDictionary()), complete);
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.ibm.safe.typestate.ap.must.AbstractMustAPFunctionProvider#
   * makeLocalAssignFlowFunction(com.ibm.safe.accesspath.LocalPathElement,
   * com.ibm.safe.accesspath.AccessPath)
   */
  protected IUnaryFlowFunction makeLocalAssignFlowFunction(LocalPathElement lhs, AccessPath rhs) {
    return new LocalAssignFlow(lhs, rhs);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.ap.AccessPathFunctionProvider#makeCheckCastFlowFunction
   * (com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey,
   * com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey,
   * com.ibm.wala.types.TypeReference)
   */
  protected IUnaryFlowFunction makeCheckCastFlowFunction(LocalPointerKey x, LocalPointerKey y, TypeReference T) {
    return new FilteredAssignFlow(new LocalPathElement(x), new LocalPathElement(y), T);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.ap.AccessPathFunctionProvider#makeGetStaticFlowFunction
   * (com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey,
   * com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey)
   */
  protected IUnaryFlowFunction makeGetStaticFlowFunction(LocalPointerKey x, StaticFieldKey Y_f) {
    return new GetStaticFlow(x, Y_f);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.ap.AccessPathFunctionProvider#makeGetFieldFlowFunction
   * (com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey,
   * com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey,
   * com.ibm.wala.classLoader.IField)
   */
  protected IUnaryFlowFunction makeGetFieldFlowFunction(LocalPointerKey x, LocalPointerKey y, IField f) {
    return new GetFieldFlow(x, y, f);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.ap.AccessPathFunctionProvider#makePutStaticFlowFunction
   * (com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey,
   * com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey)
   */
  protected IUnaryFlowFunction makePutStaticFlowFunction(StaticFieldKey X_f, LocalPointerKey y) {
    return new PutStaticFlow(X_f, y);
  }

  protected IUnaryFlowFunction makeDeadAccessPathKiller(CGNode node, ISSABasicBlock b) {
    return new MustMustNotAPDeadKiller(node, b);
  }

  protected class MustMustNotAPDeadKiller implements IUnaryFlowFunction {

    private final CGNode node;

    private final ISSABasicBlock b;

    MustMustNotAPDeadKiller(CGNode node, ISSABasicBlock b) {
      this.node = node;
      this.b = b;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction#getTargets(int)
     */
    public SparseIntSet getTargets(int d1) {
      if (d1 == 0) {
        return SparseIntSet.singleton(0);
      }

      QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d1);
      MustMustNotAuxiliary aux = (MustMustNotAuxiliary) tuple.aux;
      AccessPathSet must = aux.getMustPaths();
      AccessPathSet mustNot = aux.getMustNotPaths();
      int oldMustSize = must.size();
      int oldMustNotSize = mustNot.size();
      must = removeDeadPaths(must, node, b, node.getIR(), node.getDU());
      mustNot = removeDeadPaths(mustNot, node, b, node.getIR(), node.getDU());
      if (must.size() < oldMustSize || mustNot.size() < oldMustNotSize) {
        if (aux.isComplete() && must.size() == 0) {
          // it's dead
          return SparseIntSet.singleton(0);
        } else {
          int newIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(),
              new MustMustNotAuxiliary(must, mustNot, aux.isComplete()));
          return SparseIntSet.singleton(newIndex);
        }
      } else {
        return SparseIntSet.singleton(d1);
      }
    }
  }
}
