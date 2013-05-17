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
package j2se.typestate.generalized.cmp;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MapDemo {
  public static void printMap(String msg, Map m) {
    System.out.println(msg + ":");
    Set entries = m.entrySet();
    Iterator itr = entries.iterator();

    while (itr.hasNext()) {
      Map.Entry thisPair = (Map.Entry) itr.next();
      System.out.print(thisPair.getKey() + ": ");
      System.out.println(thisPair.getValue());
    }
  }

  // Do some inserts and printing (done in printMap).
  public static void main(String[] args) {
    Map phone1 = new TreeMap();

    phone1.put("John Doe", "212-555-1212");
    phone1.put("Jane Doe", "312-555-1212");
    phone1.put("Holly Doe", "213-555-1212");

    System.out.println("phone1.get(\"Jane Doe\"): " + phone1.get("Jane Doe"));
    System.out.println();

    printMap("phone1", phone1);
  }
}
