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

import java.util.Collection;
import java.util.Iterator;

import com.ibm.safe.accesspath.AccessPath;
import com.ibm.safe.accesspath.AccessPathDictionary;
import com.ibm.safe.accesspath.AccessPathSet;
import com.ibm.safe.accesspath.AccessPathSetTransformers;
import com.ibm.safe.accesspath.AliasOracle;
import com.ibm.safe.accesspath.ArrayContentsPathElement;
import com.ibm.safe.accesspath.FIAliasOracle;
import com.ibm.safe.accesspath.InstanceFieldPathElement;
import com.ibm.safe.accesspath.PathElement;
import com.ibm.safe.accesspath.PointerPathElement;
import com.ibm.safe.accesspath.StaticFieldPathElement;
import com.ibm.safe.typestate.quad.Auxiliary;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.ArrayContentsKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder.TypedPointerKey;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.graph.GraphReachability;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * A naive implementation of AccessPathSet transformers and helper functions. In
 * the future, we will probably have to optimize these naive implementations.
 * However, not going into premature optimization, we first provide a naive (but
 * working!) implementation of the required functionality.
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public class MustAPSetTransformers extends AccessPathSetTransformers {

  /**
   * @param pointerAnalysis
   */
  public MustAPSetTransformers(PointerAnalysis pointerAnalysis, GraphReachability<CGNode,CGNode> reach) {
    super(pointerAnalysis, reach);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.accesspath.AccessPathSetTransformers#makeAliasOracle(com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis,
   *      com.ibm.wala.ipa.callgraph.CallGraph)
   */
  protected AliasOracle makeAliasOracle(PointerAnalysis pointerAnalysis, AccessPathDictionary APDictionary,
      GraphReachability<CGNode,CGNode> reach) {
    return new FIAliasOracle(pointerAnalysis, APDictionary, reach);
  }

  /**
   * Might pointers x and y be aliased, according to base pointer analysis?
   * 
   * Return value is in 3-value logic, where
   * <ul>
   * <li> LOGICAL_ZERO means "false"
   * <li> LOGICAL_ONE means "true"
   * <li> LOGICAL_ONEHALF means "maybe"
   * </ul>
   * 
   * @param x
   * @param y
   */
  public int aliased(PointerPathElement x, PointerPathElement y) {
    if (x.equals(y)) {
      return LOGICAL_ONE;
    }

    PointerKey xPtr = x.getPointerKey();
    PointerKey yPtr = y.getPointerKey();
    OrdinalSet<InstanceKey> xSet = getPointerAnalysis().getPointsToSet(xPtr);
    OrdinalSet<InstanceKey> ySet = getPointerAnalysis().getPointsToSet(yPtr);
    if (!xSet.containsAny(ySet)) {
      if (DEBUG) {
        Trace.println(">" + x + " and " + y + " are not points-to aliased");
      }
      return LOGICAL_ZERO;
    }
    return LOGICAL_ONEHALF;
  }

  /**
   * Might access path ap and pointer y be aliased?
   * 
   * Return value is in 3-value logic, where
   * <ul>
   * <li> LOGICAL_ZERO means "false"
   * <li> LOGICAL_ONE means "true"
   * <li> LOGICAL_ONEHALF means "maybe"
   * </ul>
   */
  public int aliased(AccessPath ap, PointerPathElement y) {
    if (ap.length() == 1) {
      return aliased((PointerPathElement) ap.getHead(), y);
    }

    // Here, we're checking if there's a way to reach an instance in the points-to
    // set of y by following along access path ap in the heap graph.  It would
    // seem efficient to start with y's points-to set and follow ap backwards
    // while constraining the possible instances that the current point in ap
    // may point to.  (If we reach the empty set, there's no alias.)

    HeapGraph heapGraph = getPointerAnalysis().getHeapGraph();

    // TODO: See if keeping all instance collections in OrdinalSets is more efficient.
    OrdinalSet<InstanceKey> ySet = getPointerAnalysis().getPointsToSet(y.getPointerKey());
    Collection<InstanceKey> front, nextFront = OrdinalSet.toCollection(ySet);

    for (int curPathIndex = ap.length() - 1; curPathIndex >= 0; curPathIndex--) {
      front = nextFront;
      nextFront = HashSetFactory.make();

      // Save the path-element and key values to avoid recomputation inside the loop.
      PathElement curApElem = ap.getElementAt(curPathIndex);
      PointerKey curApKey = null;
      if (curApElem instanceof PointerPathElement) {
        curApKey = ((PointerPathElement) curApElem).getPointerKey();
      } else if (curApElem instanceof StaticFieldPathElement) {
        curApKey = ((StaticFieldPathElement) curApElem).getPointerKey();
      }

      for (InstanceKey instanceKey : front) {

        for (Iterator predIt = heapGraph.getPredNodes(instanceKey); predIt.hasNext();) {
          PointerKey predPointerKey = (PointerKey) predIt.next();

          if (curPathIndex == 0) {  // Hoping to match the anchor.

            // Strip off the type-filter layer, if present.
            if (predPointerKey instanceof TypedPointerKey) {
              predPointerKey = ((TypedPointerKey) predPointerKey).getBase();
            }

            // Relying on equals to check that this is a static or pointer (instead of extra instanceof tests).
            if (predPointerKey.equals(curApKey)) {
              return LOGICAL_ONEHALF;  // Found a path all the way back to our ap's head.
            }

          } else {  // Moving a step back through instance-field or array-content pointer key.

            if (curApElem instanceof InstanceFieldPathElement) {
              InstanceFieldPathElement curApFieldElem = (InstanceFieldPathElement) curApElem;
              if (predPointerKey instanceof InstanceFieldKey) {
                InstanceFieldKey predInstanceFieldKey = (InstanceFieldKey) predPointerKey;
                IField predField = predInstanceFieldKey.getField();
                if (predField.equals(curApFieldElem.getField())) {
                  // predPointerKey and curApElem refer to the same field; add the instance to nextFront.
                  nextFront.add(predInstanceFieldKey.getInstanceKey());
                }
              }
            } else if (curApElem instanceof ArrayContentsPathElement) {
              ArrayContentsPathElement curApArrayContentsElem = (ArrayContentsPathElement) curApElem;
              // TODO: Check if the type filter is something that can be used to improve the test.
              if (predPointerKey instanceof ArrayContentsKey) {
                // predPointerKey and curApElem access array contents; add the instance to nextFront.
                nextFront.add(((ArrayContentsKey) predPointerKey).getInstanceKey());
              }
            }
          }
        }  // for (Iterator predIt ...

      }  // for (InstanceKey instanceKey : front)

      if (nextFront.isEmpty()) {
        return LOGICAL_ZERO;  // No instances in ySet are reachable from this point in ap.
      }
    }

    if (DEBUG) {
      Trace.println(">" + ap + " and " + y + " are not points-to aliased");
    }
    return LOGICAL_ZERO;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.accesspath.AccessPathSetTransformers#getMustAccessPaths(com.ibm.safe.typestate.triplet.TripletFactoid)
   */
  protected AccessPathSet getMustAccessPaths(Auxiliary aux) {
    MustAuxiliary m = (MustAuxiliary) aux;
    return m.getMustPaths();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.accesspath.AccessPathSetTransformers#getMayAliases(com.ibm.safe.accesspath.PointerPathElement,
   *      com.ibm.capa.util.intset.SparseIntSet,
   *      com.ibm.safe.typestate.triplet.TripletTypeStateDomain)
   */
  protected AccessPathSet getMayAliases(PointerPathElement x) {
    // TODO: enhance this using completeness information from factoids
    return getGlobalAliases(x);
  }

}