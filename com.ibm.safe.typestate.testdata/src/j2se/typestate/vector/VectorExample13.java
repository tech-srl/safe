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
 * Should produce 1 false positive with base engine on EmptyVector rule
 * 
 * However, with strong updates, UnsoundEmptyVector rule should handle it.
 * 
 * @author Stephen Fink
 */
public final class VectorExample13 {

  public static void main(String[] args) {
    Vector v1 = new Vector();
    for (int i = 0; i < v1.size(); i++) {
      v1.get(i);
    }
  }
}