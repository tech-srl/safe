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
package j2se.typestate.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author yahave
 */
public final class IteratorExample8 {

  public static void main(String[] args) {
    List l1 = new ArrayList();
    Iterator it1 = l1.iterator();
    Object item = it1.next();
    it1.hasNext();
  }
}
