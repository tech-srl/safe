/*******************************************************************************
 * Copyright (c) 2002-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.metrics;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.MapIterator;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.functions.Function;

/**
 * This maintains a counter for each non-negative integer this is implemented as
 * a HashMap.
 * 
 * Use classes such as IntVector where appropriate, instead.
 * 
 * @author sfink
 * 
 */
public class IntHistogram {

  /**
   * Map: Integer -> Integer
   */
  private final HashMap<Integer, Integer> map = HashMapFactory.make();

  private static Function<Map.Entry<Integer,Integer>, Pair<Integer,Integer>> toPair = 
    new Function<Map.Entry<Integer,Integer>, Pair<Integer,Integer>>() {
    public Pair<Integer, Integer> apply(Map.Entry<Integer,Integer> object) {
      Map.Entry<Integer,Integer> e = (Map.Entry<Integer,Integer>) object;
      return Pair.make(e.getKey(), e.getValue());
    }
  };

  /**
   * add y to the count for x
   */
  public void add(int x, int y) {
    Integer Y = map.get(x);
    if (Y == null) {
      map.put(x, y);
    } else {
      map.put(x, y + Y.intValue());
    }
  }

  /**
   * @return Iterator<Pair>
   */
  public Iterator<Pair<Integer,Integer>> iterator() {
    return new MapIterator<Map.Entry<Integer,Integer>, Pair<Integer,Integer>>(map.entrySet().iterator(), toPair);
  }

}
