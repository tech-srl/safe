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

import java.util.Set;

import com.ibm.safe.reporting.message.ISolverResult;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.typestate.core.TypeStateResult;
import com.ibm.wala.util.collections.HashSetFactory;

/**
 * results from a local typestate solver
 * 
 * @author sfink
 * @author yahave
 */
public class LocalSolverResult implements ISolverResult {

  private final Set<Message> messages = HashSetFactory.make();

  public LocalSolverResult() {
    super();
  }

  public Set<Message> getMessages() {
    return messages;
  }

  public void addMessages(Set<? extends Message> messageSet) {
    messages.addAll(messageSet);
  }

  public void compose(TypeStateResult t) {
    messages.addAll(t.getMessages());
  }

}
