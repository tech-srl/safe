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

/**
 * FileList.java creating and processing a list of files.
 * Created on Aug 6, 2005
 * @author eyahav
 */
public class FileList {

  private SLL theList;

  public FileList(String[] args) {
    theList = FileList.create(args.length, args);
  }

  private static SLL create(int size, String[] args) {
    SLL newHead, head;
    int i;
    head = null;
    for (i = 0; i < size; i++) {
      newHead = new SLL();
      newHead.val = new FileComponent(args[i]);
      newHead.next = head;
      head = newHead;
    }
    return head;
  }

  public void process() {
    SLL curr = theList;
    while (curr != null) {
      FileComponent currFile = (FileComponent) curr.val;
      currFile.open();
      // do some processing
      processFile(currFile);
      currFile.close();
      curr = curr.next;
    }
  }

  public static void processFile(FileComponent f) {
    f.read();
    // do something
  }

}
