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
 * last-alloc + RRO does the trick 
 * @author yahave
 */
public class Test2 {

  public static void main(String[] args) {

    FileComponent f = new FileComponent();
    FileHolder x = new FileHolder();
    x.file = f;

    f = new FileComponent();
    FileHolder y = new FileHolder();
    y.file = f;

    f = new FileComponent();
    FileHolder z = new FileHolder();
    z.file = f;

    FileComponent f1 = x.file;
    FileComponent f2 = y.file;

    f1.close();
    f2.read();
  }
}
