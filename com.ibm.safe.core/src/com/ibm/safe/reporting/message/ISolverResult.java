/*******************************************************************************
 * Copyright (c) 2002-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.reporting.message;

import java.util.Set;

/**
 * A result of a SafeSolver.
 * 
 * @author Eran Yahav (yahave)
 */
public interface ISolverResult {
  /**
   * messages contained in the result.
   * 
   * @return messages contained in the result.
   */
  public abstract Set<? extends Message> getMessages();

  public abstract void addMessages(Set<? extends Message> messages);

}