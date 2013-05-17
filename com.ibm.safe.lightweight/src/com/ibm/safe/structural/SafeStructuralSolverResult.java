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
 * Created on Dec 16, 2004
 */
package com.ibm.safe.structural;

import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.reporting.message.ISolverResult;
import com.ibm.safe.reporting.message.Message;
import com.ibm.wala.util.collections.HashSetFactory;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class SafeStructuralSolverResult implements ISolverResult {
  Set<Message> messages = HashSetFactory.make();

  public Set<? extends Message> getMessages() {
    return messages;
  }

  public void addMessages(Set<? extends Message> messageSet) {
    messages.addAll(messageSet);
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    for (Iterator<? extends Message> it = messages.iterator(); it.hasNext();) {
      result.append(it.next().toString());
      result.append("\n");
    }
    return result.toString();
  }

}
