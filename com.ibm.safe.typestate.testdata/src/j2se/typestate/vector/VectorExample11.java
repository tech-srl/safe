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
 * Should produce 3 true warnings with any engine
 * 
 * @author Stephen Fink
 */
public final class VectorExample11 {

  public static void main(String[] args) {
    try {
      Vector v1 = new Vector();
      v1.firstElement();

      Vector v2 = new Vector(v1);
      v2.firstElement();

      Vector v3 = new Vector(0);
      v3.firstElement();

      Vector v4 = new Vector(0, 1);
      v4.firstElement();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}