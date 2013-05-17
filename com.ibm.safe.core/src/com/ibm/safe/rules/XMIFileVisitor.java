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
package com.ibm.safe.rules;

import java.io.File;
import java.util.Collection;
import java.util.Stack;

import com.ibm.safe.utils.IDirectoryWalkVisitor;

final class XMIFileVisitor implements IDirectoryWalkVisitor {
  public void visitDirectory(final File directory) {
  }

  public void visitFile(final File file) {
    this.xmiFiles.add(file);
  }

  public File[] getXMIFiles() {
    return (File[]) this.xmiFiles.toArray(new File[this.xmiFiles.size()]);
  }

  private Collection<File> xmiFiles = new Stack<File>();

}