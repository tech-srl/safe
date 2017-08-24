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
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.accesspath.AccessPath;
import com.ibm.safe.accesspath.AccessPathSet;
import com.ibm.safe.accesspath.AccessPathSetTransformers;
import com.ibm.safe.accesspath.ArrayContentsPathElement;
import com.ibm.safe.accesspath.InstanceFieldPathElement;
import com.ibm.safe.accesspath.LocalPathElement;
import com.ibm.safe.accesspath.PathElement;
import com.ibm.safe.accesspath.StaticFieldPathElement;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.typestate.ap.AccessPathFunctionProvider;
import com.ibm.safe.typestate.ap.TemporaryParameterPointerKey;
import com.ibm.safe.typestate.base.BaseProgramExitFlowFunction;
import com.ibm.safe.typestate.core.UniversalKillFlowFunction;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.quad.QuadFactoid;
import com.ibm.safe.typestate.quad.QuadTypeStateDomain;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.typestate.unique.UniqueReturnFlowFunction;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.IFDS.IReversibleFlowFunction;
import com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AbstractLocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.AbstractPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSite;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.ArrayContentsKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.ReceiverInstanceContext;
import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.GraphReachability;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * Base class for access path solver that uses must-alias
 * 
 * @author Stephen Fink
 * @author Eran Yahav
 */
public abstract class AbstractMustAPFunctionProvider extends AccessPathFunctionProvider {

  /**
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
   * @throws PropertiesException
   */
  public AbstractMustAPFunctionProvider(CallGraph cg, PointerAnalysis pointerAnalysis, ICFGSupergraph supergraph,
      QuadTypeStateDomain domain, ITypeStateDFA dfa, Collection<InstanceKey> trackedInstances, AccessPathSetTransformers apst,
      GraphReachability<CGNode,CGNode> reach, TypeStateOptions options, ILiveObjectAnalysis live, TraceReporter traceReporter)
      throws PropertiesException {
    super(cg, pointerAnalysis, supergraph, domain, dfa, trackedInstances, apst, reach, options, live, traceReporter);
  }

  public AccessPathSet kLimit(AccessPathSet s, int limit) {
    return apsTransformer.kLimit(s, limit);
  }

  public AccessPathSet kLimitPollution(AccessPathSet s, int limit) {
    return apsTransformer.kLimitPollution(s, limit);
  }

  /**
   * General flow function for an assignment to a local, when the rhs is dead
   * after the assignment
   * 
   * @author Stephen Fink
   */
  protected class LocalRenameFlowFunction implements IUnaryFlowFunction {
    final LocalPathElement lhs;

    final PathElement rhs;

    public LocalRenameFlowFunction(LocalPathElement lhs, PathElement rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
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
        MustAuxiliary must = (MustAuxiliary) tuple.aux;
        AccessPathSet newMup = rename(must.getMustPaths(), lhs, Collections.singleton(rhs));
        // if the must set for the rhs was complete, then the new must set is
        // also complete.
        MustAuxiliary newMust = new MustAuxiliary(newMup, must.isComplete());
        int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(), newMust);
        return SparseIntSet.singleton(newTupleIndex);
      }
    }

    /**
     * @return Returns the lhs.
     */
    protected LocalPathElement getLhs() {
      return lhs;
    }

    /**
     * @return Returns the rhs.
     */
    protected PathElement getRhs() {
      return rhs;
    }
  }

  /**
   * General flow function for an assignment to a local
   * 
   * @author Stephen Fink
   */
  protected class LocalAssignFlowFunction implements IUnaryFlowFunction {
    final LocalPathElement lhs;

    final AccessPath rhs;

    public LocalAssignFlowFunction(LocalPathElement lhs, AccessPath rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
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
        MustAuxiliary must = (MustAuxiliary) tuple.aux;
        AccessPathSet newMup = assign(must.getMustPaths(), lhs, rhs);
        // if the must set for the rhs was complete, then the new must set is
        // also complete.
        MustAuxiliary newMust = new MustAuxiliary(newMup, must.isComplete());
        int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(), newMust);
        return SparseIntSet.singleton(newTupleIndex);
      }
    }

    /**
     * @return Returns the lhs.
     */
    protected LocalPathElement getLhs() {
      return lhs;
    }

    /**
     * @return Returns the rhs.
     */
    protected AccessPath getRhs() {
      return rhs;
    }
  }

  /**
   * @author sfink
   * 
   *         flow function which filters assignments based on a declared type
   *         (e.g. for checkcast)
   */
  protected class CheckCastFlowFunction implements IUnaryFlowFunction {

    private final IClass klass;

    final LocalPathElement lhs;

    final LocalPathElement rhs;

    final AccessPath rhsPath;

    public CheckCastFlowFunction(LocalPathElement lhs, LocalPathElement rhs, TypeReference T) {
      this.lhs = lhs;
      this.rhs = rhs;
      this.klass = getCallGraph().getClassHierarchy().lookupClass(T);
      this.rhsPath = getAPDictionary().findOrCreate(rhs);
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
        MustAuxiliary must = (MustAuxiliary) tuple.aux;
        AccessPathSet tempMup = assign(must.getMustPaths(), lhs, rhsPath);
        // filter out paths where the first instance on the path cannot be
        // proved to be of the correct type.
        // currently our access paths don't carry type information, so we have
        // to give up on any paths
        // that have length>1, since our only type information is for paths of
        // length 1 which point directly
        // to the instance.
        AccessPathSet newMup = new AccessPathSet(getAPDictionary());
        boolean complete = must.isComplete();
        for (Iterator<AccessPath> it = tempMup.iterator(); it.hasNext();) {
          AccessPath ap = it.next();
          if (ap.getHead().equals(lhs)) {
            if (ap.length() == 1 && klass != null) {
              IClass concreteType = tuple.instance.getConcreteType();
              if (klass.isInterface() && getCallGraph().getClassHierarchy().implementsInterface(concreteType, klass)) {
                newMup.add(ap);
              } else {
                // ! klass.isInterface
                if (getCallGraph().getClassHierarchy().isSubclassOf(concreteType, klass)) {
                  newMup.add(ap);
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
            newMup.add(ap);
          }
        }

        MustAuxiliary newMust = new MustAuxiliary(newMup, complete);
        int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(), newMust);
        return SparseIntSet.singleton(newTupleIndex);
      }
    }

    /**
     * @return Returns the klass.
     */
    protected IClass getKlass() {
      return klass;
    }

    /**
     * @return Returns the lhs.
     */
    protected LocalPathElement getLhs() {
      return lhs;
    }

    /**
     * @return Returns the rhs.
     */
    protected LocalPathElement getRhs() {
      return rhs;
    }

    /**
     * @return Returns the rhsPath.
     */
    protected AccessPath getRhsPath() {
      return rhsPath;
    }

  }

  /**
   * subclasses will override as desired
   * 
   * @param mustSet
   * @param complete
   * @return a new auxiliary structure which holds a set of must-access paths
   *         and complete information
   */
  protected MustAuxiliary makeMustAuxiliary(AccessPathSet mustSet, boolean complete) {
    return new MustAuxiliary(mustSet, complete);
  }

  /**
   * 
   * Flow Function for a "new" statement
   */
  protected class AllocFlowFunction implements IUnaryFlowFunction {

    /**
     * This is the index of the tuple that is created the first time the alloc
     * site is encountered
     */
    final int uniqueAlloc;

    /**
     * This is the index of the tuple that is created the 2nd time the alloc
     * site is encountered
     */
    final int notUniqueAlloc;

    /**
     * this is the local variable which holds the result of this allocation
     */
    final LocalPathElement lhsElement;

    /**
     * initial state for a new instance
     */
    protected final IDFAState initial;

    /**
     * [I,S,MUP]' = [I,S,kill(MUP,x)] U { (instance-key , initial , {x}) }
     * 
     */
    public AllocFlowFunction(InstanceKey ik, IDFAState initial, LocalPointerKey pk) {
      AccessPathSet mustSet = new AccessPathSet(getAPDictionary());
      lhsElement = new LocalPathElement(pk);
      this.initial = initial;
      mustSet.add(getAPDictionary().findOrCreate(lhsElement));
      MustAuxiliary aux = makeMustAuxiliary(mustSet, true);
      uniqueAlloc = getQuadDomain().findOrCreate(ik, initial, true, aux);
      notUniqueAlloc = getQuadDomain().findOrCreate(ik, initial, false, aux);
    }

    /**
     * if MUP contains x, kill this factoid and add a new factoid with x-killed
     * in MUP. When x is not in the sets, it returns the original sets. Thus,
     * when x is not involved, d1 -> { d1, tupleId }.
     */

    public SparseIntSet getTargets(int d1) {
      MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();

      if (d1 != 0) {
        Object domainItem = getQuadDomain().getMappedObject(d1);
        QuadFactoid tuple = (QuadFactoid) domainItem;

        MustAuxiliary must = (MustAuxiliary) tuple.aux;
        AccessPathSet newMup = kill(must.getMustPaths(), lhsElement);

        // if the old must set was complete, it's still complete since we've
        // only killed the x path. In either case, the unique attribute must now
        // be false.
        int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, false,
            makeMustAuxiliary(newMup, must.isComplete()));
        result.add(newTupleIndex);
        result.add(notUniqueAlloc);
      } else {
        result.add(uniqueAlloc);
        result.add(0);
      }
      return result;
    }

    /**
     * @return Returns the lhsElement.
     */
    protected LocalPathElement getLhsElement() {
      return lhsElement;
    }

    /**
     * @return Returns the notUniqueAlloc.
     */
    protected int getNotUniqueAlloc() {
      return notUniqueAlloc;
    }

    /**
     * @return Returns the uniqueAlloc.
     */
    protected int getUniqueAlloc() {
      return uniqueAlloc;
    }
  }

  /**
   * x.foo()
   */
  public class CallFlowFunction implements IReversibleFlowFunction {

    private final CGNode callee;

    private final CallSiteReference site;

    public CallFlowFunction(CGNode callee, CallSiteReference site) {
      this.callee = callee;
      this.site = site;

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
        MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
        QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d1);
        if (!isFeasibleCall(tuple)) {
          // really we'd like to just ignore the callee.
          // for now, just propagate zero into it.
          // TODO: support flow functions that don't even propagate zero
          // factoid,
          // to indicate unreachable code?
          return SparseIntSet.singleton(0);
        }

        MustAuxiliary must = (MustAuxiliary) tuple.aux;
        AccessPathSet mup = must.getMustPaths();

        IMethod calleeMethod = callee.getMethod();
        int numOfParams = calleeMethod.getNumberOfParameters();

        // in case of recursion, clean up the must
        boolean newFactIsComplete = must.isComplete();
        int oldMustSize = mup.size();
        mup = killLocals(mup, getCallee());
        if (mup.size() < oldMustSize) {
          // we killed some must alias to be safe for recursion. we must
          // sacrifice
          // completeness.
          newFactIsComplete = false;
        }
        AccessPathSet prevMup = mup;
        AccessPathSet newMup = mup;
        // note:
        // for InvokeInstr - params[0] is this (for non static methods)
        // for calleeIR, the 0th parameter is this (for non static methods)
        // for IMethod, the 0-th parameter is (again) this (for non static
        // methods)
        for (int i = 0; i < numOfParams; i++) {
          // we rely on the invariant that the ith formal parameter has value
          // number i+1
          int formal = i + 1;
          TypeReference formalType = calleeMethod.getParameterType(i);
          if (formalType.isReferenceType()) {
            TemporaryParameterPointerKey actualPointerKey = TemporaryParameterPointerKey.make(i);
            LocalPointerKey formalPointerKey = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(callee,
                formal);

            PathElement formalPathElement = new LocalPathElement(formalPointerKey);
            newMup = rename(prevMup, formalPathElement, Collections.singleton(new LocalPathElement(actualPointerKey)));
            prevMup = newMup;
          }
        }

        int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(),
            new MustAuxiliary(newMup, newFactIsComplete));
        result.add(newTupleIndex);
        return result;
      }
    }

    /**
     * Is it feasible, in the environment determined by the factoid, to dispatch
     * to the callee?
     * 
     * We do this by checking for must alias on the receiver.
     */
    protected boolean isFeasibleCall(QuadFactoid tuple) {
      if (site.isSpecial() || site.isStatic()) {
        return true;
      } else {
        TemporaryParameterPointerKey recvPointerKey = TemporaryParameterPointerKey.make(0);
        AccessPath recv = getAPDictionary().findOrCreate(new LocalPathElement(recvPointerKey));
        AccessPathSet must = ((MustAuxiliary) tuple.aux).getMustPaths();
        if (must.contains(recv)) {
          IClass klass = tuple.instance.getConcreteType();
          IMethod resolved = getCallGraph().getClassHierarchy().resolveMethod(klass, callee.getMethod().getSelector());
          // Important: check equality of method references here, not the
          // methods themselves.
          // Why? Because the IMethod might actually be synthetic, and the cha
          // lookup will not
          // return the synthetic method.
          if (resolved.getReference().equals(callee.getMethod().getReference())) {
            if (callee.getContext() instanceof ReceiverInstanceContext) {
              ReceiverInstanceContext c = (ReceiverInstanceContext) callee.getContext();
              return c.getReceiver().equals(tuple.instance);
            } else {
              return true;
            }
          } else {
            return false;
          }
        } else {
          return true;
        }
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.wala.dataflow.IFDS.IReversibleFlowFunction#getSources(int)
     */
    // TODO: this does not rename must-not, probably under the assumption that
    // we're
    // throwing away mustnot information between procedure calls. Verify.
    public SparseIntSet getSources(int d2) {

      if (d2 == 0) {
        return SparseIntSet.singleton(0);
      } else {
        MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
        IMethod calleeMethod = callee.getMethod();
        QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d2);
        MustAuxiliary must = (MustAuxiliary) tuple.aux;
        AccessPathSet mup = must.getMustPaths();
        AccessPathSet newMup = mup;
        int numOfParams = calleeMethod.getNumberOfParameters();
        for (int i = 0; i < numOfParams; i++) {
          // we rely on the invariant that the ith formal parameter has value
          // number i+1
          int formal = i + 1;
          LocalPointerKey formalPointerKey = (LocalPointerKey) getPointerAnalysis().getHeapModel().getPointerKeyForLocal(callee,
              formal);
          PathElement formalPathElement = new LocalPathElement(formalPointerKey);
          TemporaryParameterPointerKey actualPointerKey = TemporaryParameterPointerKey.make(i);
          newMup = rename(newMup, new LocalPathElement(actualPointerKey), Collections.singleton(formalPathElement));
        }
        int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(),
            new MustAuxiliary(newMup, must.isComplete()));
        result.add(newTupleIndex);
        return result;
      }
    }

    /**
     * @return Returns the callee.
     */
    protected CGNode getCallee() {
      return callee;
    }
  }

  /**
   * x = Y.f
   * 
   * @author Eran Yahav (yahave)
   */
  public class GetStaticFlowFunction extends LocalAssignFlowFunction {

    public GetStaticFlowFunction(LocalPointerKey x, StaticFieldKey Y_f) {
      super(new LocalPathElement(x), getAPDictionary().findOrCreate(new StaticFieldPathElement(Y_f)));
      assert x != null && Y_f != null : "cannot create flow function with null receiver or static field";
    }
  }

  /**
   * 
   * @author Eran Yahav (yahave)
   */
  public class GetFieldFlowFunction extends LocalAssignFlowFunction {

    public GetFieldFlowFunction(LocalPointerKey x, LocalPointerKey y, IField f) {
      super(new LocalPathElement(x), getAPDictionary().concat(getAPDictionary().findOrCreate(new LocalPathElement(y)),
          new InstanceFieldPathElement(f)));
    }
  }

  /**
   * X.f = y
   * 
   * @author Eran Yahav (yahave)
   * @author Stephen Fink
   */
  public class PutStaticFlowFunction implements IUnaryFlowFunction {

    private final PathElement yPathElement;

    private final AccessPath X_fPath;

    public PutStaticFlowFunction(StaticFieldKey X_f, LocalPointerKey y) {
      assert y != null && X_f != null : "cannot create flow function with null receiver or static field";
      this.X_fPath = getAPDictionary().findOrCreate(new StaticFieldPathElement(X_f));
      this.yPathElement = new LocalPathElement(y);
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
        MustAuxiliary must = (MustAuxiliary) tuple.aux;
        AccessPathSet mup = must.getMustPaths();
        AccessPathSet newMup = updateMust(mup, X_fPath, yPathElement);

        if (must.isComplete() && newMup.size() == 0) {
          return SparseIntSet.singleton(0);
        } else {
          int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(),
              new MustAuxiliary(newMup, must.isComplete()));
          return SparseIntSet.singleton(newTupleIndex);
        }
      }
    }

    /**
     * @return Returns the x_fPath.
     */
    protected AccessPath getX_fPath() {
      return X_fPath;
    }

    /**
     * @return Returns the yPathElement.
     */
    protected PathElement getYPathElement() {
      return yPathElement;
    }
  }

  /**
   * return x;
   */
  protected class ReturnFlowFunction implements IUnaryFlowFunction {

    /**
     * caller callgraph node
     */
    private final CGNode caller;

    /**
     * callee callgrpah node
     */
    private final CGNode callee;

    /**
     * set of return values
     */
    private final Set<PathElement> retValElements;

    protected final SSAInvokeInstruction call;

    /**
     * @param retValElements
     * @param caller
     */
    protected ReturnFlowFunction(SSAInvokeInstruction call, Set<PathElement> retValElements, CGNode caller, CGNode callee) {
      this.caller = caller;
      this.callee = callee;
      this.retValElements = retValElements;
      this.call = call;
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
          Trace.println("ReturnEdge for " + d1);
        }

        MustAuxiliary aux = (MustAuxiliary) tuple.aux;
        AccessPathSet must = aux.getMustPaths();

        // re-establish must for parameters!
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
            AccessPath formalAP = getAPDictionary().findOrCreate(formalPathElement);
            must = assign(must, new LocalPathElement(act), Collections.singleton(formalAP));
          }
        }

        if (!getRetValElements().isEmpty()) {
          // add assignment of return value to invoke-instruction LHS
          PathElement symRetVal = new LocalPathElement(TemporaryParameterPointerKey.makeReturnValue());
          must = rename(must, symRetVal, getRetValElements());
        }

        must = killOutOfScopeLocals(must, getCaller());
        // we purposely give up if there's recursion, to avoid an
        // explosion of propagation
        must = killLocals(must, getCallee());

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
              new MustAuxiliary(must, aux.isComplete()));
          return SparseIntSet.singleton(newTupleIndex);
        }
      }
    }

    protected boolean canForceToUnique(QuadFactoid tuple) {
      if (tuple.instance instanceof AllocationSiteInNode) {
        AllocationSiteInNode ak = (AllocationSiteInNode) tuple.instance;
        if (UniqueReturnFlowFunction.mustBeUniqueInNode(ak, caller, getHeapGraph(), getLiveObjectAnalysis())) {
          return true;
        }
      }
      return false;
    }

    /**
     * @return Returns the caller.
     */
    protected CGNode getCaller() {
      return caller;
    }

    /**
     * @return Returns the retValElements.
     */
    protected Set<PathElement> getRetValElements() {
      return retValElements;
    }

    /**
     * @return Returns the callee.
     */
    protected CGNode getCallee() {
      return callee;
    }
  }

  /**
   * x[i] = y
   */
  protected class AStoreFlowFunction implements IUnaryFlowFunction {

    // todo: clean up these fields
    private final PathElement xPathElement;

    private final PathElement yPathElement;

    private final AccessPath xContentsPath;

    private final CGNode node;

    private final LocalPointerKey x;

    public AStoreFlowFunction(LocalPointerKey x, LocalPointerKey y) {
      this.x = x;
      this.node = x.getNode();
      this.xPathElement = new LocalPathElement(x);
      this.yPathElement = new LocalPathElement(y);
      this.xContentsPath = getAPDictionary().concat(xPathElement, ArrayContentsPathElement.instance());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.ibm.wala.dataflow.IFDS.IUnaryFlowFunction#getTargets(int)
     */
    public SparseIntSet getTargets(int d1) {

      if (d1 == 0) {
        return SparseIntSet.singleton(d1);
      }
      MutableSparseIntSet result = MutableSparseIntSet.makeEmpty();
      QuadFactoid tuple = (QuadFactoid) getQuadDomain().getMappedObject(d1);
      MustAuxiliary must = (MustAuxiliary) tuple.aux;

      if (!must.isComplete()) {
        // no point adding an array path to a not-complete factoid, is there?
        // just ignore it.
        return SparseIntSet.singleton(d1);
      } else {
        AccessPathSet mup = must.getMustPaths();
        AccessPathSet newMup = updateMust(mup, xContentsPath, yPathElement);

        boolean complete = containsAllMayAliasPaths(node, newMup, x, getAPDictionary().findOrCreate(xContentsPath.getSuffix(1)));

        int size = newMup.size();
        newMup = apsTransformer.kLimit(newMup, accessPathKLimit);
        complete = (newMup.size() == size) ? complete : false;

        if (!complete) {
          AccessPathSetTransformers.removeArrayPaths(newMup);
        }

        int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(),
            new MustAuxiliary(newMup, complete));
        result.add(newTupleIndex);

        return result;
      }
    }

    /**
     * @return Returns the xContentsPath.
     */
    protected AccessPath getXContentsPath() {
      return xContentsPath;
    }

    /**
     * @return Returns the xPathElement.
     */
    protected PathElement getXPathElement() {
      return xPathElement;
    }

    /**
     * @return Returns the yPathElement.
     */
    protected PathElement getYPathElement() {
      return yPathElement;
    }

    /**
     * @return Returns the node.
     */
    protected CGNode getNode() {
      return node;
    }

    /**
     * @return Returns the x.
     */
    protected LocalPointerKey getX() {
      return x;
    }
  }

  /**
   * x.f = y
   * 
   * @author Eran Yahav (yahave)
   * @author Stephen Fink
   */
  protected class PutFieldFlowFunction implements IUnaryFlowFunction {

    // todo: clean up these fields.
    private final LocalPointerKey x;

    private final PathElement xPathElement;

    private final PathElement yPathElement;

    private final AccessPath x_fPath;

    private final CGNode node;

    public PutFieldFlowFunction(LocalPointerKey x, LocalPointerKey y, IField f) {

      assert f != null;

      this.x = x;
      this.node = x.getNode();
      this.xPathElement = new LocalPathElement(x);
      this.yPathElement = new LocalPathElement(y);
      this.x_fPath = getAPDictionary().concat(xPathElement, new InstanceFieldPathElement(f));
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
      MustAuxiliary must = (MustAuxiliary) tuple.aux;
      AccessPathSet mup = must.getMustPaths();
      AccessPathSet newMup = updateMust(mup, x_fPath, yPathElement);

      boolean complete = false;
      if (must.isComplete()) {
        complete = containsAllMayAliasPaths(node, newMup, x, getAPDictionary().findOrCreate(x_fPath.getSuffix(1)));
      }

      int size = newMup.size();
      newMup = apsTransformer.kLimit(newMup, accessPathKLimit);
      complete = (newMup.size() == size) ? complete : false;

      int newTupleIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(),
          new MustAuxiliary(newMup, complete));
      result.add(newTupleIndex);

      return result;
    }

    /**
     * @return Returns the x_fPath.
     */
    protected AccessPath getX_fPath() {
      return x_fPath;
    }

    /**
     * @return Returns the xPathElement.
     */
    protected PathElement getXPathElement() {
      return xPathElement;
    }

    /**
     * @return Returns the yPathElement.
     */
    protected PathElement getYPathElement() {
      return yPathElement;
    }

    /**
     * @return Returns the node.
     */
    protected CGNode getNode() {
      return node;
    }

    /**
     * @return Returns the x.
     */
    protected LocalPointerKey getX() {
      return x;
    }
  }

  protected IUnaryFlowFunction makeReturnFlowFunction(Set<PathElement> retValElements, CGNode caller, CGNode callee,
      SSAInvokeInstruction call) {
    return new ReturnFlowFunction(call, retValElements, caller, callee);
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
    if (!getDFA().observesProgramExit()) {
      return UniversalKillFlowFunction.kill();
    }
    // SSAInvokeInstruction srcInvokeInstr =
    // SafeDomoUtils.getLastCallInstruction(getUncollapsed(), srcBlock);
    SSAInvokeInstruction srcInvokeInstr = null;
    CGNode caller = (CGNode) getSupergraph().getProcOf(src);

    return new BaseProgramExitFlowFunction(getDomain(), getDFA(), src, srcInvokeInstr, caller, getTraceReporter());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.ap.AccessPathFunctionProvider#makePiFlowFunction
   * (com.ibm.wala.ssa.ISSABasicBlock, com.ibm.wala.ssa.SSAPiInstruction)
   */
  protected IUnaryFlowFunction makePiFlowFunction(CGNode node, ISSABasicBlock bb, SSAPiInstruction pi) {
    Assertions.UNREACHABLE();
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.ap.AccessPathFunctionProvider#makeAllocFlowFunction
   * (com.ibm.wala.ipa.callgraph.propagation.InstanceKey,
   * com.ibm.safe.emf.typestate.IState,
   * com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey)
   */
  protected IUnaryFlowFunction makeAllocFlowFunction(InstanceKey ik, IDFAState initial, LocalPointerKey pk) {
    return new AllocFlowFunction(ik, getDFA().initial(), pk);
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
    return new PutStaticFlowFunction(X_f, y);
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
    return new AStoreFlowFunction(x, y);
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
    return new PutFieldFlowFunction(x, y, f);
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
    return new GetStaticFlowFunction(x, Y_f);
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
    return new GetFieldFlowFunction(x, y, f);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.ap.AccessPathFunctionProvider#makeALoadFlowFunction
   * (com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey,
   * com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey)
   */
  protected IUnaryFlowFunction makeALoadFlowFunction(LocalPointerKey x, LocalPointerKey y) {
    AccessPath rhs = getAPDictionary().concat(new LocalPathElement(y), ArrayContentsPathElement.instance());
    return makeLocalAssignFlowFunction(new LocalPathElement(x), rhs);
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.ibm.safe.typestate.ap.AccessPathFunctionProvider#
   * makeLocalAssignFlowFunction
   * (com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey,
   * com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey)
   */
  protected IUnaryFlowFunction makeLocalAssignFlowFunction(AbstractPointerKey x, AbstractPointerKey y) {
    return makeLocalAssignFlowFunction(new LocalPathElement(x), getAPDictionary().findOrCreate(new LocalPathElement(y)));
  }

  /**
   * subclasses should override as desired
   */
  protected IUnaryFlowFunction makeLocalAssignFlowFunction(LocalPathElement lhs, AccessPath rhs) {
    return new LocalAssignFlowFunction(lhs, rhs);
  }

  protected IUnaryFlowFunction makeLocalRenameFlowFunction(AbstractPointerKey x, AbstractPointerKey y) {
    return new LocalRenameFlowFunction(new LocalPathElement(x), new LocalPathElement(y));
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.ibm.safe.typestate.ap.AccessPathFunctionProvider#
   * makeNonEventCallFlowFunction(com.ibm.wala.ssa.SSAInvokeInstruction,
   * com.ibm.wala.ipa.callgraph.CGNode, com.ibm.wala.ipa.callgraph.CGNode)
   */
  protected IUnaryFlowFunction makeCallFlowFunction(CGNode caller, CGNode callee, SSAInvokeInstruction call) {
    return new CallFlowFunction(callee, call.getCallSite());
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
    return new CheckCastFlowFunction(new LocalPathElement(x), new LocalPathElement(y), T);
  }

  /**
   * @return Returns the accessPathKLimit.
   */
  protected int getAccessPathKLimit() {
    return accessPathKLimit;
  }

  /**
   * for each x.p.path \in s, Does s contain z.p.path for every z s.t.
   * may-alias(z,x)?
   * 
   * We assume that s is an access-path-set evaluated in call graph node "node",
   * so we don't worry about may paths that are rooted in locals that cannot be
   * live when node executes
   */
  protected boolean containsAllMayAliasPaths(CGNode node, AccessPathSet s, PointerKey x, AccessPath p) {
    AccessPath x_p = getAPDictionary().concat(makePathElement(x), p);
    int x_p_length = x_p.length();
    for (Iterator<AccessPath> it = s.pathsWithPrefix(x_p).iterator(); it.hasNext();) {
      AccessPath x_p_path = it.next();
      AccessPath path = x_p_path.length() == x_p_length ? null : getAPDictionary().findOrCreate(x_p_path.getSuffix(x_p_length));
      for (Iterator it2 = iterateMayAliases(x); it2.hasNext();) {
        PointerKey y = (PointerKey) it2.next();
        if (y.equals(x)) {
          continue;
        }
        if (y instanceof AbstractLocalPointerKey) {
          CGNode yNode = ((AbstractLocalPointerKey) y).getNode();
          if (!getReach().getReachableSet(yNode).contains(node)) {
            // we don't worry about y because it cannot be live
            // at this program point
            continue;
          }
          // y is a local pointer which may-alias x.
          // simply need to check that s contains y.p.path
          LocalPathElement yElement = new LocalPathElement((AbstractLocalPointerKey) y);
          AccessPath y_p_path = getAPDictionary().concat(yElement, p);
          if (path != null) {
            y_p_path = getAPDictionary().concat(y_p_path, path);
          }
          if (!s.contains(y_p_path)) {
            return false;
          }
        } else if (y instanceof StaticFieldKey) {
          // y is a static field which may-alias x.
          // simply need to check that s contains y.p.path
          StaticFieldPathElement yElement = new StaticFieldPathElement((StaticFieldKey) y);
          AccessPath y_p_path = getAPDictionary().concat(yElement, p);
          if (path != null) {
            y_p_path = getAPDictionary().concat(y_p_path, path);
          }
          if (!s.contains(y_p_path)) {
            return false;
          }
        } else if (y instanceof InstanceFieldKey) {
          // y is an instance field ik.g which may-alias x.
          InstanceKey ik = ((InstanceFieldKey) y).getInstanceKey();
          IField g = ((InstanceFieldKey) y).getField();
          // find some local pointer h which may point to ik,
          Iterator pred = getPointerAnalysis().getHeapGraph().getPredNodes(ik);
          PointerKey h = null;
          for (; pred.hasNext();) {
            PointerKey pk = (PointerKey) pred.next();
            if (pk instanceof AbstractLocalPointerKey) {
              h = (AbstractLocalPointerKey) pk;
              CGNode hNode = ((AbstractLocalPointerKey) h).getNode();
              if (getReach().getReachableSet(hNode).contains(node)) {
                // found a root that's simultaneously live. good.
                break;
              }
            } else if (pk instanceof StaticFieldKey) {
              h = pk;
              break;
            }
          }

          assert h != null;

          InstanceFieldPathElement g_element = new InstanceFieldPathElement(g);
          AccessPath g_p_path = getAPDictionary().concat(g_element, p);
          if (path != null) {
            g_p_path = getAPDictionary().concat(g_p_path, path);
          }
          PathElement hElement = makePathElement(h);
          AccessPath h_g_p_path = getAPDictionary().concat(hElement, g_p_path);
          if (h instanceof AbstractLocalPointerKey) {
            CGNode hNode = ((AbstractLocalPointerKey) h).getNode();
            if (getReach().getReachableSet(hNode).contains(node)) {
              // h is relevant. check that s contains h.g.p.path
              if (!s.contains(h_g_p_path)) {
                return false;
              }
              // verify that foreach z that may-alias h, s contains z.g.p.path
              if (!containsAllMayAliasPaths(node, s, h, g_p_path)) {
                return false;
              }
            } else {
              // could not find a root that's relevant.
              // it's complicated. be conservative and give up, for now.
              // TODO: fix this
              return false;
            }
          } else if (h instanceof StaticFieldKey) {
            // h is relevant. check that s contains h.g.p.path
            if (!s.contains(h_g_p_path)) {
              return false;
            }
          } else {
            Assertions.UNREACHABLE("unexpected " + h.getClass());
          }
        } else if (y instanceof ArrayContentsKey) {
          // y is an instance field ik[] which may-alias x.
          InstanceKey ik = ((ArrayContentsKey) y).getInstanceKey();
          // find some local pointer h which may point to ik,
          Iterator pred = getPointerAnalysis().getHeapGraph().getPredNodes(ik);
          PointerKey h = null;
          for (; pred.hasNext();) {
            PointerKey pk = (PointerKey) pred.next();
            if (pk instanceof AbstractLocalPointerKey) {
              h = (AbstractLocalPointerKey) pk;
              CGNode hNode = ((AbstractLocalPointerKey) h).getNode();
              if (getReach().getReachableSet(hNode).contains(node)) {
                // found a root that's simultaneously live. good.
                break;
              }
            } else if (pk instanceof StaticFieldKey) {
              h = pk;
              break;
            }
          }

          assert h != null;

          AccessPath g_p_path = getAPDictionary().concat(ArrayContentsPathElement.instance(), p);
          if (path != null) {
            g_p_path = getAPDictionary().concat(g_p_path, path);
          }
          PathElement hElement = makePathElement(h);
          AccessPath h_g_p_path = getAPDictionary().concat(hElement, g_p_path);
          if (h instanceof AbstractLocalPointerKey) {
            CGNode hNode = ((AbstractLocalPointerKey) h).getNode();
            if (getReach().getReachableSet(hNode).contains(node)) {
              // h is relevant. check that s contains h.g.p.path
              if (!s.contains(h_g_p_path)) {
                return false;
              }
              // verify that foreach z that may-alias h, s contains z.g.p.path
              if (!containsAllMayAliasPaths(node, s, h, g_p_path)) {
                return false;
              }
            } else {
              // could not find a root that's relevant.
              // it's complicated. be conservative and give up, for now.
              // TODO: fix this
              return false;
            }
          } else if (h instanceof StaticFieldKey) {
            // h is relevant. check that s contains h.g.p.path
            if (!s.contains(h_g_p_path)) {
              return false;
            }
          } else {
            Assertions.UNREACHABLE("unexpected " + h.getClass());
          }
        } else {
          Assertions.UNREACHABLE("TODO: need to implement case for " + y.getClass());
        }
      }
    }
    return true;
  }

  private PathElement makePathElement(PointerKey h) {
    if (h instanceof AbstractLocalPointerKey) {
      return new LocalPathElement((AbstractLocalPointerKey) h);
    } else if (h instanceof StaticFieldKey) {
      return new StaticFieldPathElement((StaticFieldKey) h);
    } else if (h instanceof InstanceFieldKey) {
      return new InstanceFieldPathElement(((InstanceFieldKey) h).getField());
    } else if (h instanceof ArrayContentsKey) {
      return ArrayContentsPathElement.instance();
    } else {
      Assertions.UNREACHABLE(h.getClass().toString());
      return null;
    }
  }

  /**
   * the result May contain x
   * 
   * @return Collection<PointerKey>
   */
  private Iterator iterateMayAliases(PointerKey x) {
    final HeapGraph hg = getPointerAnalysis().getHeapGraph();
    final Iterator<InstanceKey> outer = getPointerAnalysis().getPointsToSet(x).iterator();
    return new Iterator() {
      Iterator inner = outer.hasNext() ? hg.getPredNodes(outer.next()) : null;

      public void remove() {
        Assertions.UNREACHABLE();
      }

      public boolean hasNext() {
        return inner != null && inner.hasNext();
      }

      public Object next() {
        Object result = inner.next();
        if (!inner.hasNext()) {
          inner = outer.hasNext() ? hg.getPredNodes(outer.next()) : null;
        }
        return result;
      }
    };
  }

  protected IUnaryFlowFunction makeDeadAccessPathKiller(CGNode node, ISSABasicBlock b) {
    return new MustAPDeadKiller(node, b);
  }

  protected class MustAPDeadKiller implements IUnaryFlowFunction {

    private final CGNode node;

    private final ISSABasicBlock b;

    MustAPDeadKiller(CGNode node, ISSABasicBlock b) {
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
      MustAuxiliary aux = (MustAuxiliary) tuple.aux;
      AccessPathSet must = aux.getMustPaths();
      int oldSize = must.size();
      must = removeDeadPaths(must, node, b, node.getIR(), node.getDU());
      if (must.size() < oldSize) {
        if (must.size() == 0 && aux.isComplete()) {
          return SparseIntSet.singleton(0);
        } else {
          int newIndex = getQuadDomain().findOrCreate(tuple.instance, tuple.state, tuple.isUnique(),
              new MustAuxiliary(must, aux.isComplete()));
          return SparseIntSet.singleton(newIndex);
        }
      } else {
        return SparseIntSet.singleton(d1);
      }
    }
  }
}