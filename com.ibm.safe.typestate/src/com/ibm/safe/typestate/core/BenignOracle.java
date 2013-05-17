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

import com.ibm.safe.dfa.events.IEvent;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.MapUtil;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;
import com.ibm.wala.util.intset.OrdinalSetMapping;

public class BenignOracle {

  private final static int DEBUG_LEVEL = 1;

  private final static boolean NO_LIBRARY_ERRORS = true;

  private final CallGraph cg;

  private final PointerAnalysis pointerAnalysis;

  private Set<InstanceKey> benignInstanceKeys = HashSetFactory.make();

  private Set<Pair<CGNode, SSAInstruction>> benignStatements = HashSetFactory.make();

  public BenignOracle(CallGraph callGraph, PointerAnalysis pointerAnalysis) {
    this.cg = callGraph;
    this.pointerAnalysis = pointerAnalysis;
  }

  public boolean isBenignInstanceKey(InstanceKey ik) {
    return benignInstanceKeys.contains(ik);
  }

  public boolean isBenignStatement(CGNode caller, SSAInvokeInstruction call) {
    return benignStatements.contains(Pair.make(caller, call));
  }

  @SuppressWarnings("unused")
  public boolean isBenignMethod(IMethod m) {
    // TODO: extend this to handle more cases
    if (NO_LIBRARY_ERRORS) {
      return !(m.getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application));
    }
    return false;
  }

  public void addBenignInstanceKey(InstanceKey ik) {
    if (DEBUG_LEVEL > 0) {
      System.err.println("Adding instancekey: " + ik);
    }
    benignInstanceKeys.add(ik);
  }

  public void addBenignStatement(CGNode node, SSAInstruction curr) {
    if (DEBUG_LEVEL > 0) {
      System.err.println("Adding statement: " + node + "," + curr);
    }
    benignStatements.add(Pair.make(node, curr));
  }

  public Map<InstanceKey, Set<Pair<CGNode, SSAInstruction>>> possibleErrorLocations(TypeStateProperty property) {
    HashMap<InstanceKey, Set<Pair<CGNode, SSAInstruction>>> result = HashMapFactory.make();
    // scan nodes and collect instances that flow to each event
    for (Iterator<CGNode> it = cg.iterator(); it.hasNext();) {
      CGNode n = it.next();
      if (n.getMethod().isStatic()) {
        continue;
      }
      if (property.receives(n.getMethod())) {
        String sig = n.getMethod().getSignature();
        IEvent e = property.matchDispatchEvent(sig);
        if (property.eventTransitionsToAccept(e)) {
          // value number 1 is the receiver
          PointerKey receiver = pointerAnalysis.getHeapModel().getPointerKeyForLocal(n, 1);
          OrdinalSet<InstanceKey> recvrs = pointerAnalysis.getPointsToSet(receiver);
          for (Iterator<InstanceKey> it2 = recvrs.iterator(); it2.hasNext();) {
            InstanceKey instance = it2.next();
            Set<Pair<CGNode, SSAInstruction>> s = MapUtil.findOrCreateSet(result, instance);
            Collection<Pair<CGNode, SSAInstruction>> callerInstructions = computeRelevantInstructions(Collections.singleton(n),
                toOrdinalInstanceSet(Collections.singleton(instance)));
            for (Iterator<Pair<CGNode, SSAInstruction>> cit = callerInstructions.iterator(); cit.hasNext();) {
              Pair<CGNode, SSAInstruction> p = cit.next();
              CGNode caller = (CGNode) p.fst;
              if (!NO_LIBRARY_ERRORS) {
                s.add(p);
              } else if (isApplicationNode(caller)) {
                s.add(p);
              }
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * @param targets
   * @param receivers
   * @return the collection of Pair<CGNode,SSAInvokeInstruction> s.t. it
   *         invokes some t \in targets on a receiver r \in receivers
   */
  public Collection<Pair<CGNode, SSAInstruction>> computeMethodErrorInstructions(CGNode caller, Collection<CGNode> targets,
      OrdinalSet<InstanceKey> receivers) {
    HashSet<Pair<CGNode, SSAInstruction>> result = HashSetFactory.make();
    IR ir = caller.getIR();
    for (Iterator<CGNode> it = targets.iterator(); it.hasNext();) {
      CGNode t = it.next();
      for (Iterator<CallSiteReference> it2 = cg.getPossibleSites(caller, t); it2.hasNext();) {
        CallSiteReference site = it2.next();
        SSAAbstractInvokeInstruction[] calls = ir.getCalls(site);
        for (int i = 0; i < calls.length; i++) {
          SSAInvokeInstruction call = (SSAInvokeInstruction) calls[i];
          int receiver = call.getReceiver();
          PointerKey r = pointerAnalysis.getHeapModel().getPointerKeyForLocal(caller, receiver);
          OrdinalSet<InstanceKey> pointsTo = pointerAnalysis.getPointsToSet(r);
          if (pointsTo.containsAny(receivers)) {
            result.add(Pair.make(caller, (SSAInstruction) call));
          }
        }
      }
    }
    return result;
  }

  /**
   * @param callers
   *            Collection<Pair<CGNode,SSAInstruction>>
   * @return true iff some n /in callers is from the application loader
   */
  boolean pairContainsApplicationNode(Collection<Pair<CGNode, SSAInstruction>> callers) {
    for (Iterator<Pair<CGNode, SSAInstruction>> it = callers.iterator(); it.hasNext();) {
      Pair<CGNode, SSAInstruction> p = it.next();
      CGNode n = (CGNode) p.fst;
      if (n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)) {
        return true;
      }
    }
    return false;
  }

  boolean isApplicationNode(CGNode n) {
    return n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application);
  }

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
   * @param targets
   * @param receivers
   * @return the collection of <node,instruction> Pairs s.t. n invokes some t
   *         \in targets on a receiver r \in receivers
   */
  protected Collection<Pair<CGNode, SSAInstruction>> computeRelevantInstructions(Collection<CGNode> targets,
      OrdinalSet<InstanceKey> receivers) {
    HashSet<Pair<CGNode, SSAInstruction>> result = HashSetFactory.make();
    for (Iterator<CGNode> it = targets.iterator(); it.hasNext();) {
      CGNode t = it.next();
      caller_loop: for (Iterator<? extends CGNode> it2 = cg.getPredNodes(t); it2.hasNext();) {
        CGNode caller = (CGNode) it2.next();
        IR ir = caller.getIR();
        for (Iterator<CallSiteReference> it3 = cg.getPossibleSites(caller, t); it3.hasNext();) {
          CallSiteReference site = (CallSiteReference) it3.next();
          SSAAbstractInvokeInstruction[] calls = ir.getCalls(site);
          for (int i = 0; i < calls.length; i++) {
            SSAInvokeInstruction call = (SSAInvokeInstruction) calls[i];
            int receiver = call.getReceiver();
            PointerKey r = pointerAnalysis.getHeapModel().getPointerKeyForLocal(caller, receiver);
            OrdinalSet<InstanceKey> pointsTo = pointerAnalysis.getPointsToSet(r);
            if (pointsTo.containsAny(receivers)) {
              result.add(Pair.make(caller, (SSAInstruction) call));
              continue caller_loop;
            }
          }
        }
      }
    }
    return result;
  }
}
