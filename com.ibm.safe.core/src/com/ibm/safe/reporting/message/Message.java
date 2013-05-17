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

import com.ibm.safe.rules.IRule;

/**
 * General interface for SAFE warning messages.
 * Created on Dec 16, 2004
 * @author Eran Yahav (yahave)
 */
public interface Message {

  public IRule getRule();

  public Location getLocation();

  public String getMessageType();

  public String getText();
}