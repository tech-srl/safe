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

/**
 * Test7 --- simpler than Test5
 * @author yahave
 */
public class Test7 {

  public FileComponent getFile() {
    FileComponent result;
    result = new FileComponent();
    return result;
  }

  public FileHolder getFileHolder(FileComponent aFile) {
    FileHolder result;
    result = new FileHolder();
    result.file = aFile;
    return result;
  }

  public static void main(String[] args) {

    Test7 t = new Test7();

    FileComponent f;
    FileComponent f1;
    FileComponent f2;
    FileComponent f3;
    FileHolder x;
    FileHolder y;
    FileHolder z;

    f = t.getFile();
    x = t.getFileHolder(f);

    f = t.getFile();
    y = t.getFileHolder(f);

    f = t.getFile();
    z = t.getFileHolder(f);

    f1 = x.file;
    f2 = y.file;

    f1.close();
    f2.read();

    f3 = z.file;
    f3.read();
  }
}
