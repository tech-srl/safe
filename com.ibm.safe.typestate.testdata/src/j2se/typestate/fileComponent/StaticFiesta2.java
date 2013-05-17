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
 * Expected Result: This is a variant of StaticFiesta1 in which the calls
 * to the component() method are replaced by direct access to the public 
 * field "theComponent".
 * As a result, no error should be reported.
 * @author Eran Yahav (eyahav)
 *********************************************************************/
public class StaticFiesta2 {
  public static FileComponent st_x;

  public static FileComponentContainer st_c_x;

  public static void main(String[] args) {
    st_x = new FileComponent(); // FileComponent Site #1
    FileComponent f2 = new FileComponent(); // FileComponent Site #2
    FileComponentContainer c2;
    FileComponent f3;
    FileComponent f4;

    st_c_x = new FileComponentContainer(); // FileComponentContainer Site #1
    c2 = new FileComponentContainer(); // FileComponentContainer Site #2

    st_c_x.theComponent = st_x;
    c2.theComponent = f2;

    f3 = st_c_x.theComponent;
    f3.close();
    f4 = c2.theComponent;
    f4.read();
  }
}
