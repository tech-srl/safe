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

public class SafeAssertions {

  public static final boolean verifyAssertions = false;

  public static void _assert(boolean c, String s) {
    assert c : s;
  }

  public static void _assert(boolean c) {
    assert c;
  }
}
