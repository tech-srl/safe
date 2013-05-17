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
 * Should produce 1 warning with Base TypeState engine and with access path
 * 
 * @author Stephen Fink
 */
public final class VectorExample2 {

  public static void main(String[] args) {
    try {
      Vector v1 = new Vector();
      Vector v2 = new Vector();
      v1.removeAllElements();
      v2.add(new Object());
      v1.firstElement();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}