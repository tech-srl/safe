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
package j2se.typestate.file;

/* 
 * Created on Aug 11, 2003
 */

import java.util.Random;

/**
 * @author eyahav
 */
public class FileExample4b {
  static Random r = new Random();

  static int aNumber = r.nextInt();

  public static void main(String[] args) {

    // create a list of file components
    SLL newHead, head;
    int i;
    int size = args.length;
    head = null;
    for (i = 0; i < size; i++) {
      newHead = new SLL();
      newHead.val = new FileComponent(args[i]);
      newHead.next = head;
      head = newHead;
    }

    // now add a new file
    newHead = new SLL();
    newHead.val = new FileComponent();
    newHead.next = head;
    head = newHead;

    // close all files in the list
    SLL curr = head;
    while (curr != null) {
      FileComponent currFile = (FileComponent) curr.val;
      currFile.close();
      curr = curr.next;
    }

    FileComponent f = head.val;
    f.read();
    f.close();
  }
}
