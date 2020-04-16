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
package com.ibm.safe.internal.filtering;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import java.util.function.Predicate;

public final class OrFilter<T> implements Predicate<T> {

  public OrFilter() {
  }

  public OrFilter(final Predicate<T> leftFilter, final Predicate<T> rightFilter) {
    this.classFilterList.add(leftFilter);
    this.classFilterList.add(rightFilter);
  }

  // --- Interface methods implementation

  public boolean test(final T clazz) {
    for (Iterator<Predicate<T>> iter = this.classFilterList.iterator(); iter.hasNext();) {
      if (iter.next().test(clazz)) {
        return true;
      }
    }
    return false;
  }

  // --- Public services

  public void addFilter(final Predicate<T> classFilter) {
    this.classFilterList.add(classFilter);
  }

  // --- Private code

  private final List<Predicate<T>> classFilterList = new LinkedList<Predicate<T>>();

}
