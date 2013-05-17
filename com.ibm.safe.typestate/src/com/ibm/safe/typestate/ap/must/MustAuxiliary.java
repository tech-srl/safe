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
package com.ibm.safe.typestate.ap.must;

import com.ibm.safe.accesspath.AccessPathSet;
import com.ibm.safe.typestate.quad.Auxiliary;

/**
 * @author yahave
 * @author sfink
 * 
 * State tracking must-alias information
 */
public class MustAuxiliary implements Auxiliary {

  /**
   * access paths we know are must-alias to a given instance
   */
  private final AccessPathSet mustPaths;

  /**
   * Are the must-alias paths complete? If so, then for an AP m, m may point to
   * the instance iff m /in mustPaths
   */
  private final boolean complete;

  /**
   * @param mustPaths
   *            access paths we know are must-alias to a given instance
   * @param complete
   *            Are the must-alias paths complete?
   */
  public MustAuxiliary(AccessPathSet mustPaths, boolean complete) {
    this.mustPaths = mustPaths;
    this.complete = complete;
  }

  /**
   * @return Returns the complete.
   */
  public boolean isComplete() {
    return complete;
  }

  /**
   * @return Returns the mustPaths.
   */
  public AccessPathSet getMustPaths() {
    return mustPaths;
  }

  public boolean equals(Object arg0) {
    if (arg0 == null)
      return false;
    if (getClass().equals(arg0.getClass())) {
      MustAuxiliary other = (MustAuxiliary) arg0;
      return mustPaths.equals(other.mustPaths) && complete == other.complete;
    } else {
      return false;
    }
  }

  public int hashCode() {
    return 6911 * mustPaths.hashCode() + (complete ? 1 : 0);
  }

  public String toString() {
    return mustPaths.toString() + "," + complete;
  }
}
