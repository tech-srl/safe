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
 * Should produce 1 false positive with Base TypeState engine, due to weak
 * update on s2.
 * 
 * @author Stephen Fink
 * @author Eran Yahav
 */
public final class StackExample3 {

  public static void main(String[] args) {
    try {
      // Stack s1 = new Stack();
      Stack s2 = new Stack();
      // s1.removeAllElements();
      s2.push(new Object());
      s2.peek();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}