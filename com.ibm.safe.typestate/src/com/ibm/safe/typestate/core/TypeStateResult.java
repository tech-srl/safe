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
/*
 * Created on Dec 6, 2004
 */
package com.ibm.safe.typestate.core;

import java.util.Collection;
import java.util.Set;

import com.ibm.safe.Factoid;
import com.ibm.safe.ICFGSupergraph;
import com.ibm.safe.reporting.message.ISolverResult;
import com.ibm.safe.reporting.message.Message;
import com.ibm.wala.dataflow.IFDS.TabulationResult;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;

/**
 * @author Eran Yahav (yahave)
 * 
 * Results from a single run of a typestate solver
 */
public class TypeStateResult implements ISolverResult {

  private TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Factoid> result;

  protected final TypeStateDomain domain;

  private ICFGSupergraph supergraph;

  public TypeStateResult(TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Factoid> r, TypeStateDomain domain, ICFGSupergraph supergraph) {
	this.result = r;
    this.domain = domain;
    this.supergraph = supergraph;
  }

  public Set<? extends Message> getMessages() {
    return domain.getMessages();
  }

  public String toString() {
    return getMessages().toString();
  }

  public void addMessages(Set<? extends Message> messageSet) {
    Set<TypeStateMessage> existing = domain.getMessages();
    for (Message message : messageSet) {
      if (message instanceof TypeStateMessage) {
        existing.add((TypeStateMessage) message);
      } else {
        throw new RuntimeException("Incompatible message type");
      }
    }
  }

  public Collection<InstanceKey> getAcceptingInstances() {
    return domain.getAcceptingInstances();
  }

  public TabulationResult<BasicBlockInContext<IExplodedBasicBlock>, CGNode, Factoid> getResult() {
    return result;
  }

  public TypeStateDomain getDomain() {
    return domain;
  }

  public ICFGSupergraph getSupergraph() {
    return supergraph;
  }
}