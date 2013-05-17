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
package com.ibm.safe.internal.entrypoints;

public final class MainClassesEntryPointsReader implements IEntryPointsReader {

  public MainClassesEntryPointsReader(final String[] mainClasses) {
    this.entryPoints = new EntryPointDefinition[mainClasses.length];
    for (int i = 0; i < mainClasses.length; i++) {
      this.entryPoints[i] = new EntryPointDefinition(mainClasses[i], MAIN_METHOD_NAME, MAIN_METHOD_DESCRIPTOR);
    }
  }

  public EntryPointDefinition[] getEntryPointDefinitions() {
    return this.entryPoints;
  }

  private EntryPointDefinition[] entryPoints;

  static final String MAIN_METHOD_NAME = "main"; //$NON-NLS-1$

  static final String MAIN_METHOD_DESCRIPTOR = "([Ljava/lang/String;)V"; //$NON-NLS-1$

}
