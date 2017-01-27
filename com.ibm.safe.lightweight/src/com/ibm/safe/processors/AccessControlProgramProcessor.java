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
package com.ibm.safe.processors;

import java.util.Set;

import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.rules.StructuralRule;
import com.ibm.safe.secure.accessibility.AccessibilityAnalyzer;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.Predicate;

public class AccessControlProgramProcessor implements ProgramProcessor {

  /** underlying class hierarchy */
  protected IClassHierarchy classHierarchy;

  private final StructuralRule[] structuralRules;

  private final Predicate<IClass> classFilter;

  private Set<? extends Message> messages;

  public AccessControlProgramProcessor(IClassHierarchy classHierarchy, StructuralRule[] theStructuralRules,
      final Predicate<IClass> someClassFilter) {
    this.classHierarchy = classHierarchy;
    this.structuralRules = theStructuralRules;
    this.classFilter = someClassFilter;
  }

  public void process() {
    AccessibilityAnalyzer analyzer = new AccessibilityAnalyzer();
    messages = analyzer.process(classHierarchy, this.structuralRules, this.classFilter);
  }

  public void addClassProcessor(ClassProcessor cp) {
    throw new UnsupportedOperationException();
  }

  public Object getResult() {
    return messages;
  }
}
