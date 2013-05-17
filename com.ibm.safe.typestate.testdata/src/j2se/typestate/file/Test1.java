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
 * 
 * @author yahave
 *
 */
public class Test1 {

  public static void main(String[] args) {

    FileHolder x = new FileHolder();
    FileHolder y = new FileHolder();
    FileHolder z = new FileHolder();

    FileComponent f = new FileComponent();
    x.file = f;
    f = new FileComponent();
    y.file = f;
    f = new FileComponent();
    z.file = f;

    FileComponent f1 = x.file;
    FileComponent f2 = y.file;

    f1.close();
    f2.read();
  }
}
