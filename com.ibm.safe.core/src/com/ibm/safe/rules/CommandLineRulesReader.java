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
import java.io.IOException;

import com.ibm.safe.utils.SafeHome;
import com.ibm.safe.utils.SafeLogger;

public final class CommandLineRulesReader extends AbstractRulesReader implements IRulesReader {

  public final static String NO_EXISTING_DIR = "No existing directory to load the rules: ";

  public final static String NO_PROPER_DIRS = "Unable to get access to any valid directory to load SAFE rules.";

  private final String[] rulesDirs;

  public CommandLineRulesReader(final String[] rulesDirectories) {
    this.rulesDirs = rulesDirectories;
  }

  public void load(ClassLoader classLoader) throws IOException {
    boolean oneExistingDir = false;
    for (int i = 0; i < this.rulesDirs.length; i++) {
      final File directory = getDirectoryFile(classLoader, this.rulesDirs[i]);
      if (directory != null) {
        oneExistingDir = true;
        preDirectoryTraversal(directory);
      }
    }
    if (!oneExistingDir) {
      SafeLogger.severe(NO_PROPER_DIRS);
    }
  }

  private File getDirectoryFile(final ClassLoader classLoader, final String dirName) {
    File dir = new File(dirName);
    if (!dir.isAbsolute()) {
      dir = new File(SafeHome.getSafeHomeDir(classLoader), dirName);
    }
    if (!dir.exists()) {
      SafeLogger.warning(NO_EXISTING_DIR + dirName);
      return null;
    }
    return dir;
  }

}
