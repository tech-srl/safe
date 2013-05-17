/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.options;

import java.io.File;
import java.io.FileFilter;

public final class ByteCodeFilter implements FileFilter {

  public boolean accept(final File file) {
    return (file.isDirectory() || looksLikeBytecode(file.getName()));
  }

  public static boolean looksLikeBytecode(String filename) {
    return filename.endsWith(CommonOptions.CLASS_EXT) || filename.endsWith(CommonOptions.JAR_EXT)
        || filename.endsWith(CommonOptions.EAR_EXT) || filename.endsWith(CommonOptions.WAR_EXT)
        || filename.endsWith(CommonOptions.RAR_EXT);
  }
}