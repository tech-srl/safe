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
package com.ibm.safe.core.tests;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.TestCase;

import com.ibm.wala.util.ref.ReferenceCleanser;

/**
 * @author yahave
 * @author sfink
 * 
 */
public class SafeTCase extends TestCase {

  public static boolean ONLY_PRECOMMIT = false;

  protected static String getStackStrace(final Throwable exception) {
    final StringWriter strWriter = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(strWriter);
    exception.printStackTrace(printWriter);
    printWriter.close();
    return strWriter.toString();
  }

  protected static String getPackageName(final String className) {
    return className.substring(0, className.lastIndexOf('.') + 1);
  }

  public static boolean isPreCommit() {
    return ONLY_PRECOMMIT;
  }

  protected void tearDown() throws Exception {
    ReferenceCleanser.clearSoftCaches();
  }
}
