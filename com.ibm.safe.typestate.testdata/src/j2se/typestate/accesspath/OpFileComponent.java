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
package j2se.typestate.accesspath;

public class OpFileComponent {
  private boolean closed = true;

  public OpFileComponent() {
  }

  public void open() {
    closed = false;
  }

  public void read() {
    if (closed) {
      throw new RuntimeException("Cannot read from closed file");
    }
  }
}
