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
package com.ibm.safe.accesspath;


/**
 * A path element which represents the operator [i] in an access path (an array
 * element)
 * 
 * TODO: track more info about the array or i?
 * 
 * @author yahave
 * @author sfink
 */
public class ArrayContentsPathElement implements PathElement {

  private static final ArrayContentsPathElement singleton = new ArrayContentsPathElement();

  /**
   * @return the unique instance of this class
   */
  public static ArrayContentsPathElement instance() {
    return singleton;
  }

  /**
   * prevent external instantiation
   */
  private ArrayContentsPathElement() {
  }

  public boolean isAnchor() {
    return false;
  }
  
  public String toString() {
    return "[?]";
  }

}
