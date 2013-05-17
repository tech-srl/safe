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
 * Has one true error.
 * 
 * Designed to test multiple call sites for a single return site.
 * 
 * See SAFE bug 45653, which would cause AP engine to fail this test.
 * 
 * @author Stephen Fink
 */
public final class VectorExample18 {

  public static void main(String[] args) {
    Vector v1 = new Vector();
    try {
      if (args.length > 0) {
        v1 = foo(v1);
      } else {
        v1 = foo(v1);
      }
    } catch (Throwable t) {
      // this is a return site with 2 call sites.
      v1.get(0);
    }
  }

  public static Vector foo(Vector v) {
    v.capacity(); // force exceptional exit to be realizable; we're too stupid
    // to realize this statement won't throw an exception.
    return v;
  }
}