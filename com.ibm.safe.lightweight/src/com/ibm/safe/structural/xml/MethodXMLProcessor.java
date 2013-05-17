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
 * Created on Dec 25, 2004
 */
package com.ibm.safe.structural.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.safe.processors.InstructionProcessor;
import com.ibm.safe.processors.MethodProcessor;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAOptions;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class MethodXMLProcessor implements MethodProcessor {

  private static final String BODY_TAG = "body";

  // private static final String INSTR_TAG = "inst";

  private static final String ID_TAG = "id";

  private InstructionXMLProcessor instXMLModel = new InstructionXMLProcessor();

  protected IClassHierarchy cha;

  protected CallGraph callGraph;

  protected Document doc;

  private Element bodyRoot;

  public MethodXMLProcessor(IClassHierarchy cha, CallGraph callGraph) {
    this.cha = cha;
    this.callGraph = callGraph;
  }

  public void setup(IClass c, Object context) {
  }

  public void setup(IMethod method, Document doc) {
    this.doc = doc;
  }

  public void addInstructionProcessor(InstructionProcessor ip) {
    throw new UnsupportedOperationException();
  }

  public void processProlog(IMethod method) {
    this.bodyRoot = doc.createElement(BODY_TAG);
  }

  public void processEpilog(IMethod method) {

  }

  public Object getResult() {
    return bodyRoot;
  }

  /**
   * Creates DOM sub-document for given method
   * 
   * @param method -
   *            analyzed method
   */
  public void process(IMethod method) {
    if (method.isAbstract() || method.isNative()) {
      return;
    }

    IR methodIR = new AnalysisCache().getSSACache().findOrCreateIR(method, Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
    int i = 0;
    instXMLModel.setDocument(doc);
    instXMLModel.setEnvironment(methodIR);
    for (int j = 0, size = methodIR.getInstructions().length; j < size; ++j) {
      SSAInstruction instr = methodIR.getInstructions()[j];
      if (instr != null) {
        instXMLModel.processProlog(instr);
        instXMLModel.process(instr, methodIR.getControlFlowGraph().getProgramCounter(j));
        instXMLModel.processEpilog(instr);
        Element instrElement = (Element) instXMLModel.getResultElement();
        instrElement.setAttribute(ID_TAG, String.valueOf(i));
        i++;
        bodyRoot.appendChild(instrElement);
      }
    }
  }
}