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
import java.io.FileFilter;

/**
 * File filter that merely select only XMI files.
 * 
 * @author egeay
 */
final class BasicXMLFilter implements FileFilter {

  /**
   * Returns true if file is a directory or is an XMI file.
   */
  public boolean accept(final File file) {
    return (file.isDirectory() || file.getName().endsWith(".xml")); //$NON-NLS-1$
  }

}
