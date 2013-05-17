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
package com.ibm.safe.typestate.options;

import java.util.Map;

import com.ibm.wala.util.collections.HashMapFactory;

public class ModularSummaryKind {

  private final String summaryKind;

  private static Map<String, ModularSummaryKind> allKinds = HashMapFactory.make();

  public static final ModularSummaryKind BASE = createModularSummaryKind("Base");

  public static final ModularSummaryKind MUST = createModularSummaryKind("Must");

  public static final ModularSummaryKind PROP_BASE = createModularSummaryKind("PropBase");

  public static final ModularSummaryKind QUAD = createModularSummaryKind("Quad");

  public String toString() {
    return this.summaryKind;
  }

  private ModularSummaryKind(final String summaryKind) {
    this.summaryKind = summaryKind;
  }

  private static ModularSummaryKind createModularSummaryKind(final String sumKind) {
    ModularSummaryKind mmk = new ModularSummaryKind(sumKind);
    allKinds.put(sumKind, mmk);
    return mmk;
  }

  public static ModularSummaryKind getSummaryKindFromString(String kindString) {
    return allKinds.get(kindString);
  }

}
