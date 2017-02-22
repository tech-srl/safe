/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.dfa;

import com.ibm.safe.dfa.events.IDispatchEvent;

/**
 * @author yahave
 */
public interface IDFATransition {
  public String getDestination();

  void setDestination(String value);
  
  public default void setDestination(IDFAState state) {
	  this.setDestination(state.getName());
  }

  public String getEvent();

  void setEvent(String value);

  public default void setEvent(IDispatchEvent event) {
	  this.setEvent(event.getName());
  }

  public String getSource();

  void setSource(String value);
  
  public default void setSource(IDFAState state) {
	 this.setSource(state.getName()); 
  }
  
}
