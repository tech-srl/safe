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
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * Util methods to walk through a given directory structure with the ability to
 * define filters related to this walk.
 * 
 * @author egeay
 */
public final class DirectoryWalk {

  /**
   * Walks through all a directory structure without any filtering. Notifies
   * files (regular files or directories) found via a visitor.
   * 
   * @param file
   *            The source file that is the root of this walk through. Can be a
   *            regular file. In that case, only that file is notified.
   * @param visitor
   *            A visitor instance that will be used to notify files visited.
   * @see IDirectoryWalkVisitor
   */
  public static void walk(final File file, final IDirectoryWalkVisitor visitor) {
    walk(file, new AlwaysTrueFileFilter(), visitor);
  }

  /**
   * Walks through all a directory structure with a {@link FileFilter} instance
   * to prune some files. Notifies files (regular files or directories)found via
   * a visitor.
   * 
   * @param file
   *            The source file that is the root of this walk through. Can be a
   *            regular file. In that case, only that file is notified.
   * @param fileFilter
   *            The filter that will be used to prune some files in the walk.
   * @param visitor
   *            A visitor instance that will be used to notify files visited.
   * @see IDirectoryWalkVisitor
   */
  public static void walk(final File file, final FileFilter fileFilter, final IDirectoryWalkVisitor visitor) {
    notifyVisitor(file, visitor);
    final File[] files = file.listFiles(fileFilter);
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          visitor.visitDirectory(files[i]);
          walk(files[i], fileFilter, visitor);
        } else {
          visitor.visitFile(files[i]);
        }
      }
    }
  }

  /**
   * Walks through all a directory structure with a {@link FilenameFilter}
   * instance to prune some files. Notifies files (regular files or
   * directories)found via a visitor.
   * 
   * @param file
   *            The source file that is the root of this walk through. Can be a
   *            regular file. In that case, only that file is notified.
   * @param fileFilter
   *            The filter that will be used to prune some files in the walk.
   * @param visitor
   *            A visitor instance that will be used to notify files visited.
   * @see IDirectoryWalkVisitor
   */
  public static void walk(final File file, final FilenameFilter fileFilter, final IDirectoryWalkVisitor visitor) {
    notifyVisitor(file, visitor);
    final File[] files = file.listFiles(fileFilter);
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        if (files[i].isDirectory()) {
          visitor.visitDirectory(files[i]);
          walk(files[i], fileFilter, visitor);
        } else {
          visitor.visitFile(files[i]);
        }
      }
    }
  }

  private static void notifyVisitor(final File file, final IDirectoryWalkVisitor visitor) {
    if (file.isDirectory()) {
      visitor.visitDirectory(file);
    } else {
      visitor.visitFile(file);
    }
  }

  private static class AlwaysTrueFileFilter implements FileFilter {

    public boolean accept(final File file) {
      return true;
    }

  }

  private DirectoryWalk() {
  }

}
