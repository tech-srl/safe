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
import java.net.URL;

import org.eclipse.osgi.util.NLS;

public final class SafeHome {

  public static final String URL_RESOLUTION_ERROR = "Unable to resolve URL ";

  public static String getSafeHomeDir(final ClassLoader classLoader, final boolean shouldDisplayPropertiesFileLoaded) {
    final String envProperty = System.getProperty("SAFE_HOME"); //$NON-NLS-1$
    if (envProperty != null) {
      return envProperty;
    }

    URL url = classLoader.getResource("safe.properties"); //$NON-NLS-1$'

    if (url == null) {
      return System.getProperty(USER_DIR);
    } else {
      final String file = SafeEclipseUtils.getFileFromURL(url);
      if (file == null) {
        return System.getProperty(USER_DIR);
      }
      if (shouldDisplayPropertiesFileLoaded) {
        System.out.println(NLS.bind("Loaded properties file ''{0}''.", file));
      }
      return new File(file).getParentFile().getParentFile().getPath();
    }
  }

  public static String getSafeHomeDir(final ClassLoader classLoader) {
    return getSafeHomeDir(classLoader, false /* shouldDisplayPropertiesFileLoaded */);
  }

  // --- Private code

  private static final String USER_DIR = "user.dir"; //$NON-NLS-1$

}
