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
package com.ibm.safe.typestate.unique;

import java.util.Iterator;

import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.typestate.base.BaseCallFlowFunction;
import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.HeapModel;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * The unique-flow function exploits the "uniqueness" information stored in the
 * domain to perform strong-update of the automaton state whenever the
 * allocation site is known to be unique.
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 * 
 */
public class UniqueCallFlowFunction extends BaseCallFlowFunction {

  /**
   * true iff the FI pointer analysis has a unique possible receiver for this
   * call
   */
  private boolean uniqueMayTarget;

  public UniqueCallFlowFunction(TypeStateDomain domain, ITypeStateDFA dfa, OrdinalSet<InstanceKey> instances, IEvent event,
      BasicBlockInContext<IExplodedBasicBlock> block, SSAInvokeInstruction invokeInstr, CGNode caller, CGNode callee, PointerAnalysis pointerAnalysis,
      IClassHierarchy cha) {
    super(domain, dfa, instances, event, block, invokeInstr, caller);
    if (invokeInstr.getCallSite().isStatic()) {
      uniqueMayTarget = false;
    } else {
      HeapModel hm = pointerAnalysis.getHeapModel();
      OrdinalSet<InstanceKey> pointsTo = pointerAnalysis
          .getPointsToSet(hm.getPointerKeyForLocal(caller, invokeInstr.getReceiver()));
      uniqueMayTarget = existsUniqueReceiver(pointsTo, callee, cha);
    }
  }

  public static boolean existsUniqueReceiver(OrdinalSet<InstanceKey> pointsTo, CGNode callee, IClassHierarchy cha) {
    boolean foundOne = false;
    for (Iterator<InstanceKey> it = pointsTo.iterator(); it.hasNext();) {
      InstanceKey ik = it.next();
      IClass type = ik.getConcreteType();
      IMethod target = cha.resolveMethod(type, callee.getMethod().getSelector());
      if (target.getReference().equals(callee.getMethod().getReference())) {
        if (foundOne) {
          // found second target.
          return false;
        } else {
          foundOne = true;
        }
      }
    }
    return true;
  }

  /**
   * Should this function use strong update to kill the inputFact?
   * 
   * Subclasses should override as desired.
   */
  protected boolean strongUpdate(BaseFactoid inputFact) {
    UniqueFactoid triplet = (UniqueFactoid) inputFact;
    return uniqueMayTarget && triplet.isUnique();
  }

}