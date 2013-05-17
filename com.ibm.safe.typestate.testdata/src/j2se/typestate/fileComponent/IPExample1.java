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

/*********************************************************************
 * Description: A simple incorrect usage of a file component.
 * the file is closed and then read from, which is illegal.
 * Expected Result: this case should report a true alarm.
 * @author Eran Yahav (eyahav)
 *********************************************************************/
public class IPExample1 {

  public IPExample1() {
  }

  public static void main(String[] args) {
    IPExample1 example = new IPExample1();

    FileComponent f1 = new FileComponent();
    example.wrappedClose(f1);
    example.wrappedRead(f1);
  }

  public void wrappedClose(FileComponent c) {
    c.close();
  }

  public void wrappedRead(FileComponent c) {
    c.read();
  }

}
