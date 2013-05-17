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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

class KernelBenchmark5 {
  static Random r = new Random();

  static int anumber = r.nextInt();

  private Worklist worklist;

  public static void main(String[] args) {
    KernelBenchmark5 m = new KernelBenchmark5();
    m.initializeWorklist(args);
    m.processWorklist();
  }

  void initializeWorklist(String[] args) {
    worklist = new Worklist();
    worklist.addItem("aha");
    worklist.addItem("oho");
  }

  void processWorklist() {
    Set s = worklist.unprocessedItems();
    for (Iterator i = s.iterator(); i.hasNext();) {
      Object item = i.next(); // CME may occur here
      // if (anumber == 143)
      processItem(item);
    }
  }

  void processItem(Object item) {
    foo();
  }

  void foo() {
    worklist.addItem("str");
  }
}

class Worklist {
  Set s;

  public Worklist() {
    s = new HashSet();
  }

  public void addItem(Object item) {
    s.add(item);
  }

  public Set unprocessedItems() {
    return s;
  }
}
