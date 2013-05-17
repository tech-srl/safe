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
 * @author yahave
 */
public class OpFileComponent {
  private boolean closed = true;

  private Object dummy_to_avoid_smashing;

  public OpFileComponent() {
  }

  public void open() {
    closed = false;
  }

  public void close() {
    closed = true;
  }

  public void read() {
    if (closed) {
      throw new RuntimeException("Cannot read from closed file");
    }
  }
}
