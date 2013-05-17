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
package j2se.typestate.fileComponent;

import j2se.typestate.accesspath.OpFileComponent;

/*********************************************************************
 * @author Eran Yahav (eyahav)
 * This testcase is supposed to exhibit an unbounded accesspath.
 * Expected Result: This testscase should create an unbounded access path.
 *********************************************************************/
public class FCExample14 {

  public static void main(String[] args) {
    OpFileComponent f1 = new OpFileComponent(); // OpFileComponent Site #1

    Item currentItem = new Item();
    currentItem.file = f1;

    Item anotherItem = new Item();
    anotherItem.next = currentItem;

    OpFileComponent theFile = anotherItem.next.file;

    theFile.open();
    theFile.read();
  }

  public static class Item {
    public Item next;

    public OpFileComponent file;
  }

}
