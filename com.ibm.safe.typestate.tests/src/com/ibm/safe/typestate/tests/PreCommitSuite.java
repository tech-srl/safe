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
package com.ibm.safe.typestate.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.ibm.safe.core.tests.SafeTCase;
import com.ibm.safe.j2se.typestate.FileComponentTest;
import com.ibm.safe.j2se.typestate.InputStreamTest;
import com.ibm.safe.j2se.typestate.IteratorTest;
import com.ibm.safe.j2se.typestate.OutputStreamTest;
import com.ibm.safe.j2se.typestate.PrintStreamTest;
import com.ibm.safe.j2se.typestate.SocketChannelTest;
import com.ibm.safe.j2se.typestate.StackTest;
import com.ibm.safe.j2se.typestate.URLConnectionTest;
import com.ibm.safe.j2se.typestate.VectorTest;

public final class PreCommitSuite {

  public static Test suite() {
    final TestSuite suite = new TestSuite();

    SafeTCase.ONLY_PRECOMMIT = true;

    suite.addTestSuite(OutputStreamTest.class);
    suite.addTestSuite(IteratorTest.class);
    suite.addTestSuite(FileComponentTest.class);
    suite.addTestSuite(VectorTest.class);
    suite.addTestSuite(InputStreamTest.class);
    suite.addTestSuite(StackTest.class);
    suite.addTestSuite(PrintStreamTest.class);
    suite.addTestSuite(SocketChannelTest.class);
    suite.addTestSuite(URLConnectionTest.class);

    return suite;
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

}
