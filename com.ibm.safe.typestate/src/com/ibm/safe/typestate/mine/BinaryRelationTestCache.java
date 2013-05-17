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
package com.ibm.safe.typestate.mine;

import com.ibm.wala.util.intset.BasicNaturalRelation;
import com.ibm.wala.util.intset.IBinaryNaturalRelation;
import com.ibm.wala.util.intset.IntSet;

/**
 * @author sfink
 * 
 * a cache for results of a binary relation test. used for optimization
 */
public class BinaryRelationTestCache {

  /**
   * relation of (x,y) such that we know R(x,y)
   */
  private final IBinaryNaturalRelation testedTrue = new BasicNaturalRelation();

  /**
   * relation of (x,y) such that we know NOT R(x,y)
   */
  private final IBinaryNaturalRelation testedFalse = new BasicNaturalRelation();

  /**
   * Have we recorded the fact that R(i,j) holds?
   */
  public boolean isTrue(int i, int j) {
    return testedTrue.contains(i, j);
  }

  /**
   * Have we recorded the fact that R(i,j) does NOT hold?
   */
  public boolean isFalse(int i, int j) {
    return testedFalse.contains(i, j);
  }

  /**
   * Record the fact that R(i,j) does NOT hold.
   */
  public void recordFalse(int i, int j) {
    testedFalse.add(i, j);
  }

  /**
   * Record the fact that R(i,j) does hold.
   */
  public void recordTrue(int i, int j) {
    testedTrue.add(i, j);
  }

  /**
   * @return set of j known that R(i,j) holds, or null if none.
   */
  public IntSet getKnownTrue(int i) {
    return testedTrue.getRelated(i);
  }

}
