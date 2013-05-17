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

/******************************************************************************* 
 * Description: An extremely simple correct usage of a file component. 
 * Expected Result: this case should not report any false alarms. 
 * @author Eran Yahav (eyahav)
 ******************************************************************************/
public class SimpleExample2 {

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent();
    f1.read();
    f1.close();
  }

}
