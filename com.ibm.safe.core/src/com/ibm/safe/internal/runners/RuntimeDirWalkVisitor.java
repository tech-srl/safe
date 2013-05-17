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
package com.ibm.safe.internal.runners;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.jar.JarFile;

import com.ibm.safe.utils.IDirectoryWalkVisitor;
import com.ibm.safe.utils.SafeLogger;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.classLoader.Module;

final public class RuntimeDirWalkVisitor implements IDirectoryWalkVisitor {

  public RuntimeDirWalkVisitor(final Collection<Module> jarFileCollection) {
    this.jarFiles = jarFileCollection;
  }

  public void visitDirectory(final File directory) {
    // Do nothing in that case !
  }

  public void visitFile(final File file) {
    try {
      this.jarFiles.add(new JarFileModule(new JarFile(file)));
    } catch (IOException except) {
      SafeLogger.severe("Unable to add jar file from runtime directory specified.", except);
    }
  }

  // --- Private code

  private final Collection<Module> jarFiles;

}
