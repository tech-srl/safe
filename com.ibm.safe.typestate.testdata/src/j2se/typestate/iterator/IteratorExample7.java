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
import java.util.Collection;
import java.util.Iterator;

/**
 * @author yahave
 */
public final class IteratorExample7 {

  public void dummy(int value) {
    final Collection elements = getElements();
    for (Iterator iter = elements.iterator(); iter.hasNext();) {
      firstCall((String) iter.next());
      // ...
      secondCall((String) iter.next());
    }
  }

  private static Collection getElements() {
    return new ArrayList(10);
  }

  private static void firstCall(final String element) {
    // Do something
  }

  private static void secondCall(final String element) {
    // Do something
  }

}
