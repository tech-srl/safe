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
 * Created on Dec 23, 2004
 */
package com.ibm.safe.typestate.quad;

import java.util.Iterator;
import java.util.Set;

import com.ibm.wala.util.collections.HashSetFactory;

/**
 * @author Eran Yahav (yahave)
 * 
 * domain of the third element in a triplet (SJF: eventually this domain should
 * probably be a mutable mapping, to allow a bit-vector-based implementation)
 */
public class AuxiliaryDomain {

  /**
   * values (elements) of the auxiliary domain
   */
  private Set<Auxiliary> values = HashSetFactory.make();

  /**
   * what is the initial value of this auxiliary domain
   */
  private Auxiliary initialValue;

  public Iterator<Auxiliary> iterator() {
    return values.iterator();
  }

  public int size() {
    return values.size();
  }

  protected void addValue(Auxiliary value) {
    values.add(value);
  }

  /**
   * @return Returns the initialValue.
   */
  public Auxiliary getInitialValue() {
    return initialValue;
  }

  /**
   * @param initialValue
   *            The initialValue to set.
   */
  public void setInitialValue(Auxiliary initialValue) {
    this.initialValue = initialValue;
    addValue(initialValue);
  }
}
