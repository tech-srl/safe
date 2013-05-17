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
package com.ibm.safe.typestate.strongUpdate;

import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.typestate.base.BaseCallFlowFunction;
import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * An extension of the base (actually separating) typestate solver that
 * unsoundly always performs strong updates.
 * 
 * @author Stephen Fink
 * @author yahave
 */
public class StrongUpdateCallFlowFunction extends BaseCallFlowFunction {

  public StrongUpdateCallFlowFunction(TypeStateDomain domain, ITypeStateDFA dfa, OrdinalSet<InstanceKey> instances, IEvent event,
      BasicBlockInContext<IExplodedBasicBlock> block, SSAInvokeInstruction invokeInstr, CGNode caller) {
    super(domain, dfa, instances, event, block, invokeInstr, caller);
  }

  /**
   * Unconditionally, unsoundly, perform strong update.
   */
  protected boolean strongUpdate(BaseFactoid inputFact) {
    return true;
  }

}