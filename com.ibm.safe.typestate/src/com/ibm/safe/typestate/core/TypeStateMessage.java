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
 * Created on Dec 10, 2004
 */
package com.ibm.safe.typestate.core;

import com.ibm.safe.reporting.message.Location;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSite;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.util.debug.Assertions;

/**
 * A message to be used when reporting a typestate violation. This objects
 * serves as an aggregate for all information contained in a typestate violation
 * message. note: please note that in our terminology, the typestate automaton's
 * only accepting state is the error state.
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public class TypeStateMessage implements Message {

  /**
   * instance-key for which message is reported
   */
  public InstanceKey instance;

  /**
   * callGraph Node for which the message is reported
   */
  private final CGNode cgnode;

  /**
   * Instruction for which the message is reported
   */
  public SSAInstruction instr;

  /**
   * Supergraph node for which the message is reported
   */
  private final Object supergraphNode;

  /**
   * The state we were in before transitioning to accept
   */
  private final BaseFactoid inputFact;

  /**
   * caller of the method for which the error is reported (SJF: do we need
   * this?)
   */
  public CGNode caller;

  /**
   * additional information
   */
  public Object additionalInfo;

  /**
   * underlying property
   */
  public TypeStateProperty property;

  /**
   * 
   */
  private int lineNumber;

  /**
   * a path in the exploded supergraph which demonstrates the problem
   */
  //private TypeStateWitness witness;

  public TypeStateMessage(TypeStateProperty property, BaseFactoid inputFact, CGNode cgnode, Object supergraphNode,
      SSAInvokeInstruction instr, CGNode caller) {
    this.property = property;
    this.inputFact = inputFact;
    this.cgnode = cgnode;
    this.supergraphNode = supergraphNode;
    this.instance = inputFact.instance;
    this.instr = instr;
    this.caller = caller;
    try {
      this.lineNumber = invokeInstructionLineNumber();
    } catch (InvalidClassFileException e) {
      e.printStackTrace();
      Assertions.UNREACHABLE();
    }
  }

  public TypeStateMessage(TypeStateProperty property, BaseFactoid inputFact, CGNode cgnode, Object supergraphNode,
      SSAInvokeInstruction instr, CGNode caller, Object additionalInfo) {
    this.property = property;
    this.inputFact = inputFact;
    this.cgnode = cgnode;
    this.supergraphNode = supergraphNode;
    this.instance = inputFact.instance;
    this.instr = instr;
    this.caller = caller;
    this.additionalInfo = additionalInfo;
    try {
      this.lineNumber = invokeInstructionLineNumber();
    } catch (InvalidClassFileException e) {
      e.printStackTrace();
      Assertions.UNREACHABLE();
    }
  }

  public TypeStateMessage(TypeStateProperty property, BaseFactoid inputFact, CGNode cgnode, Object supergraphNode,
      SSAInstruction instr, CGNode caller, Object additionalInfo, int lineNumber) {
    this.property = property;
    this.inputFact = inputFact;
    this.cgnode = cgnode;
    this.supergraphNode = supergraphNode;
    this.instance = inputFact.instance;
    this.instr = instr;
    this.caller = caller;
    this.additionalInfo = additionalInfo;
    this.lineNumber = lineNumber;
  }

  public int getAllocSiteLineNumber() {
    if (instance instanceof AllocationSite) {
      AllocationSite allocInstance = (AllocationSite) instance;
      NewSiteReference siteRef = allocInstance.getSite();
      return siteRef.getProgramCounter();
    }
    return 0;
  }

  /**
   * Get the source line number for this message (note the use of getLineNumber
   * over the PC from the instruction) This works only for invoke instructions;
   * 
   * @return a source line number
   * @throws InvalidClassFileException
   */
  private int invokeInstructionLineNumber() throws InvalidClassFileException {
    if (instr == null) {
      return -1;
    }
    assert (instr instanceof SSAInvokeInstruction);
    int pc = ((SSAInvokeInstruction) instr).getProgramCounter();
    IMethod m = caller.getMethod();
    return m != null ? m.getLineNumber(pc) : -1;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public IMethod getMethod() {
    return caller != null ? caller.getMethod() : null;
  }

  public SSAInstruction getInstruction() {
    return instr;
  }

  public InstanceKey getInstance() {
    return instance;
  }

  public CGNode getCaller() {
    return caller;
  }

  public IMethod getAllocSiteMethod() {
    IMethod m = null;

    if (instance instanceof AllocationSite) {
      AllocationSite allocInstance = (AllocationSite) instance;
      m = allocInstance.getMethod();
    }
    return m;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.base.Message#getRule()
   */
  public IRule getRule() {
    return property.getRule();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.base.Message#getLocation()
   */
  public Location getLocation() {
    Location result;
    if (instr != null) {
      result = Location.createMethodLocation(getMethod().getDeclaringClass().getName(), getMethod().getSelector(), getLineNumber());
    } else {
      result = Location.createUnknownLocation();
    }
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.base.Message#getMessageType()
   */
  public String getMessageType() {
    return "typestate"; //$NON-NLS-1$
  }

  public Object getAdditionalInfo() {
    return additionalInfo;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (!(other instanceof TypeStateMessage)) {
      return false;
    }
    TypeStateMessage otherMessage = (TypeStateMessage) other;
    return ((instr != null ? instr.equals(otherMessage.instr) : true) && (cgnode != null ? cgnode.equals(otherMessage.cgnode)
        : true));
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return (instr != null ? instr.hashCode() : 0) + 31 * (cgnode != null ? cgnode.hashCode() : 0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    final StringBuilder strBuilder = new StringBuilder();
    strBuilder.append(getRule().getName()).append(" (").append(getLocation().getSourceLocation()) //$NON-NLS-1$
        .append(')');
    return strBuilder.toString();
  }

  /**
   * add a witness to this message: a path in the exploded supergraph which
   * demonstrates the problem.
   */
//  public void populateWitness(TypeStateDomain domain, TabulationSolver solver) {
//    System.err.println("populate witness for " + this);
//    ExplodedSupergraphNode<Object> sink = computeSink(domain, solver.getSupergraph());
//    System.err.println("sink " + sink);
//    ExplodedSupergraphWithSummaryEdges<Object> esg = new ExplodedSupergraphWithSummaryEdges<Object>(solver.getSupergraph(), solver
//        .getProblem().getFunctionMap(), solver);
//    ExplodedSupergraphPath<Object> path = ExplodedSupergraphPath.findRealizablePath(esg, sink);
//    System.err.println("path computed");
//    if (path == null) {
//      throw new UnsupportedOperationException("witness generation failed, null path");
//    }
//    // System.err.println("PATH: " + path);
//    path = ExplodedSupergraphPath.summarize(solver.getSupergraph(), path);
//    if (Assertions.verifyAssertions) {
//      Assertions._assert(path != null);
//    }
//    // System.err.println("SUMMARIZED PATH: " + path);
//    if (instr != null) {
//      witness = new TypeStateWitness(instance, instr, solver.getProblem(), path);
//      System.err.println("witness: " + witness);
//    } else {
//      System.err.println("witness generation n/a");
//    }
//
//  }

  /**
   * @return an exploded supergraph node which represents this accepting message
   */
//  private ExplodedSupergraphNode<Object> computeSink(TypeStateDomain domain, ISupergraph supergraph) {
//    IBasicBlock block = (IBasicBlock) supergraphNode;
//
//    return new ExplodedSupergraphNode<Object>(block, domain.getMappedIndex(inputFact));
//  }
  //
  // /**
  // * @return an exploded supergraph node which represents the initial fact
  // at
  // * the basic block which allocates the instance.
  // */
  // private ExplodedSupergraphNode computeSource(TypeStateDomain domain,
  // ISupergraph supergraph) {
  // AllocationSiteKey alloc = (AllocationSiteKey) instance;
  // CallGraph cg = ((PartiallyCollapsedSupergraph)
  // supergraph).getCallGraph();
  // IR ir = ((SSAContextInterpreter)
  // cg.getInterpreter(alloc.getNode())).getIR(alloc.getNode(), new
  // WarningSet());
  // int instructionIndex = ir.getNewInstructionIndex(alloc.getSite());
  // IBasicBlock block =
  // ir.getControlFlowGraph().getBlockForInstruction(instructionIndex);
  //
  // return new ExplodedSupergraphNode(block, domain.getIndex(instance,
  // property.initial()));
  // }

  public String getText() {
    return toString();
  }

}