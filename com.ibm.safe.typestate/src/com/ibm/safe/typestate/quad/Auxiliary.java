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
/*
 * Created on Dec 23, 2004
 */
package com.ibm.safe.typestate.quad;

/**
 * A member of the auxiliary domain, the third element in a triplet factoid.
 * 
 * An auxiliary must implement equals and hashCode according to its values.
 * 
 * @author Eran Yahav (yahave)
 */
public interface Auxiliary {

  public boolean equals(Object other);

  public int hashCode();
}