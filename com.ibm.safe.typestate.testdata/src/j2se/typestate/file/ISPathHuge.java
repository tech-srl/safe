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

import java.io.FileInputStream;
import java.util.Random;

/**
 * A ESP-like path combination of InputStreams
 * 
 * @author eyahav
 */
public class ISPathHuge {

  static Random r = new Random();

  static int aNumber = r.nextInt();

  public static void main(String[] args) {

    boolean p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, flag;

    try {

      FileInputStream f1 = new FileInputStream("test.file");
      if (aNumber >= 41) {
        p1 = true;
        f1.close();
      } else {
        p1 = false;
      }

      FileInputStream f2 = new FileInputStream("test.file");
      if (aNumber >= 42) {
        p2 = true;
        f2.close();
      } else {
        p2 = false;
      }

      FileInputStream f3 = new FileInputStream("test.file");
      if (aNumber >= 43) {
        p3 = true;
        f3.close();
      } else {
        p3 = false;
      }

      FileInputStream f4 = new FileInputStream("test.file");
      if (aNumber >= 44) {
        p4 = true;
        f4.close();
      } else {
        p4 = false;
      }

      FileInputStream f5 = new FileInputStream("test.file");
      if (aNumber >= 45) {
        p5 = true;
        f5.close();
      } else {
        p5 = false;
      }

      FileInputStream f6 = new FileInputStream("test.file");
      if (aNumber >= 46) {
        p6 = true;
        f6.close();
      } else {
        p6 = false;
      }

      FileInputStream f7 = new FileInputStream("test.file");
      if (aNumber >= 46) {
        p7 = true;
        f7.close();
      } else {
        p7 = false;
      }

      FileInputStream f8 = new FileInputStream("test.file");
      if (aNumber >= 46) {
        p8 = true;
        f8.close();
      } else {
        p8 = false;
      }

      FileInputStream f9 = new FileInputStream("test.file");
      if (aNumber >= 46) {
        p9 = true;
        f9.close();
      } else {
        p9 = false;
      }

      FileInputStream f10 = new FileInputStream("test.file");
      if (aNumber >= 46) {
        p10 = true;
        f10.close();
      } else {
        p10 = false;
      }

      FileInputStream r = new FileInputStream("test.file");

      if (aNumber >= 45) {
        flag = true;
        r.close();
      } else {
        flag = false;
      }

      if (!flag) {
        r.read();
      }
    } catch (Exception e) {
      System.out.println("Error handling files");
    }
  }
}
