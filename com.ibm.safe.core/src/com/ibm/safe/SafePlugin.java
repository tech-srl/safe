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
package com.ibm.safe;

import org.eclipse.core.runtime.Plugin;

/**
 * The main plugin class to be used in this plugin.
 */
public class SafePlugin extends Plugin {

  /**
   * Initializes the default static plugin instance.
   */
  public SafePlugin() {
    SafePlugin.plugin = this;
  }

  /**
   * Returns the shared instance.
   */
  public static SafePlugin getDefault() {
    return plugin;
  }

  // --- Private code

  /**
   * The shared plugin instance.
   */
  private static SafePlugin plugin;
}
