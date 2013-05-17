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
package j2se.typestate.fileComponent;

/**
 * This small tweak on simple-example 3 exposes a problem with the AP engine and
 * its relation to IFDS summaries. While we'd expect to report only 1 error with
 * AP, we actually get 3 due to limitations in context-sensitive analysis of
 * foo.
 * 
 * @author eyahav
 * @author sfink
 * 
 */
public class SimpleExample4 {

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent();
    FileComponent f2 = new FileComponent();

    foo(f1);
    f1.read();

    f2.read();
    foo(f2);
  }

  public static void foo(FileComponent f) {
    f.close();
  }

}
