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
package j2se.typestate.file;

// Test6b.java --- simpler than Test5

public class Test6b {

  public FileComponent getFile() {
    FileComponent result;
    result = new FileComponent();
    return result;
  }

  public static void main(String[] args) {

    Test6b t = new Test6b();

    FileComponent f;
    FileComponent f1;
    FileComponent f2;

    f = t.getFile();
    FileHolder x = new FileHolder();
    x.file = f;

    // bug -- don't get new f -- f = t.getFile();
    FileHolder y = new FileHolder();
    y.file = f;

    f = t.getFile();
    FileHolder z = new FileHolder();
    z.file = f;

    f1 = x.file;
    f2 = y.file;

    f1.close();
    f2.read();
  }
}
