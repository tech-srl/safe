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

package com.ibm.safe.typestate.ap.must.mustnot;

import com.ibm.safe.accesspath.AccessPathSet;
import com.ibm.safe.typestate.ap.must.MustAuxiliary;
import com.ibm.wala.util.debug.Assertions;

/**
 * State tracking must and must-not-alias information
 * 
 * @author yahave, sjfink
 */
public class MustMustNotAuxiliary extends MustAuxiliary {

  private final static boolean DEBUG = false;

  /**
   * access paths we know are must-not-alias to a given instance
   */
  private final AccessPathSet mustNotPaths;

  /**
   * @param mustPaths
   *          access paths we know are must-alias to a given instance
   * @param mustNotPaths
   *          access paths we know are must-not-alias to a given instance
   * @param complete
   *          Are the must-alias paths complete?
   */
  public MustMustNotAuxiliary(AccessPathSet mustPaths, AccessPathSet mustNotPaths, boolean complete) {
    super(mustPaths, complete);
    this.mustNotPaths = mustNotPaths;

    if (DEBUG) {
      AccessPathSet temp = new AccessPathSet(mustPaths);
      int size = temp.size();
      temp.removeAll(mustNotPaths);
      if (temp.size() != size) {
        System.err.println("MUST " + mustPaths);
        System.err.println("MUST NOT " + mustNotPaths);
        Assertions.UNREACHABLE("uh oh.  not disjoint.");
      }
    }
  }

  public String toString() {
    return getMustPaths().toString() + "," + (isComplete() ? "complete" : "notComplete") + "," + getMustNotPaths().toString();
  }

  /**
   * @return Returns the mustNotPaths.
   */
  public AccessPathSet getMustNotPaths() {
    return mustNotPaths;
  }

  public boolean equals(Object arg0) {
    if (arg0 instanceof MustMustNotAuxiliary) {
      MustMustNotAuxiliary other = (MustMustNotAuxiliary) arg0;
      return super.equals(arg0) && mustNotPaths.equals(other.mustNotPaths);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return super.hashCode() + 7753 * mustNotPaths.hashCode();
  }

}
