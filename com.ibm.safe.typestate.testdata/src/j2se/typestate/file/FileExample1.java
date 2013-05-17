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

import java.util.Random;

/**
 * FileExample1\FileExample1.java
 * 
 * Demonstrate the importance of combined pointer and typestate analysis (a la
 * SAS'03)
 * 
 * @author eyahav
 */
public class FileExample1 {
  static Random r = new Random();

  static int aNumber = r.nextInt();

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent(); /* o1 */
    FileComponent f2 = new FileComponent(); /* o2 */
    FileComponent f3;

    f3 = f2;

    if (aNumber == 42) {
      f2.close(); /* o2 gets closed */
      f3 = f1;
    }

    f3.read();
  }
}
