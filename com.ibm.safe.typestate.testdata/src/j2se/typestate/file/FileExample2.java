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
 * There exists an error path in which f3 is closed and read is invoked.
 * 
 * @author eyahav
 */
public class FileExample2 {
  static Random r = new Random();

  static int aNumber = r.nextInt();

  public static void main(String[] args) {
    FileComponent f1 = new FileComponent();
    FileComponent f2 = new FileComponent();
    FileComponent f3;

    f3 = f2;

    if (aNumber == 42) {
      f2.close();
      f3 = f1;
    } else {
      f2.close();
    }

    f3.read();
  }
}
