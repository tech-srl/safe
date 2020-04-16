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

import java.util.function.Predicate;

public final class AlwaysTrueClassFilter<T> implements Predicate<T> {

  // --- Interface methods implementation

  public boolean test(final T clazz) {
    return true;
  }

}
