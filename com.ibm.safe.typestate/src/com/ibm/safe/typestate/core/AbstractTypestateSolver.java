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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.safe.Factoid;
import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.dfa.DFASpec;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.dfa.events.IObjectDeathEventImpl;
import com.ibm.safe.dfa.events.IProgramExitEventImpl;
import com.ibm.safe.internal.exceptions.MaxFindingsException;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.internal.exceptions.SolverTimeoutException;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.reporting.message.AggregateSolverResult;
import com.ibm.safe.reporting.message.ISolverResult;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.rules.TypestateRule;
import com.ibm.safe.solvers.ICFGTabulationProblem;
import com.ibm.safe.typestate.merge.AbstractUnification;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.metrics.CallGraphMetricsByLoader;
import com.ibm.safe.typestate.metrics.TypeStateMetrics;
import com.ibm.safe.typestate.mine.TraceReporter;
import com.ibm.safe.typestate.mine.TracingProperty;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.typestate.rules.InstanceBatchIterator;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.SyntheticMethod;
import com.ibm.wala.dataflow.IFDS.TabulationResult;
import com.ibm.wala.dataflow.IFDS.TabulationSolver;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSite;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.CollectionFilter;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Iterator2Collection;
import com.ibm.wala.util.collections.MapUtil;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.graph.NumberedGraph;
import com.ibm.wala.util.graph.traverse.DFSPathFinder;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.intset.OrdinalSetMapping;

/**
 * Abstract base class for typestate solvers
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public abstract class AbstractTypestateSolver extends AbstractWholeProgramSolver {

  /**
   * analysis domain
   */
  private TypeStateDomain domain;

  /**
   * Property automaton
   */
  private final ITypeStateDFA dfa;

  /**
   * An object that holds information on statements and instances that are known
   * to be error-free
   */
  private final BenignOracle benignOracle;

  /**
   * If non-null, an object to track statistics with
   */
  private final TypeStateMetrics metrics;

  /**
   * If non-null, an object to record tracing information
   */
  private final TraceReporter traceReporter;

  private final Logger logger = Logger.getGlobal();

  /**
   * Instantiate a new base-safe-solver.
   * 
   * @param pointerAnalysis
   *          - results of pointer-analysis
   * @param dfa
   *          - automaton of typestate property to be verified
   * @param warnings
   *          - collector of produced warnings
   */
  public AbstractTypestateSolver(CallGraph cg, PointerAnalysis pointerAnalysis, ITypeStateDFA dfa, TypeStateOptions options,
      ILiveObjectAnalysis live, BenignOracle ora, TypeStateMetrics metrics, IReporter reporter, TraceReporter traceReporter,
      IMergeFunctionFactory mergeFactory) {
    super(cg, pointerAnalysis, options, live, reporter, mergeFactory);
    this.dfa = dfa;
    this.benignOracle = ora;
    this.metrics = metrics;
    this.traceReporter = traceReporter;
  }

  /**
   * construct an abstract typestate solver with a null trace reporter TODO: the
   * mining code (trace reporter) will be eventually refactored out of here
   * 
   * @param cg
   * @param pointerAnalysis
   * @param property
   * @param options
   * @param live
   * @param ora
   * @param warnings
   * @param metrics
   * @param reporter
   * @param mergeFactory
   */
  public AbstractTypestateSolver(CallGraph cg, PointerAnalysis pointerAnalysis, TypeStateProperty property,
      TypeStateOptions options, ILiveObjectAnalysis live, BenignOracle ora, TypeStateMetrics metrics, IReporter reporter,
      IMergeFunctionFactory mergeFactory) {
    this(cg, pointerAnalysis, property, options, live, ora, metrics, reporter, null, mergeFactory);
  }

  /**
   * @return Returns the domain.
   */
  public TypeStateDomain getDomain() {
    return domain;
  }

  /**
   * @return Returns the benign oracle.
   */
  public BenignOracle getBenignOracle() {
    return benignOracle;
  }

  /**
   * compute the set of nodes in the call graph which the IFDS solver cannot
   * ignore.
   * 
   * Note that this set does not have to include any nodes in which no
   * interesting instance is dead. The actual supergraph will be built as a
   * minimal spanning tree in order to reach all nodes that matter.
   * 
   * subclasses should override this as desired
   * 
   * @param instances
   *          Set <InstanceKey>, interesting instances
   * @return set of CGNodes which lie on some path from the root method to an
   *         event which changes state relevant to the IFDS solver.
   * @throws WalaException
   * @throws PropertiesException
   */
  protected Collection<CGNode> computeNodesThatMatter(OrdinalSet<InstanceKey> instances) throws WalaException, PropertiesException {
    // just return all nodes
    return Iterator2Collection.toList(getCallGraph().iterator());
  }

  /**
   * create an object to control the separation policy.
   * 
   * Note: this is currently broken right now! Separation is baked in as the
   * only policy!! How did this happen?
   * 
   * @param allInstances
   *          set of all instanceKeys which must be solved for
   * @return an object which batches these instanceKeys
   */
  protected abstract InstanceBatchIterator makeBatchIterator(Collection<InstanceKey> allInstances);

  /**
   * @param instances
   * @return an ordinal set of these instances, indexed by the pointer analysis
   *         instance key mapping
   */
  protected OrdinalSet<InstanceKey> toOrdinalInstanceSet(Collection<InstanceKey> instances) {
    MutableSparseIntSet s = MutableSparseIntSet.makeEmpty();
    OrdinalSetMapping<InstanceKey> map = pointerAnalysis.getInstanceKeyMapping();
    for (Iterator<InstanceKey> it = instances.iterator(); it.hasNext();) {
      InstanceKey i = it.next();
      s.add(map.getMappedIndex(i));
    }
    return new OrdinalSet<InstanceKey>(s, map);
  }

  /**
   * initialize the typestate property - find the IClass of the tracked type,
   * and update the property accordingly. Not the most beautiful way to do this.
   * 
   * @return true if there is a property type
   * @throws PropertiesException
   */
  protected boolean initializeProperty() throws PropertiesException {

    if (getOptions().shouldMineDFA()) {
      return !getDFA().getTypes().isEmpty();
    } else {
      boolean hasType = false;

      assert getDFA() instanceof TypeStateProperty;

      TypeStateProperty property = (TypeStateProperty) getDFA();
      TypestateRule rule = (TypestateRule) property.getRule();
      for (Iterator<String> it = rule.getTypes().iterator(); it.hasNext();) {
        String typeDef = it.next();
        IClass pType = TypeStatePropertyContext.getPropertyTrackedType(getCallGraph().getClassHierarchy(), typeDef);
        if (pType != null) {
          property.addType(pType);
          hasType = true;
        }
      }
      return hasType;
    }
  }

  /**
   * @param instances
   *          - set of tracked instances
   */
  protected abstract void initializeDomain(Collection<InstanceKey> instances);

  /**
   * @return the name of this type state property
   */
  public String getPropertyName() {
    return getDFA().getName();
  }

  /**
   * @param domain
   *          The domain to set.
   */
  public void setDomain(TypeStateDomain domain) {
    this.domain = domain;
  }

  /**
   * @return a Filter that only accepts accepting states in the DFA
   */
  protected Predicate<IDFAState> makeAcceptFilter() {
    assert getDFA() instanceof TypeStateProperty;
    TypeStateProperty property = (TypeStateProperty) getDFA();
    Set<IDFAState> accept = property.getAcceptingStates();
    final Predicate<IDFAState> acceptFilter = new CollectionFilter<IDFAState>(accept);
    return acceptFilter;
  }

  /**
   * TODO: this is inefficient. optimize with caching if needed
   * 
   * @param e
   * @return true iff all non-self transitions in the DFA on event e go to an
   *         accepting state
   * @throws PropertiesException
   */
  boolean eventTransitionsOnlyToAccept(IEvent e) throws PropertiesException {
    if (getOptions().shouldMineDFA()) {
      return false;
    } else {

      assert getDFA() instanceof TypeStateProperty;

      TypeStateProperty property = (TypeStateProperty) getDFA();
      return property.eventTransitionsOnlyToAccept(e);
    }
  }

  /**
   * TODO: this is inefficient. optimize with caching if needed
   * 
   * @param e
   * @return true iff some non-self transition in the DFA on event e goes to an
   *         accepting state
   */
  protected boolean eventTransitionsToAccept(IEvent e) {

    assert getDFA() instanceof TypeStateProperty;

    TypeStateProperty property = (TypeStateProperty) getDFA();
    return property.eventTransitionsToAccept(e);
  }

  /**
   * @param callers
   *          Collection<CGNode>
   * @return true iff some n /in callers is from the application loader
   */
  boolean containsApplicationNode(Collection<CGNode> callers) {
    for (Iterator<CGNode> it = callers.iterator(); it.hasNext();) {
      CGNode n = it.next();
      if (n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param instances
   * @return a filter which accepts a CGNode if the live analysis says some
   *         instance \in instances may be live in that node
   */
  protected static Predicate<CGNode> makeLiveNodeFilter(final OrdinalSet<InstanceKey> instances, final ILiveObjectAnalysis live) {
    Predicate<CGNode> liveFilter = new Predicate<CGNode>() {
      public boolean test(CGNode n) {
        for (Iterator<InstanceKey> it = instances.iterator(); it.hasNext();) {
          InstanceKey ik = it.next();
          if (ik instanceof AllocationSite) {
            AllocationSite ak = (AllocationSite) ik;
            try {
              if (live.mayBeLive(ak, n, -1)) {
                return true;
              }
            } catch (WalaException e) {
              e.printStackTrace();
              return true;
            }
          } else {
            // TODO: need to implement live analysis for this.
            return true;
          }
        }
        return false;
      }
    };
    return liveFilter;
  }

  /**
   * @param instances
   *          Set <InstanceKey>, interesting instances
   * @return set of nodes which are recognized as event nodes by the typestate
   *         property
   */
  protected Collection<CGNode> scanForEventNodes(OrdinalSet<InstanceKey> instances) {
    HashSet<CGNode> result = new HashSet<CGNode>();
    for (Iterator<CGNode> it = getCallGraph().iterator(); it.hasNext();) {
      CGNode n = it.next();
      // 1. check that this method is invoked on an interesting instance
      if (n.getMethod().isStatic()) {
        continue;
      }
      if (receiversIncludeRelevantInstance(instances, n)) {
        // 2. check that the method is interesting
        if (getDFA().receives(n.getMethod())) {
          result.add(n);
        }
      }
    }
    return result;
  }

  /**
   * @param instances
   * @param n
   * @return true iff the pointer analysis indicates that some i \in instances
   *         might be the receiver object for node n
   */
  boolean receiversIncludeRelevantInstance(OrdinalSet<InstanceKey> instances, CGNode n) {
    // value number 1 is the receiver
    PointerKey receiver = getPointerAnalysis().getHeapModel().getPointerKeyForLocal(n, 1);
    OrdinalSet<InstanceKey> recvrs = getPointerAnalysis().getPointsToSet(receiver);
    // TODO: more efficient set union could be done
    boolean foundOne = false;
    for (Iterator<InstanceKey> it2 = instances.iterator(); it2.hasNext();) {
      if (recvrs.contains(it2.next())) {
        foundOne = true;
        break;
      }
    }
    return foundOne;
  }

  /**
   * compute the set of instance keys which should be solved for
   * 
   * subclasses can override this as desired, in order to implement
   * instance-based slicing
   * 
   * @return Set of InstanceKeys matching the property type
   */
  protected Collection<InstanceKey> computeTrackedInstancesByType() {
    Collection<InstanceKey> result = new HashSet<InstanceKey>();
    for (Iterator<InstanceKey> it = getPointerAnalysis().getInstanceKeys().iterator(); it.hasNext();) {
      InstanceKey key = it.next();
      IClass instanceType = key.getConcreteType();
      if (TypeStatePropertyContext.isTrackedType(getCallGraph().getClassHierarchy(), getDFA().getTypes(), instanceType)) {
        if (IGNORE_REFLECTIVE_SPAWN) {
          if (key instanceof AllocationSite) {
            AllocationSite ak = (AllocationSite) key;
            if (ak.getMethod().isSynthetic()) {
              SyntheticMethod sm = (SyntheticMethod) ak.getMethod();
              if (!sm.isFactoryMethod()) {
                result.add(key);
              }
            } else {
              result.add(key);
            }
          } else {
            result.add(key);
          }
        } else {
          result.add(key);
        }
      }
    }
    return result;
  }

  /**
   * compute the set of instance keys which should be solved for
   * 
   * subclasses can override this as desired, in order to implement
   * instance-based slicing
   * 
   * @return Set of InstanceKeys matching the property type
   * @throws PropertiesException
   */
  protected Collection<InstanceKey> computeTrackedInstances() throws PropertiesException {
    Collection<InstanceKey> result = computeTrackedInstancesByType();

    if (getOptions().shouldMineDFA()) {
      Map<InstanceKey, Set<IEvent>> m = mapInstances2Events();
      result.retainAll(m.keySet());
    } else if (getOptions().shouldSliceDFA()) {

      assert (getDFA() instanceof TypeStateProperty);

      TypeStateProperty property = (TypeStateProperty) getDFA();
      System.err.println("do " + property.getRule().getName());
      System.err.println("before dfa slice: " + result.size());
      Collection<InstanceKey> receivers = computeRelevantReceiversByDFA();
      result.retainAll(receivers);
      System.err.println("after dfa slice : " + result.size());
    }

    logger.fine(() -> "Number of relevant instances: " + result.size());

    return result;
  }

  /**
   * @return Collection <InstanceKey>s.t. the pointer analysis indicates that
   *         there may be a path to an accepting state
   * @throws PropertiesException
   */
  protected Collection<InstanceKey> computeRelevantReceiversByDFA() throws PropertiesException {

    assert getDFA() instanceof TypeStateProperty;

    TypeStateProperty property = (TypeStateProperty) getDFA();
    Map<InstanceKey, Set<IEvent>> m = mapInstances2Events();
    System.out.println(m.size());
    final DFASpec idfa = ((TypestateRule) property.getRule()).getTypeStateAutomaton();
    final NumberedGraph<Object> dfa = idfa.asGraph();
    Object start = idfa.initialState();
    final Predicate acceptFilter = makeAcceptFilter();

    /**
     * For each instance, check that the instance-specific DFA includes a path
     * to an accepting state
     */
    HashSet<InstanceKey> result = new HashSet<InstanceKey>();
    for (Iterator<Map.Entry<InstanceKey, Set<IEvent>>> it = m.entrySet().iterator(); it.hasNext();) {
      Map.Entry<InstanceKey, Set<IEvent>> e = it.next();
      InstanceKey ik = e.getKey();
      final Set<IEvent> events = e.getValue();

      DFSPathFinder<Object> search = new DFSPathFinder<Object>(dfa, start, acceptFilter) {
        private static final long serialVersionUID = -964543746573101872L;

        /**
         * get the out edges of a given node, tracing only edges labelled with
         * appropriate events
         * 
         * @param n
         *          the node of which to get the out edges
         * @return the out edges
         * 
         */
        protected Iterator<Object> getConnected(Object n) {
          IDFAState state = (IDFAState) n;
          HashSet<Object> reached = new HashSet<Object>(dfa.getSuccNodeCount(n));
          for (Iterator<IEvent> it = events.iterator(); it.hasNext();) {
            IEvent e = it.next();
            IDFAState next = (IDFAState) getDFA().successor(state, e);
            if (dfa.containsNode(next)) {
              reached.add(next);
            }
          }
          return reached.iterator();
        }
      };
      if (search.find() != null) {
        // there is a path in the DFA.
        if (!NO_LIBRARY_ERRORS || appMightRaiseError(events, ik)) {
          result.add(ik);
        }
      }
    }
    return result;
  }

  /**
   * TODO: inefficient. can speed up with caching event->node mapping
   * 
   * @param events
   *          a set of events
   * @param instance
   *          an instance
   * @return true if there is some caller of an event e \in events such that the
   *         event may transition to an accepting state when invoked on the
   *         instance, and the caller is from the application class loader
   */
  protected boolean appMightRaiseError(Collection<IEvent> events, InstanceKey instance) {
    for (Iterator<CGNode> it = getCallGraph().iterator(); it.hasNext();) {
      CGNode n = it.next();
      if (n.getMethod().isStatic()) {
        continue;
      }
      if (getDFA().receives(n.getMethod())) {
        IEvent e = getDFA().matchDispatchEvent(n.getMethod().getSignature());
        if (events.contains(e) && eventTransitionsToAccept(e)) {
          Collection<CGNode> callers = computeRelevantCallers(Collections.singleton(n), toOrdinalInstanceSet(Collections
              .singleton(instance)));
          if (callers != null) {
            if (containsApplicationNode(callers)) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  /**
   * set up a mapping of instance keys to events that the instance key flows to
   * 
   * If NO_LIBRARY_ERRORS, ignore events that can only cause errors from library
   * code.
   * 
   * @return Map<InstanceKey,Set<IEvent>>
   * @throws PropertiesException
   */
  protected Map<InstanceKey, Set<IEvent>> mapInstances2Events() throws PropertiesException {
    HashMap<InstanceKey, Set<IEvent>> result = new HashMap<InstanceKey, Set<IEvent>>();
    // scan nodes and collect instances that flow to each event
    for (Iterator<CGNode> it = getCallGraph().iterator(); it.hasNext();) {
      CGNode n = it.next();
      if (n.getMethod().isStatic()) {
        continue;
      }
      if (getDFA().receives(n.getMethod())) {
        IEvent e = getDFA().matchDispatchEvent(n.getMethod().getSignature());
        // value number 1 is the receiver
        PointerKey receiver = getPointerAnalysis().getHeapModel().getPointerKeyForLocal(n, 1);
        OrdinalSet<InstanceKey> recvrs = getPointerAnalysis().getPointsToSet(receiver);
        for (Iterator<InstanceKey> it2 = recvrs.iterator(); it2.hasNext();) {
          InstanceKey instance = it2.next();
          if (NO_LIBRARY_ERRORS) {
            Collection<CGNode> callers = computeRelevantCallers(Collections.singleton(n), toOrdinalInstanceSet(Collections
                .singleton(instance)));
            if (callers != null && !callers.isEmpty()) {
              if (eventTransitionsOnlyToAccept(e)) {
                if (containsApplicationNode(callers)) {
                  MapUtil.findOrCreateSet(result, instance).add(e);
                }
              } else {
                MapUtil.findOrCreateSet(result, instance).add(e);
              }
            }
          } else {
            MapUtil.findOrCreateSet(result, instance).add(e);
          }
        }
      }
    }

    // when program-exit event is used by property --- add program-exit
    // event
    // to all instances
    if (getDFA().observesProgramExit()) {
      for (Iterator<Set<IEvent>> it = result.values().iterator(); it.hasNext();) {
        Set<IEvent> curr = it.next();
        curr.add(IProgramExitEventImpl.singleton());
      }
    }
    // when program-exit event is used by property --- add program-exit
    // event
    // to all instances
    if (getDFA().observesObjectDeath()) {
      for (Iterator<Set<IEvent>> it = result.values().iterator(); it.hasNext();) {
        Set<IEvent> curr = it.next();
        curr.add(IObjectDeathEventImpl.singleton());
      }
    }
    return result;
  }

  /**
   * The returned result does not include benign statements, nor statements for
   * which we already have a finding.
   * 
   * @param targets
   * @param receivers
   * @return the collection of nodes n s.t. n invokes some t \in targets on a
   *         receiver r \in receivers
   */
  @SuppressWarnings("unused")
  protected Collection<CGNode> computeRelevantCallers(Collection<CGNode> targets, OrdinalSet<InstanceKey> receivers) {
    HashSet<CGNode> result = new HashSet<CGNode>();
    for (Iterator<CGNode> it = targets.iterator(); it.hasNext();) {
      CGNode t = it.next();
      caller_loop: for (Iterator<? extends CGNode> it2 = getCallGraph().getPredNodes(t); it2.hasNext();) {
        CGNode caller = it2.next();
        IR ir = caller.getIR();
        for (Iterator<CallSiteReference> it3 = getCallGraph().getPossibleSites(caller, t); it3.hasNext();) {
          CallSiteReference site = it3.next();
          SSAAbstractInvokeInstruction[] calls = ir.getCalls(site);
          for (int i = 0; i < calls.length; i++) {
            SSAInvokeInstruction call = (SSAInvokeInstruction) calls[i];
            if (getDFA().matchDispatchEvent(caller, call.getDeclaredTarget().getSignature()) == null) {
              continue;
            }
            if (IGNORE_BENIGN_STATEMENTS) {
              if (getBenignOracle().isBenignStatement(caller, call)) {
                if (DEBUG_LEVEL > 2) {
                  System.err.println("Skipped benign statement.");
                }
                continue;
              }
            }
            if (getDomain() != null && getDomain().hasMessage(caller, call)) {
              // ignore the caller if we have already reported a
              // message for it.
              continue;
            }
            int receiver = call.getReceiver();
            PointerKey r = getPointerAnalysis().getHeapModel().getPointerKeyForLocal(caller, receiver);
            OrdinalSet<InstanceKey> pointsTo = getPointerAnalysis().getPointsToSet(r);
            if (pointsTo.containsAny(receivers)) {
              result.add(caller);
              continue caller_loop;
            }
          }
        }
      }
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return "Solver for " + this.getDFA().toString();
  }

  /**
   * @return Returns the metrics.
   */
  public TypeStateMetrics getMetrics() {
    return metrics;
  }

  /**
   * @return Returns the dfa.
   */
  protected ITypeStateDFA getDFA() {
    return dfa;
  }

  /**
   * A temporary migration aid. This will go away shortly.
   */
  protected TypeStateProperty getDFAAsProperty() {

    assert getDFA() instanceof TypeStateProperty;

    return (TypeStateProperty) getDFA();
  }

  /**
   * @param instances
   *          a set of interesting instances
   * @return a solver result for these set of instances
   * @throws WalaException
   * @throws SolverInterruptedException
   * @throws SetUpException
   * @throws PropertiesException
   * @throws SetUpException
   * @throws CancelException
   */
  @SuppressWarnings("unused")
  protected TypeStateResult solveForInstances(Collection<InstanceKey> instances, AnalysisCache ac) throws WalaException,
      PropertiesException, SetUpException, CancelException {
    OrdinalSet<InstanceKey> instanceSet = toOrdinalInstanceSet(instances);
    logger.fine(() -> "original callgraph: " + getCallGraph().getNumberOfNodes());
    Collection<CGNode> relevantNodes = computeNodesThatMatter(instanceSet);

    logger.fine(() -> "sliced callgraph: " + relevantNodes.size());

    if (relevantNodes.size() == 0) {
      Trace.println("Found no relevant events!");
      return null;
    } else {
      if (DEBUG_LEVEL > 0) {
        Trace.println("Domain:\n" + getDomain().toString());
      }

      WholeProgramSupergraph supergraph = buildSupergraph(ac, relevantNodes);

      if (getOptions().shouldCollectStatistics()) {
        getMetrics().recordSupergraphSize(supergraph.getNumberOfNodes());
      }
      checkGraph(supergraph);

      ICFGTabulationProblem p = createTypeStateProblem(supergraph, instances);

      // Extremely ugly hack to enable tracing property know the merge
      // function
      // used
      // TODO: clean this, please! [EY]
      // this is close to being criminal activity
      if (getOptions().shouldMineDFA()) {
        ((TracingProperty) getDFA()).createInitial((AbstractUnification) p.getMergeFunction());
      }

      TabulationSolver<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Factoid> solver = TabulationSolver.make(p);

      TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Factoid> r = solver.solve();

      if (DEBUG_LEVEL > 0) {
        Trace.println("IFDS Result \n " + r.toString());
      }
      if (GUI_DEBUG) {
        launchGuiExplorer(r);
      }
      if (supportsWitnessGeneration() && (getOptions().shouldGenerateWitness())) {
        try {
          getDomain().populateWitnesses(solver);
        } catch (UnsupportedOperationException e) {
          System.err.println("WARNING: could not populate witness due to " + e.getMessage());
          if (GUI_DEBUG_ON_EXCEPTION) {
            launchGuiExplorer(r);
          }
        }
      }
      
      return new TypeStateResult(r, getDomain(), supergraph);
    }
  }

  /**
   * Perfom the analysis
   * 
   * @throws WalaException
   * @throws SetUpException
   * @throws MaxFindingsException
   * @throws SetUpException
   * @throws CancelException
   * @throws
   */
  public ISolverResult perform(final IProgressMonitor monitor) throws WalaException, SolverTimeoutException, PropertiesException,
      MaxFindingsException, SetUpException, CancelException {
    AnalysisCache ac = new AnalysisCacheImpl();
    monitor.beginTask(null, 1);
    monitor.subTask(toString());
    AggregateSolverResult result = new AggregateSolverResult();
    BenignOracle oracle = getBenignOracle();
    try {
      boolean hasPropertyType = initializeProperty();
      if (hasPropertyType) {
        initializeNoCollapseSet();

        Collection<InstanceKey> instances = computeTrackedInstances();
        result.addPotentialInstances(instances);

        Map<InstanceKey, Set<Pair<CGNode, SSAInstruction>>> potentialErrors = null;
        if (!getOptions().shouldMineDFA()) {
          // get potential error locations

          assert getDFA() instanceof TypeStateProperty;

          TypeStateProperty property = (TypeStateProperty) getDFA();
          potentialErrors = oracle.possibleErrorLocations(property);
        }
        if (getOptions().shouldCollectStatistics() && !getOptions().shouldMineDFA()) {
          getMetrics().setNumberOfCandidateStatements(getPropertyName(), countCandidateStatements(computeTrackedInstancesByType()));
        }

        for (Iterator<InstanceKey> it = instances.iterator(); it.hasNext();) {
          InstanceKey theInstance = it.next();

          if (!oracle.isBenignInstanceKey(theInstance)) {
            Set<InstanceKey> oneInstance = Collections.singleton(theInstance);
            logger.fine(() -> "Solve for " + theInstance);

            initializeDomain(oneInstance);
            TypeStateResult baseResult = solveForInstances(oneInstance, ac);

            result.addInstanceResult(theInstance, baseResult);
          } else {
            result.addSkippedInstance(theInstance);
            System.err.println("Skipped benign instance " + theInstance);
          }

          if (Thread.interrupted()) {
            throw new SolverTimeoutException(result);
          }
        }
        if (!getOptions().shouldMineDFA()) {
          updateBenignOracle(result, instances, potentialErrors);
        }

      } else {
        Trace.println("---No instances of property Type were found---");
      }
    } finally {
      monitor.done();

      // TODO: the following is a ugly hack to be removed ASAP.
      if (getOptions().shouldMineDFA() && getOptions().shouldCollectStatistics()) {
        dumpMetrics();
      }
    }

    return result;
  }

  /**
   * for debug purposes
   */
  protected void dumpMetrics() {
    if (getMetrics() == null) {
      return;
    }
    CallGraphMetricsByLoader primMetrics = getMetrics().getCallGraphMetrics(ClassLoaderReference.Primordial);
    CallGraphMetricsByLoader appMetrics = getMetrics().getCallGraphMetrics(ClassLoaderReference.Application);
    System.out.println(primMetrics.toString());
    System.out.println(appMetrics.toString());

  }

  @SuppressWarnings("unused")
  protected void updateBenignOracle(AggregateSolverResult result, Collection<InstanceKey> potentialInstances,
      Map<InstanceKey, Set<Pair<CGNode, SSAInstruction>>> potentialErrors) {

    Collection<InstanceKey> errInstances = new HashSet<InstanceKey>();
    Collection<Pair<CGNode, SSAInstruction>> errInstructions = HashSetFactory.make();

    for (Iterator<InstanceKey> it = result.iterateInstances(); it.hasNext();) {
      InstanceKey key = it.next();
      ISolverResult curr = result.getInstanceResult(key);
      Collection<? extends Message> messages = curr.getMessages();
      for (Iterator<? extends Message> it2 = messages.iterator(); it2.hasNext();) {
        TypeStateMessage msg = (TypeStateMessage) it2.next();
        InstanceKey errInstance = msg.getInstance();
        CGNode errNode = msg.getCaller();
        SSAInstruction errInstr = msg.getInstruction();

        errInstances.add(errInstance);
        errInstructions.add(Pair.make(errNode, errInstr));

      }
    }

    if (DEBUG_LEVEL > 1) {
      System.err.println("Potential instances: " + potentialInstances.size());
      System.err.println("Potential errors: " + potentialErrors.size());

      System.err.println("Err instances: " + errInstances.size());
      System.err.println("Err instructions: " + errInstructions.size());
    }

    for (Iterator<InstanceKey> it = potentialInstances.iterator(); it.hasNext();) {
      InstanceKey curr = it.next();
      if (!errInstances.contains(curr)) {
        getBenignOracle().addBenignInstanceKey(curr);
      }
      Collection<Pair<CGNode, SSAInstruction>> currErrors = potentialErrors.get(curr);
      if (currErrors != null) {
        for (Iterator<Pair<CGNode, SSAInstruction>> it2 = currErrors.iterator(); it2.hasNext();) {
          Pair<CGNode, SSAInstruction> p = it2.next();
          if (!errInstructions.contains(p)) {
            getBenignOracle().addBenignStatement(p.fst, p.snd);
          }
        }
      }
    }
  }

  /**
   * In order to allow strong updates, we'd better not slice selectively at call
   * sites that might call an event, because the flow functions treat sliced
   * callees as identity flow; hence we'd lose strong updates by over-aggressive
   * slicing there.
   * 
   * @param slice
   *          Collection<CGNode>
   * @param eventNodes
   *          Collection<CGNode>
   */
  protected void addAllCalleesOfEventSites(Collection<CGNode> slice, Collection<CGNode> eventNodes) {
    for (Iterator<CGNode> it = eventNodes.iterator(); it.hasNext();) {
      CGNode event = it.next();
      if (slice.contains(event)) {
        for (Iterator<? extends CGNode> it2 = getCallGraph().getPredNodes(event); it2.hasNext();) {
          CGNode caller = it2.next();
          for (Iterator<CallSiteReference> it3 = getCallGraph().getPossibleSites(caller, event); it3.hasNext();) {
            CallSiteReference site = it3.next();
            slice.addAll(getCallGraph().getPossibleTargets(caller, site));
          }
        }
      }
    }
  }

  protected abstract TypeStateProblem createTypeStateProblem(ICFGSupergraph supergraph, Collection<InstanceKey> instances)
      throws WalaException, PropertiesException;

  /**
   * @return number of possible statements that can cause findings for this
   *         property, when restricted to the instances in the collection
   */
  protected int countCandidateStatements(Collection<InstanceKey> instances) {
    int count = 0;
    for (Iterator<CGNode> it = getCallGraph().iterator(); it.hasNext();) {
      CGNode n = it.next();
      if (n.getMethod().isStatic()) {
        continue;
      }

      assert (getDFA() instanceof TypeStateProperty);

      TypeStateProperty property = (TypeStateProperty) getDFA();
      if (property.receives(n.getMethod())) {
        String sig = n.getMethod().getSignature();
        IEvent e = property.matchDispatchEvent(sig);
        if (property.eventTransitionsToAccept(e)) {
          for (Iterator<? extends CGNode> it2 = getCallGraph().getPredNodes(n); it2.hasNext();) {
            CGNode caller = it2.next();
            if (!NO_LIBRARY_ERRORS || isApplicationNode(caller)) {
              IR ir = caller.getIR();
              sites: for (Iterator<CallSiteReference> it3 = getCallGraph().getPossibleSites(caller, n); it3.hasNext();) {
                CallSiteReference site = it3.next();
                SSAAbstractInvokeInstruction[] calls = ir.getCalls(site);
                for (int i = 0; i < calls.length; i++) {
                  int recv = calls[i].getReceiver();
                  PointerKey pk = getPointerAnalysis().getHeapModel().getPointerKeyForLocal(caller, recv);
                  Collection<InstanceKey> c = OrdinalSet.toCollection(getPointerAnalysis().getPointsToSet(pk));
                  if (c.removeAll(instances)) {
                    count++;
                    continue sites;
                  }
                }
              }
            }
          }

        }
      }
    }
    return count;
  }

  /**
   * @return Returns the traceReporter.
   */
  protected TraceReporter getTraceReporter() {
    return traceReporter;
  }

  /**
   * @return Returns the options.
   */
  protected TypeStateOptions getOptions() {
    return (TypeStateOptions) options;
  }

}