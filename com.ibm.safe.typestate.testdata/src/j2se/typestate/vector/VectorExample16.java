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
 * Has a true error.
 * 
 * @author Stephen Fink
 */
public final class VectorExample16 {

  public static void main(String[] args) {
    Vector v1 = new Vector();
    try {
      throw new VectorException(v1);
    } catch (VectorException e) {
      e.v.get(1);
    }
  }

  private static class VectorException extends Exception {
    private Vector v;

    VectorException(Vector v) {
      this.v = v;
    }
  }
}