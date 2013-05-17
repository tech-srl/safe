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
package com.ibm.safe.utils;

import java.io.File;

/**
 * Visitor interface that is being called by
 * {@link com.ibm.safe.utils.DirectoryWalk} methods for a given directory walk.
 * 
 * @see com.ibm.safe.utils.DirectoryWalk
 * @author egeay
 */
public interface IDirectoryWalkVisitor {

  /**
   * Notifies that a directory has just been found during the walk.
   */
  public void visitDirectory(final File directory);

  /**
   * Notifies that a regular file has just been found during the walk.
   */
  public void visitFile(final File file);

}
