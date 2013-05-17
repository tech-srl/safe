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
package j2se.typestate.accesspath.context_sensitivity;

import java.util.Iterator;

/**
 * This example do not produce a violation... unfortunately. next() will always
 * be indeed after a hasNext() execution, except that one will occur after a
 * hasNext() that failed ! So the code here is buggy but SAFE can't detect it.
 * 
 * @author egeay
 */
public final class ImprovedContextSensitivity2 {

  public static void main(final String[] args) {
    final ProjectRep projectRep = new ProjectRep();
    // ...
    final Iterator mainClassMethodsIter = projectRep.getMethodsOfMainClass();
    while (mainClassMethodsIter.hasNext()) {
      firstCall((String) mainClassMethodsIter.next());
    }
    // ...
    secondCall((String) mainClassMethodsIter.next());
  }

  private static void firstCall(final String element) {
    // Do something
  }

  private static void secondCall(final String element) {
    // Do something
  }

}
