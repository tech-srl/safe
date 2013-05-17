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
/*******************************************************************************
 * Name: APDoubleClose2.java 
 * Author: Eran Yahav (eyahav)
 ******************************************************************************/

package j2se.typestate.accesspath;

import j2se.typestate.fileComponent.FileComponent;

public class APDoubleClose2 {

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent(); // FileComponent Site #1
    FileComponent f2 = new FileComponent(); // FileComponent Site #2
    FileComponent x;
    x = f2;
    x.close();
    x.close();
  }
}
