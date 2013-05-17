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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.safe.Factoid;
import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.internal.exceptions.MaxFindingsException;
import com.ibm.safe.internal.exceptions.PropertiesException;
import com.ibm.safe.internal.exceptions.SetUpException;
import com.ibm.safe.internal.exceptions.SolverTimeoutException;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.reporting.message.ISolverResult;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.typestate.core.AbstractTypestateSolver;
import com.ibm.safe.typestate.core.BenignOracle;
import com.ibm.safe.typestate.core.TypeStateFunctionProvider;
import com.ibm.safe.typestate.core.TypeStateMessage;
import com.ibm.safe.typestate.core.TypeStateProblem;
import com.ibm.safe.typestate.core.TypeStateProperty;
import com.ibm.safe.typestate.core.TypeStateResult;
import com.ibm.safe.typestate.merge.IMergeFunctionFactory;
import com.ibm.safe.typestate.metrics.TypeStateMetrics;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.dataflow.IFDS.TabulationResult;
import com.ibm.wala.dataflow.IFDS.TabulationSolver;
import com.ibm.wala.escape.ILiveObjectAnalysis;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * An intra-procedural typestate solver
 * 
 * @author sfink
 * @author yahave
 */
public abstract class AbstractLocalSolver extends AbstractTypestateSolver {

  /**
   * Map: InstanceKey -> Set<CGNode>
   */
  private final Map<InstanceKey, Collection<CGNode>> nodesThatMatter = HashMapFactory.make();

  /**
   * @param cg
   * @param pointerAnalysis
   * @param property
   * @param options
   * @param live
   * @param warnings
   */
  public AbstractLocalSolver(CallGraph cg, PointerAnalysis pointerAnalysis, TypeStateProperty property, TypeStateOptions options,
      ILiveObjectAnalysis live, BenignOracle ora, TypeStateMetrics metrics, IReporter reporter,
      IMergeFunctionFactory mergeFunctionFactory) {
    super(cg, pointerAnalysis, property, options, live, ora, metrics, reporter, mergeFunctionFactory);
  }

  /**
   * @param instances
   * @return set of nodes which are relevant to the dataflow solution of the set
   *         of instances
   * @throws PropertiesException
   */
  protected Collection<CGNode> getNodesThatMatter(Collection<InstanceKey> instances) throws PropertiesException {
    Set<CGNode> result = HashSetFactory.make();
    for (Iterator<InstanceKey> it = instances.iterator(); it.hasNext();) {
      InstanceKey ik = it.next();
      result.addAll(getNodesThatMatter(ik));
    }
    return result;
  }

  /**
   * @param ik
   * @return set of nodes which are relevant to the dataflow solution of ik
   * @throws PropertiesException
   */
  private Collection<CGNode> getNodesThatMatter(InstanceKey ik) throws PropertiesException {
    Collection<CGNode> result = nodesThatMatter.get(ik);
    if (result == null) {
      result = computeNodesThatMatter(ik);
      nodesThatMatter.put(ik, result);
    }
    return result;
  }

  /**
   * @param ik
   * @return set of nodes which are relevant to the dataflow solution of ik
   * @throws PropertiesException
   */
  protected abstract Collection<CGNode> computeNodesThatMatter(InstanceKey ik) throws PropertiesException;

  /**
   * @return an instance of the "normal", interprocedural flow function provider
   * @throws PropertiesException
   */
  protected abstract TypeStateFunctionProvider makeFlowFunctions(ICFGSupergraph supergraph, Collection<InstanceKey> instances)
      throws PropertiesException;

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.AbstractSolver#supportsWitnessGeneration()
   */
  protected boolean supportsWitnessGeneration() {
    Assertions.UNREACHABLE();
    return false;
  }

  @SuppressWarnings("unused")
  public ISolverResult perform(IProgressMonitor monitor) throws WalaException, SolverTimeoutException, MaxFindingsException,
      SetUpException, PropertiesException {
    monitor.beginTask(null, 1);
    monitor.subTask(toString());
    LocalSolverResult result = new LocalSolverResult();
    try {
      boolean hasPropertyType = initializeProperty();
      if (hasPropertyType != false) {
        Collection<InstanceKey> instances = computeTrackedInstances();

        OrdinalSet<InstanceKey> ordInstances = toOrdinalInstanceSet(instances);

        Collection<CGNode> acceptNodes = scanForEventNodes(ordInstances);
        acceptNodes = pruneForAccept(acceptNodes);

        // nodeSet := set of nodes which may directly go to error state.
        Collection<CGNode> nodeSet = computeRelevantCallers(acceptNodes, ordInstances);

        BenignOracle oracle = getBenignOracle();

        Collection<Pair<CGNode, SSAInstruction>> instructions = HashSetFactory.make();

        for (Iterator<CGNode> it = nodeSet.iterator(); it.hasNext();) {
          CGNode n = it.next();
          if (!oracle.isBenignMethod(n.getMethod())) {
            // System.err.println(n);

            // BFSPathFinder bfs = new
            // BFSPathFinder(getCallGraph(),getCallGraph().getFakeRootNode(),n);
            // List L = bfs.find();
            // for (Iterator it2 = L.iterator(); it2.hasNext(); ) {
            // System.err.println(it2.next());
            // }
            Collection<InstanceKey> relevant = computeInstancesForNode(n, ordInstances, acceptNodes);

            Collection<Pair<CGNode, SSAInstruction>> relevantInstructions = oracle.computeMethodErrorInstructions(n, acceptNodes,
                toOrdinalInstanceSet(relevant));
            instructions.addAll(relevantInstructions);
            // System.err.println(relevantInstructions);

            initializeDomain(relevant);

            SingleProcedureSupergraph supergraph = buildSupergraph(n);
            checkGraph(supergraph);

            TypeStateProblem p = createTypeStateProblem(n, supergraph, relevant);
            TabulationSolver<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Factoid> solver = TabulationSolver.make(p);
            TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Factoid> r = solver.solve();

            if (DEBUG_LEVEL > 0) {
              Trace.println("IFDS Result \n " + r.toString());
            }
            if (GUI_DEBUG) {
              launchGuiExplorer(r);
            }
            TypeStateResult t = new TypeStateResult(r, getDomain(), supergraph);
            result.compose(t);
          }
          if (Thread.interrupted()) {
            throw new SolverTimeoutException(result);
          }
        }
        updateBenignOracle(instances, instructions, result);

      } else {
        Trace.println("---No instances of property Type were found---");
      }
    } catch (CancelException e) {
      throw new SolverTimeoutException(result);
    } finally {
      monitor.done();
    }

    return result;
  }

  /**
   * Update benign oracle according to analysis results. All instances that were
   * processed and for which no error was reported are sent to benign-oracle for
   * accumulation.
   * 
   * @param instances
   * @param t
   */
  private void updateBenignOracle(Collection<InstanceKey> instances, Collection<Pair<CGNode, SSAInstruction>> instructions,
      LocalSolverResult t) {
    Collection<Message> messages = t.getMessages();
    Collection<InstanceKey> errInstances = new HashSet<InstanceKey>();
    Collection<Pair<CGNode, SSAInstruction>> errInstructions = HashSetFactory.make();
    for (Iterator<Message> it = messages.iterator(); it.hasNext();) {
      TypeStateMessage msg = (TypeStateMessage) it.next();
      errInstances.add(msg.getInstance());
      errInstructions.add(Pair.make(msg.getCaller(), msg.getInstruction()));
    }
    for (Iterator<InstanceKey> instanceIt = instances.iterator(); instanceIt.hasNext();) {
      InstanceKey curr = instanceIt.next();
      if (!errInstances.contains(curr)) {
        // System.err.println("---Benign instances: " + curr);
        getBenignOracle().addBenignInstanceKey(curr);
      }
    }
    if (!messages.isEmpty()) {
      // make sure we are getting instructions from the messages
      assert (!errInstructions.isEmpty());
    }
    for (Iterator<Pair<CGNode, SSAInstruction>> instrIt = instructions.iterator(); instrIt.hasNext();) {
      Pair<CGNode, SSAInstruction> p = instrIt.next();
      if (!errInstructions.contains(p)) {
        getBenignOracle().addBenignStatement((CGNode) p.fst, (SSAInstruction) p.snd);
      }
    }
  }

  private SingleProcedureSupergraph buildSupergraph(CGNode n) {
    IR ir = n.getIR();
    ControlFlowGraph<SSAInstruction, IExplodedBasicBlock> cfg = ExplodedControlFlowGraph.make(ir);
    return new SingleProcedureSupergraph(getCallGraph(),n, cfg);
  }

  /**
   * compute the set of instances that might flow to an accept node from node n
   */
  private Collection<InstanceKey> computeInstancesForNode(CGNode n, OrdinalSet<InstanceKey> instances,
      Collection<CGNode> acceptNodes) {
    IR ir = n.getIR();
    Collection<InstanceKey> result = HashSetFactory.make(instances.size());
    for (Iterator<CGNode> it = acceptNodes.iterator(); it.hasNext();) {
      CGNode a = it.next();
      if (getCallGraph().hasEdge(n, a)) {
        for (Iterator<CallSiteReference> it2 = getCallGraph().getPossibleSites(n, a); it2.hasNext();) {
          CallSiteReference site = it2.next();
          SSAAbstractInvokeInstruction[] calls = ir.getCalls(site);
          for (int i = 0; i < calls.length; i++) {
            SSAInvokeInstruction call = (SSAInvokeInstruction) calls[i];
            int receiver = call.getReceiver();
            PointerKey r = getPointerAnalysis().getHeapModel().getPointerKeyForLocal(n, receiver);
            OrdinalSet<InstanceKey> pointsTo = getPointerAnalysis().getPointsToSet(r);
            for (Iterator<InstanceKey> it3 = OrdinalSet.intersect(instances, pointsTo).iterator(); it3.hasNext();) {
              result.add(it3.next());
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * @param eventNodes
   *          set<CGNode>
   * @return set<CGNode> \in eventNodes s.t. the node may transition to an
   *         accepting state
   */
  private Collection<CGNode> pruneForAccept(Collection<CGNode> eventNodes) {
    Collection<CGNode> result = HashSetFactory.make(eventNodes.size());
    for (Iterator<CGNode> it = eventNodes.iterator(); it.hasNext();) {
      CGNode n = it.next();
      if (getDFA().receives(n.getMethod())) {
        String sig = n.getMethod().getSignature();
        IEvent e = getDFA().matchDispatchEvent(sig);
        if (eventTransitionsToAccept(e)) {
          result.add(n);
        }
      }
    }
    return result;
  }

  protected abstract TypeStateProblem createTypeStateProblem(CGNode node, ICFGSupergraph supergraph,
      Collection<InstanceKey> instances) throws WalaException, PropertiesException;

}
