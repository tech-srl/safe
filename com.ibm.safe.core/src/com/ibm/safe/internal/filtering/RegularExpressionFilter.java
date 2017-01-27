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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.util.Predicate;

public final class RegularExpressionFilter<T> extends Predicate<T> {

  public RegularExpressionFilter(final String regularExpression) {
    this.pattern = Pattern.compile(regularExpression);
  }

  // --- Interface methods implementation

  public boolean test(final T clazz) {
    IClass klass = (IClass) clazz;
    final Matcher matcher = this.pattern.matcher(getRegularName(klass.getName().toString()));
    return matcher.matches();
  }

  // --- Private code

  private String getRegularName(final String className) {
    return className.substring(1).replace('/', '.');
  }

  final Pattern pattern;

}
