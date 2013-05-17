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
package j2se.typestate.vector;

import java.util.Vector;

/**
 * Has one real error. Tests a slicing-related bug discovered by noam.
 * 
 * @author sfink
 */
public final class VectorExample30 {

  static Vector v;

  public static void main(String[] args) {
    Vector x = new Vector();
    v = x;
    foo();
    v.firstElement();
  }

  public static void foo() {
  }
}