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

/**
 * unique logic allows us to perform strong updates resulting in 0 false positives for APMust.
 * UniqueEscape alone is not enough, because without AP, the call to close looks polymorphic
 * because of loss of precision in the context-insensitive component() method.
 * @author Eran Yahav (eyahav)
 */
public class StaticFiesta1 {
  public static FileComponent st_x;

  public static FileComponentContainer st_c_x;

  public static void main(String[] args) {
    st_x = new FileComponent(); // FileComponent Site #1
    FileComponent f2 = new FileComponent(); // FileComponent Site #2

    st_c_x = new FileComponentContainer(); // FileComponentContainer Site #1
    FileComponentContainer c2 = new FileComponentContainer(); // FileComponentContainer
                                                              // Site #2

    st_c_x.theComponent = st_x;
    c2.theComponent = f2;

    FileComponent f3 = st_c_x.component();
    f3.close();
    FileComponent f4 = c2.component();
    f4.read();
  }
}
