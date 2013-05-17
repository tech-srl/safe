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
 * Name: SimpleExample1.java
 * Description: An extremely simple incorrect usage of a file component.
 * the file is closed and then read from, which is illegal.
 * Expected Result: this case should report a true alarm.
 * Author: Eran Yahav (eyahav)
 *********************************************************************/
public class SimpleExample1 {

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent();
    f1.close();
    f1.read();
  }
}
