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
package com.ibm.safe.typestate.merge;

import com.ibm.wala.dataflow.IFDS.IMergeFunction;
import com.ibm.wala.util.intset.MutableMapping;

public class EmptyMergeFactory implements IMergeFunctionFactory {

  private static EmptyMergeFactory singleton = new EmptyMergeFactory();

  private EmptyMergeFactory() {
  }

  public IMergeFunction create(MutableMapping domain) {
    return null;
  }

  public static IMergeFunctionFactory instance() {
    return singleton;
  }

}
