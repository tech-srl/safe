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

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;


public class SafeEclipseUtils {
  public static String getFileFromURL(final URL url) {
    if (BUNDLE_RESOURCE.equals(url.getProtocol())) {
      try {
        return FileLocator.resolve(url).getFile();
      } catch (IOException except) {
        SafeLogger.severe(SafeHome.URL_RESOLUTION_ERROR + url);
        return null;
      }
    } else {
      return url.getPath();
    }
  }

  private static final String BUNDLE_RESOURCE = "bundleresource"; //$NON-NLS-1$

}
