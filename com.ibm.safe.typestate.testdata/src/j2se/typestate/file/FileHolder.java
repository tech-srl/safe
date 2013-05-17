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

/**
 * A (potentially recursive) container of a FileComponent. 
 * Let's us create example with FileComponents deep into the heap 
 * @author Eran Yahav (yahave)
 */
public class FileHolder {
  public FileComponent file;

  public FileHolder holder;
}
