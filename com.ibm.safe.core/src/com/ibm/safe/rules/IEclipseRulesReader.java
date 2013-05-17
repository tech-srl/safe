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
package com.ibm.safe.rules;

import java.io.IOException;

/**
 * Not only this interface can read SAFE EMF rules
 * {@link com.ibm.safe.rules.IRulesReader}, it can also persist them in
 * Eclipse metadata, allowing customization of them accross the session.
 * 
 * @author egeay
 */
public interface IEclipseRulesReader extends IRulesReader {

  /**
   * Saves EMF rules loaded in memory into Eclipse metadata.
   * 
   * @throws IOException
   *             Occurs if for any reasons we are not able to save the rules on
   *             disk.
   */
  public void persistEMFResources() throws IOException;

}
