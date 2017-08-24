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
package com.ibm.safe.accesspath;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.utils.Trace;
import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.AbstractLocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.AllocationSite;
import com.ibm.wala.ipa.callgraph.propagation.ArrayContentsKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder.TypedPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.GraphReachability;
import com.ibm.wala.util.intset.OrdinalSet;

/**
 * @author sfink
 * @author yahave (Eran Yahav)
 * 
 * An Alias oracle implementation based on DOMO's flow-insensitive
 * pointer analysis
 */
public class FIAliasOracle implements AliasOracle {

  private final static boolean DEBUG = false;

  /**
   * An optimization to exclude global aliases that involve two local pointers
   * that cannot be simultaneously live
   */
  private final static boolean PRUNE_MUTUALLY_DEAD_LOCALS = true;

  /**
   * governing pointer analysis
   */
  private final PointerAnalysis pointerAnalysis;

  /**
   * reachability analysis over the call graph
   */
  private final GraphReachability<CGNode,CGNode> reach;

  private final AccessPathDictionary APDictionary;

  /**
   * @param pointerAnalysis
   *          governing pointer analysis
   */
  public FIAliasOracle(PointerAnalysis pointerAnalysis, AccessPathDictionary APDictionary, GraphReachability<CGNode,CGNode> reach) {
    this.pointerAnalysis = pointerAnalysis;
    this.reach = reach;
    this.APDictionary = APDictionary;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.accesspath.AliasOracle#getGlobalAliases(com.ibm.safe.accesspath
   * .PointerPathElement)
   */
  public AccessPathSet getGlobalAliases(PointerPathElement x) {
    AccessPathSet result = new AccessPathSet(APDictionary);
    PointerKey xPtr = x.getPointerKey();

    OrdinalSet<InstanceKey> xPointsTo = pointerAnalysis.getPointsToSet(xPtr);
    if (DEBUG) {
      Trace.println("*PointsTo set for " + xPtr + " has size: " + xPointsTo.size());
      Trace.println("*PointsTo set for " + xPtr + " is: " + xPointsTo);
    }
    for (Iterator<InstanceKey> it = xPointsTo.iterator(); it.hasNext();) {
      InstanceKey ik = it.next();
      if (DEBUG) {
        Trace.println("Processing PTS element " + ik + " for " + xPtr);
      }
      // TODO: cache this?
      result.addAll(getAcyclicAccessPaths(ik));
    }
    if (DEBUG) {
      Trace.println("Aliases for: " + xPtr + " = " + result);
    }

    if (PRUNE_MUTUALLY_DEAD_LOCALS) {
      if (x instanceof LocalPathElement) {
        LocalPointerKey lpk = (LocalPointerKey) x.getPointerKey();
        result = pruneForLiveLocals(result, lpk.getNode());
      }
    }
    return result;
  }

  /**
   * @param s
   *          Set<accessPath>
   * @param node
   * @return the subset s` of s s.t. for each p \in s`, if p starts with a local
   *         pointer, then p.getNode() is reachable from node
   */
  private AccessPathSet pruneForLiveLocals(AccessPathSet s, CGNode node) {
    AccessPathSet result = new AccessPathSet(APDictionary);

    for (Iterator<AccessPath> it = s.iterator(); it.hasNext();) {
      AccessPath ap = it.next();
      if (ap.length() > 0) {
        // TODO: why do we have access paths of length 0
        PathElement pe = ap.getHead();
        if (pe instanceof LocalPathElement) {
          LocalPathElement lpe = (LocalPathElement) pe;
          AbstractLocalPointerKey lpk = (AbstractLocalPointerKey) lpe.getPointerKey();
          CGNode n = lpk.getNode();
          OrdinalSet<CGNode> reachable = reach.getReachableSet(n);
          if (reachable.contains(node)) {
            result.add(ap);
          }
        } else {
          // ap is not anchored in a local, so include it
          result.add(ap);
        }
      }
    }
    return result;
  }

  /**
   * return a set of acyclic access paths that may be pointing to the given
   * instance. Fixed a couple of bugs and made significant optimizations but the
   * code is so expensive that it's not useful (and can't be tested fully).
   * Further optimizations are possible but seem pointless. Clients should test
   * aliasing by computing overlap going forward in paths that actually matter,
   * rather than asking for all paths to an instance. [alexey]
   * 
   * @param instance
   *          - an instance which is the target of the access paths
   */
  @Deprecated
  public AccessPathSet getAcyclicAccessPaths(InstanceKey instance) {
    AccessPathSet result = new AccessPathSet(APDictionary);
    assert instance != null;

    HeapGraph heapGraph = pointerAnalysis.getHeapGraph();
    // set of nodes which have been visited
    Set<Object> visited = HashSetFactory.make();

    Set<Object> front = Collections.emptySet();
    Set<Object> nextFront = Collections.singleton((Object) instance);
    AccessPathSet currentPaths = null;
    AccessPathSet nextPaths = new AccessPathSet(APDictionary);

    AccessPathSet anchoredPaths = new AccessPathSet(APDictionary);

    while (!nextFront.isEmpty()) {
      visited.addAll(front);
      front = nextFront;
      currentPaths = nextPaths;
      nextFront = HashSetFactory.make();
      nextPaths = new AccessPathSet(APDictionary);
      for (Iterator<Object> frontIterator = front.iterator(); frontIterator.hasNext();) {
        Object node = frontIterator.next();
        if (DEBUG) {
          Trace.println("FrontNode: " + node);
        }

        for (Iterator<? extends Object> predIt = heapGraph.getPredNodes(node); predIt.hasNext();) {
          Object predObj = predIt.next();
          if (DEBUG) {
            Trace.println("PredNode: " + predObj);
            Trace.println("PredNode Class: " + predObj.getClass());
          }

          if (!visited.contains(predObj)) {
            if (DEBUG) {
              Trace.println("currentPaths: " + currentPaths);
            }
            PathElement predElement = null;

            if (predObj instanceof InstanceFieldKey) {
              IField predField = ((InstanceFieldKey) predObj).getField();
              predElement = new InstanceFieldPathElement(predField);
            } else if (predObj instanceof AbstractLocalPointerKey) {
              AbstractLocalPointerKey predPointer = (AbstractLocalPointerKey) predObj;
              predElement = new LocalPathElement(predPointer);
            } else if (predObj instanceof AllocationSite) {
              AllocationSite predAllocSite = (AllocationSite) predObj;
              if (DEBUG) {
                Trace.println("** " + node.toString() + node.getClass());
                Trace.println("** " + predAllocSite.toString());
              }
              predElement = null;
            } else if (predObj instanceof StaticFieldKey) {
              predElement = new StaticFieldPathElement((StaticFieldKey) predObj);
            } else if (predObj instanceof ArrayContentsKey) {
              predElement = ArrayContentsPathElement.instance();
            } else if (predObj instanceof TypedPointerKey) {
              TypedPointerKey tpk = (TypedPointerKey) predObj;
              predObj = tpk.getBase();

              assert predObj instanceof AbstractLocalPointerKey;

              AbstractLocalPointerKey lpk = (AbstractLocalPointerKey) predObj;
              predElement = new LocalPathElement(lpk);
            } else if (predObj instanceof InstanceKey) {
              // do nothing.
            } else {
              Assertions.UNREACHABLE(predObj.getClass().toString());
            }

            for (Iterator<AccessPath> pathIterator = currentPaths.iterator(); pathIterator.hasNext();) {
              Object item = pathIterator.next();
              if (DEBUG) {
                Trace.println("Item class:" + item.getClass());
              }
              AccessPath curr = (AccessPath) item;

              if (predElement == null) {
                // Ran into an instance key, e.g., allocation site: copy
                // unchanged curr to nextPaths.
                // TODO: If we ever implement removal from the iterator, it
                // should be safe to remove
                // curr from pathIterator (currentPaths) here. BTW, curr cannot
                // be anchored here.
                nextPaths.add(curr);
                if (DEBUG) {
                  Trace.println("Copied to nextPaths: " + curr);
                }
              } else {
                // If curr makes sense as an extension of predElement, prepend
                // predElement to curr
                // and save it in anchoredPaths or nextPaths, as appropriate.
                if (isValidSuffix(node, curr)) {
                  AccessPath ap = APDictionary.concat(predElement, curr);
                  (predElement.isAnchor() ? anchoredPaths : nextPaths).add(ap);
                  if (DEBUG) {
                    Trace.println("Added to " + (ap.isAnchored() ? "anchoredPaths" : "nextPaths") + ": " + ap);
                  }
                }
              }
            }

            // Process immediate predecessors of instance: if predElement is an
            // anchor, add the
            // corresponding path to anchoredPaths, o.w. add it to nextPaths for
            // future processing.
            if (currentPaths.isEmpty() && predElement != null) {
              AccessPath ap = APDictionary.findOrCreate(predElement);
              (predElement.isAnchor() ? anchoredPaths : nextPaths).add(ap);
              if (DEBUG) {
                Trace.println("Added to " + (ap.isAnchored() ? "anchoredPaths" : "nextPaths") + ": " + ap);
              }
            }

            if (predElement == null || !predElement.isAnchor()) { // Don't waste
              // cycles on
              // nodes with
              // no
              // predecessors.
              nextFront.add(predObj);
            }
          }
        }
      }
    }
    if (DEBUG) {
      Trace.println("Got number of paths: " + currentPaths.size());
    }
    for (Iterator<AccessPath> it = currentPaths.iterator(); it.hasNext();) {
      AccessPath currPath = it.next();
      if (DEBUG) {
        Trace.println("PATH: " + currPath);
      }
    }

    result.addAll(anchoredPaths);
    result.addAll(currentPaths);

    assert !result.isEmpty() : "instance must be potentially reachable " + instance;
    return result;
  }

  private boolean isValidSuffix(Object node, AccessPath path) {
    PathElement pathHead = path.getHead();
    if (pathHead instanceof InstanceFieldPathElement) {
      IField pathHeadField = ((InstanceFieldPathElement) pathHead).getField();
      for (Iterator<Object> succIter = pointerAnalysis.getHeapGraph().getSuccNodes(node); succIter.hasNext();) {
        Object succObj = succIter.next();
        if (succObj instanceof InstanceFieldKey) {
          InstanceFieldKey succInstanceFieldKey = (InstanceFieldKey) succObj;
          if (succInstanceFieldKey.getField().equals(pathHeadField)) {
            return true;
          }
        }
      }
      return false;
    } else if (pathHead instanceof ArrayContentsPathElement) {
      for (Iterator<Object> succIter = pointerAnalysis.getHeapGraph().getSuccNodes(node); succIter.hasNext();) {
        Object succObj = succIter.next();
        if (succObj instanceof ArrayContentsKey) {
          // ArrayContentsPathElement remembers nothing about the key, so we
          // can't do any better than
          // to say that it could be the right array contents.
          return true;
        }
      }
      return false;
    } else {
      Assertions.UNREACHABLE("Expected path to start with an instance field or array contents: " + path);
      return false;
    }
  }
}
