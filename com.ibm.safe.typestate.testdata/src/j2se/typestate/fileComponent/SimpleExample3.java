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
 * An extremely simple correct and incorrect usage of a file component. 
 * The file component allocated at *alloc1* is used incorrectly, the file component allocated 
 * at *alloc2* is used correctly. 
 * Expected Result: A true alarm should be reported for *alloc1*, no
 * alarm should be reported for *alloc2*. 
 * 
 * @author Eran Yahav (eyahav)
 ******************************************************************************/

public class SimpleExample3 {

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent(); // *alloc1*
    FileComponent f2 = new FileComponent(); // *alloc2*

    f1.close();
    f1.read();

    f2.read();
    f2.close();
  }

}
