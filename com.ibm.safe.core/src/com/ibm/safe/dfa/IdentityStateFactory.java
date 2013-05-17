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
package com.ibm.safe.dfa;

/**
 * Returns the content object as the state (identity)
 * @author yahave
 *
 */
public class IdentityStateFactory implements IDFAStateFactory {

  private static IdentityStateFactory theInstance;

  public static IdentityStateFactory getInstance() {
    if (theInstance == null) {
      theInstance = new IdentityStateFactory();
    }
    return theInstance;
  }

  public Object createState(Object content) {
    return content;
  }

}
