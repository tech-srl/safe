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
package j2se.typestate.stack;

import java.util.Stack;

/**
 * Should produce 1 true warning with any TypeState engine.
 * 
 * @author Stephen Fink
 * @author Eran Yahav
 */
public final class StackExample1 {

  public static void main(String[] args) {
    try {
      Stack s = new Stack();
      s.pop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}