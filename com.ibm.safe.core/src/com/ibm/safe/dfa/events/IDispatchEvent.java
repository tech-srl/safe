/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.dfa.events;


/**
 * An event as defined in {@link com.ibm.safe.typestate.rules.IEvent} but with a
 * generalization of its name.
 */
public interface IDispatchEvent extends IEvent {

  /**
   * Returns the regular expression that generalizes event name.
   */
  String getPattern();

  void setPattern(String value);

}
