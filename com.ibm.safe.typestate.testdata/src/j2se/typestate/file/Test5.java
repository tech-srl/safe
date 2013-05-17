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

public class Test5 {

  public FileHolder buildList(FileHolder h) {
    FileHolder x;
    FileHolder y = new FileHolder();

    x = h;

    for (int i = 0; i < 42; i++) {
      y = new FileHolder();
      y.holder = x;
      x = y;
    }
    return y;
  }

  public static void main(String[] args) {

    Test5 t = new Test5();

    FileHolder x;
    FileHolder y = new FileHolder();
    FileHolder z = new FileHolder();
    FileHolder w = new FileHolder();

    FileComponent f = new FileComponent();
    FileHolder h1 = new FileHolder();
    h1.file = f;

    y = t.buildList(h1);

    f = new FileComponent();
    FileHolder h2 = new FileHolder();
    h2.file = f;

    z = t.buildList(h2);

    f = new FileComponent();
    FileHolder h3 = new FileHolder();
    h3.file = f;

    w = t.buildList(h3);

    FileHolder href = z;
    FileComponent fref = z.file;
    while (fref == null && href != null) {
      href = href.holder;
      fref = href.file;
    }

    fref.close();

    href = w;
    fref = w.file;
    while (fref == null && href != null) {
      href = href.holder;
      fref = href.file;
    }

    fref.close();

    href = y;
    FileComponent fref2 = y.file;
    while (fref2 == null && href != null) {
      href = href.holder;
      fref2 = href.file;
    }

    fref2.read();

    // a use of fref so I can see both fref and fref2 on the same config
    FileComponent dummyRef = fref;

  }
}
