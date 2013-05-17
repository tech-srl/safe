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
 * This demonstrates the 2 false positives for EmptyVector on BCEL. This is not
 * an aliaising limitation nor a path-sensitivity limitation.
 * 
 * With EmptyVectorUnsound, APMust should return 1 finding, a false positive.
 * APMustUnsound should also return 1 finding, showing that aliasing is not the
 * issue.
 * 
 * @author sfink
 */
public final class VectorExample28 {

  public static void main(String[] args) {
    Container c = new Container();
    c.v1 = new Vector();
    c.v2 = new Vector();
    if (args.length > 1) {
      c.add(new Object());
    }
    if (!c.isEmpty()) {
      c.get2();
    }
  }

  private static class Container {
    Vector v1;

    Vector v2;

    public boolean isEmpty() {
      return v1.isEmpty();
    }

    public Object get1() {
      return v1.get(0);
    }

    public Object get2() {
      return v2.get(0);
    }

    public void add(Object o) {
      v1.add(o);
      v2.add(o);
    }
  }
}