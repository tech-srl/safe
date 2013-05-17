/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.accesspath;


import com.ibm.wala.classLoader.IField;

/**
 * An instance field in an access-path string.
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public class InstanceFieldPathElement extends FieldPathElement {

  /**
   * @param fld
   */
  public InstanceFieldPathElement(IField fld) {
    super(fld);
    assert(!fld.isStatic());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.accesspaths.PathElement#isAnchor()
   */
  public boolean isAnchor() {
    return false;
  }
}
