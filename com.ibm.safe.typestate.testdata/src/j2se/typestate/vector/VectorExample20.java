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
package j2se.typestate.vector;

import java.util.Vector;

/**
 * @author sfink
 * 
 * Vector in a vector ... designed to exercise SAFE bug 45655 regarding ignoring
 * aliases induced by events.
 * 
 * Has one real error.
 * 
 * APMust currently reports 2 errors (one false positive), because it loses the
 * must-alias property for v2 during an arraystore. TODO: think about how to
 * deal with this. For example, an arraystore or putfield doesn't add any new
 * anchored length 1 access paths, so maybe we can be be more precise about the
 * possible contents of v1 (can't be V2), even though we've potentially lost
 * complete must-alias on v2.
 */
public final class VectorExample20 {

  public static void main(String[] args) {
    Vector v1 = new Vector();
    Vector v2 = new Vector();
    if (args.length > 1) {
      // confuse the engine to think the call to add might be
      // an event.
      v1 = v2;
    }
    v1.add(v2);

    // false positive: AP-must engine thinks that V2 might be empty
    // here (true), and that v1 might point to V2 (since it lost
    // must-alias to V2 during the call to add)
    // there's an invariant we're missing which is
    // "v1 points to something that's not empty"
    // this may be a fundamental flaw in everything we're doing?
    Vector v3 = (Vector) v1.get(0);

    v3.firstElement();

  }
}