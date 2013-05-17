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

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class used as entry point for every SAFE logging activities.
 * 
 * @author egeay
 */
public final class SafeLogger {

  /**
   * Log a message with an information severity level.
   */
  public static void info(final String infoMessage) {
    Logger.getLogger("").log(Level.INFO, infoMessage); //$NON-NLS-1$
  }

  /**
   * Log a parameterized message with an information severity level.
   */
  public static void info(final String infoMessage, final Object pararemeter) {
    Logger.getLogger("").log(Level.INFO, infoMessage, pararemeter); //$NON-NLS-1$
  }

  /**
   * Log a parameterized message with an information severity level.
   */
  public static void info(final String infoMessage, final Object[] pararemeters) {
    Logger.getLogger("").log(Level.INFO, infoMessage, pararemeters); //$NON-NLS-1$
  }

  /**
   * Log a message with an information severity level, thus the exception trace
   * related to.
   */
  public static void info(final String infoMessage, final Throwable exception) {
    Logger.getLogger("").log(Level.INFO, infoMessage, exception); //$NON-NLS-1$
  }

  /**
   * Log a message with a severe severity level.
   */
  public static void severe(final String severeMessage) {
    Logger.getLogger("").log(Level.SEVERE, severeMessage); //$NON-NLS-1$
  }

  /**
   * Log a parameterized message with a severe severity level.
   */
  public static void severe(final String severeMessage, final Object parameter) {
    Logger.getLogger("").log(Level.SEVERE, severeMessage, parameter); //$NON-NLS-1$
  }

  /**
   * Log a parameterized message with a severe severity level.
   */
  public static void severe(final String severeMessage, final Object[] parameters) {
    Logger.getLogger("").log(Level.SEVERE, severeMessage, parameters); //$NON-NLS-1$
  }

  /**
   * Log a message with a severe severity level, thus the exception trace
   * related to.
   */
  public static void severe(final String severeMessage, final Throwable exception) {
    Logger.getLogger("").log(Level.SEVERE, severeMessage, exception); //$NON-NLS-1$
  }

  /**
   * Log a message with a warning severity level.
   */
  public static void warning(final String warningMessage) {
    Logger.getLogger("").log(Level.WARNING, warningMessage); //$NON-NLS-1$
  }

  /**
   * Log a parameterized message with a warning severity level.
   */
  public static void warning(final String warningMessage, final Object parameter) {
    Logger.getLogger("").log(Level.WARNING, warningMessage, parameter); //$NON-NLS-1$
  }

  /**
   * Log a parameterized message with a warning severity level.
   */
  public static void warning(final String warningMessage, final Object[] parameters) {
    Logger.getLogger("").log(Level.WARNING, warningMessage, parameters); //$NON-NLS-1$
  }

  /**
   * Log a message with a warning severity level, thus the exception trace
   * related to.
   */
  public static void warning(final String warningMessage, final Throwable exception) {
    Logger.getLogger("").log(Level.WARNING, warningMessage, exception); //$NON-NLS-1$
  }

  /**
   * log a message on entering a method
   * 
   * @param sourceClass -
   *            method class
   * @param sourceMethod -
   *            method signature
   */
  public static void entering(String sourceClass, String sourceMethod) {
    Logger.getLogger("").entering(sourceClass, sourceMethod);
  }

  // --- Private code

  private SafeLogger() {
    Logger.getLogger("").setUseParentHandlers(false); //$NON-NLS-1$
    final Handler[] handlers = Logger.getLogger("").getHandlers(); //$NON-NLS-1$
    if ((handlers.length == 1) && (handlers[0] instanceof ConsoleHandler)) {
      ((ConsoleHandler) handlers[0]).setLevel(Level.WARNING);
    }
  }

}
