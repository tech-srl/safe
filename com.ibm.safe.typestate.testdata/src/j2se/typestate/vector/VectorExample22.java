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
 * @author sfink
 * 
 * Has no true error. AP should not be confused by the checkcast
 */
public final class VectorExample22 {

  public static void main(String[] args) {
    Vector v = new Vector();
    foo(v, v);
  }

  private static void foo(Object o, Vector v) {
    if (o instanceof Vector) {
      Vector vo = (Vector) o;
      vo.add(new Object());
      v.firstElement();
    }
  }
}