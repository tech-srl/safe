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

import java.util.ArrayList;
import java.util.Collection;

public final class StringEntryPointsReader implements IEntryPointsReader {

  public StringEntryPointsReader(final String entryPointsOptionValue) {
    createEntryPoints(entryPointsOptionValue.split(separatorChar));
  }

  public EntryPointDefinition[] getEntryPointDefinitions() {
    return this.entryPoints.toArray(new EntryPointDefinition[this.entryPoints.size()]);
  }

  private void createEntryPoints(final String[] entryPointStrings) {
    this.entryPoints = new ArrayList<EntryPointDefinition>(entryPointStrings.length);
    for (int i = 0; i < entryPointStrings.length; i++) {
      final int openingParenthesisIndex = entryPointStrings[i].indexOf('(');
      final String firstSegment = entryPointStrings[i].substring(0, openingParenthesisIndex).replace('.', '/');
      final int lastSlashIndex = firstSegment.lastIndexOf('/');

      this.entryPoints.add(new EntryPointDefinition(firstSegment.substring(0, lastSlashIndex), firstSegment
          .substring(lastSlashIndex + 1), entryPointStrings[i].substring(openingParenthesisIndex)));
    }
  }

  private Collection<EntryPointDefinition> entryPoints;

  private static final String separatorChar = ","; //$NON-NLS-1$

}
