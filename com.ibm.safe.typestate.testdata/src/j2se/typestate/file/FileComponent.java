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

/*
 * Created on Aug 6, 2003
 */

/**
 * @author eyahav
 */
public class FileComponent {
  protected String fileName;

  protected boolean isOpen;

  public FileComponent() {
    this.fileName = "default";
  }

  public FileComponent(String fileName) {
    this.fileName = fileName;
    // System.out.println("FileComponent " + fileName + " created");
  }

  public void open() {
    isOpen = true;
    // more code
    // System.out.println("FileComponent " + fileName + " opened");
  }

  public void close() {
    isOpen = false;
    // more code
    // System.out.println("FileComponent " + fileName + " closed");
  }

  public void read() {
    if (!isOpen)
      throw new RuntimeException("FileComponent read while not open");
    // more code
    // System.out.println("FileComponent " + fileName + " read");
  }

  // no need for that now
  // public boolean isOpen() {
  // return isOpen;
  // }
}
