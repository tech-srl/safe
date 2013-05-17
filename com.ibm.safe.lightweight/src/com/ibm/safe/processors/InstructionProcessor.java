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
package com.ibm.safe.processors;

import java.util.Collection;
import java.util.Map;

import com.ibm.safe.intraproc.sccp.SCCPValue;
import com.ibm.safe.reporting.message.Message;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.CancelException;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public interface InstructionProcessor {

  public void processProlog(SSAInstruction inst);

  public void process(SSAInstruction inst, int bcIndex);

  public void processEpilog(SSAInstruction inst);

  public Collection<? extends Message> getResult();

  public void setup(IMethod method, Map<Integer, SCCPValue> context, IR ir) throws CancelException;
}