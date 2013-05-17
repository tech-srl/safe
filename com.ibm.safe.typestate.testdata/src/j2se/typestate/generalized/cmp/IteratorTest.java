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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Illustrates use of Comparator for ignoring case distinctions.
 */
class IgnoreCase implements Comparator {
  public int compare(Object obj1, Object obj2) {
    String s1 = (String) obj1;
    String s2 = (String) obj2;

    return s1.compareToIgnoreCase(s2);
  }
}

/**
 * Test program for lots of collections.
 */
class IteratorTest {
  /**
   * Print any collection.
   */
  public static void printCollection(Collection c) {
    Iterator itr = c.iterator();

    while (itr.hasNext())
      System.out.print(itr.next() + " ");
    System.out.println();
  }

  public static void main(String[] args) {
    List l1 = new ArrayList();
    l1.add("Jack");
    l1.add("Jill");
    l1.add("Bill");

    List l2 = new LinkedList(l1);
    Set s1 = new TreeSet(l1);
    Set s2 = new HashSet(l1);
    Set s3 = new TreeSet(Collections.reverseOrder());
    Set s4 = new TreeSet(new IgnoreCase());

    s3.add("joe");
    s3.add("bob");
    s3.add("hal");

    s4.add("Jeb!");
    s4.add("jill");
    s4.add("jack");

    printCollection(l1); // Jack Jill Bill
    printCollection(l2); // Jack Jill Bill
    printCollection(s1); // Bill Jack Jill
    printCollection(s2); // Some unspecified order
    printCollection(s3); // joe hal bob
    printCollection(s4); // jack Jeb! jill

    List stud1 = new ArrayList();
    stud1.add(new SimpleStudent("Bob", 0));
    stud1.add(new SimpleStudent("Joe", 1));
    stud1.add(new SimpleStudent("Bob", 2)); // duplicate

    Set stud2 = new TreeSet(stud1); // will only have 2 items
    Set stud3 = new HashSet(stud1); // will only have 2 items, if hashCode is
    // implemented. Otherwise will have 3
    // because duplicate will not be detected.
    printCollection(stud1); // Bob Joe Bob
    printCollection(stud2); // Bob Joe
    printCollection(stud3); // Two items in unspecified order
  }
}

/**
 * Illustrates use of hashCode/equals for a user-defined class. Also illustrates
 * the compareTo function. Students are ordered on basis of name only.
 */
class SimpleStudent implements Comparable {
  String name;

  int id;

  public SimpleStudent(String n, int i) {
    name = n;
    id = i;
  }

  public String toString() {
    return name + " " + id;
  }

  public boolean equals(Object rhs) {
    if (rhs == null || getClass() != rhs.getClass())
      return false;

    SimpleStudent other = (SimpleStudent) rhs;
    return name.equals(other.name);
  }

  public int compareTo(Object other) {
    return name.compareTo(((SimpleStudent) other).name);
  }

  public int hashCode() {
    return name.hashCode();
  }
}
