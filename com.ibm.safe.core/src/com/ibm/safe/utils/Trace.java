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

import java.util.Collection;
import java.util.logging.Logger;

public class Trace {

  public static void println(String string) {
    Logger.getGlobal().fine(string);
  }

  public static void print(String string) {
	Logger.getGlobal().fine(string);
  }

  public static void setTraceFile(String stringValue) {
    // TODO Auto-generated method stub
    
  }

  public static void println(Object s) {
    Logger.getGlobal().fine(s != null ? s.toString() : "null");
  }

  public static void printCollection(String string, Collection<? extends Object> c) {
    Logger.getGlobal().fine(string + ": " + c);
  }

  public static Object getTraceFile() {
    // TODO Auto-generated method stub
    return null;
  }

}
