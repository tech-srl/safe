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

/*
 * Created on Aug 11, 2003
 */

/**
 * @author eyahav
 */
public class FileExample3 {
  static Random r = new Random();

  static int aNumber = r.nextInt();

  public static void main(String[] args) {
    FileComponent f = new FileComponent();

    while (aNumber > 0) {
      f.read();
      if (aNumber < 5) {
        f.close();
      }
      f = new FileComponent();
    }
  }
}
