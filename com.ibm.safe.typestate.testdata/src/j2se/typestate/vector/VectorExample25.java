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
 * A test of arrays. Has one true error.
 * 
 * AP engine should report 2 findings (1 false positive)
 */
public final class VectorExample25 {

  public static void main(String[] args) {
    Vector v1 = new Vector();
    Vector v2 = new Vector();
    Vector[] v = new Vector[2];
    v[0] = v1;
    v[1] = v2;
    v[0].add(new Object());
    v[0].firstElement();
    v[1].firstElement();
  }
}