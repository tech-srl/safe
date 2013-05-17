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
package com.ibm.safe.typestate.rules;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.debug.Assertions;

/**
 * @author sfink
 * @author yahave
 * 
 * This class controls the "batch size" which controls the instances which are
 * solved simultaneously in the typestate solver.
 * 
 * This class is intended to be used as a mix-in, to control separation as an
 * implementation detail
 */
public abstract class InstanceBatchIterator {

  /**
   * the set of all instances which must be solved for
   */
  private final Collection<InstanceKey> allInstances;

  /**
   * @param allInstances
   *            the set of all instances which must be solved for
   */
  InstanceBatchIterator(Collection<InstanceKey> allInstances) {
    this.allInstances = allInstances;
  }

  /**
   * @return an Iterator<Collection<InstanceKey>>, where each Collection of
   *         instances will be solved simultaneously
   */
  abstract Iterator<Collection<InstanceKey>> getInstanceBatches();

  /**
   * 
   * @param allInstances
   * @return an interator which embodies the "no separation" batch policy
   */
  public static InstanceBatchIterator makeNoSeparation(Collection<InstanceKey> allInstances) {
    return new AllTogether(allInstances);
  }

  /**
   * 
   * @param allInstances
   * @return an interator which embodies the "complete separation" batch policy
   */
  public static InstanceBatchIterator makeSeparation(Collection<InstanceKey> allInstances) {
    return new Separation(allInstances);
  }

  /**
   * @author sfink
   * 
   * "no separation": solve all instances together
   */
  private static class AllTogether extends InstanceBatchIterator {

    AllTogether(Collection<InstanceKey> allInstances) {
      super(allInstances);
    }

    Iterator<Collection<InstanceKey>> getInstanceBatches() {
      return Collections.singleton(super.allInstances).iterator();
    }
  }

  /**
   * @author sfink
   * @author yahave
   * 
   * "separation": solve each instance separately
   */
  private static class Separation extends InstanceBatchIterator {

    Separation(Collection<InstanceKey> allInstances) {
      super(allInstances);
    }

    Iterator<Collection<InstanceKey>> getInstanceBatches() {
      final Iterator<InstanceKey> it = super.allInstances.iterator();
      return new Iterator<Collection<InstanceKey>>() {

        public void remove() {
          Assertions.UNREACHABLE();
        }

        public boolean hasNext() {
          return it.hasNext();
        }

        public Collection<InstanceKey> next() {
          return Collections.singleton(it.next());
        }

      };
    }
  }
}
