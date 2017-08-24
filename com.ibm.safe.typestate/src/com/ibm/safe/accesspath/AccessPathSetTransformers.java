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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.ibm.safe.typestate.quad.Auxiliary;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.GraphReachability;

/**
 * A naive implementation of AccessPathSet transformers and helper functions. In
 * the future, we will probably have to optimize these naive implementations.
 * However, not going into premature optimization, we first provide a naive (but
 * working!) implementation of the required functionality.
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 */
public abstract class AccessPathSetTransformers {

  protected final static boolean DEBUG = false;

  /**
   * A token for 3-value logic that means "false"
   */
  protected static final int LOGICAL_ZERO = 0;

  /**
   * A token for 3-value logic that means "true"
   */
  protected static final int LOGICAL_ONE = 1;

  /**
   * A token for 3-value logic that means "maybe"
   */
  protected static final int LOGICAL_ONEHALF = 2;

  /**
   * governing pointer analysis
   */
  private final PointerAnalysis pointerAnalysis;

  /**
   * A fall-back for asking alias questions
   */
  private final AliasOracle aliasOracle;

  private final AccessPathDictionary APDictionary = new AccessPathDictionary();

  public AccessPathSetTransformers(PointerAnalysis pointerAnalysis, GraphReachability<CGNode,CGNode> reach) {
    this.pointerAnalysis = pointerAnalysis;
    this.aliasOracle = makeAliasOracle(pointerAnalysis, APDictionary, reach);
  }

  protected abstract AliasOracle makeAliasOracle(PointerAnalysis pointerAnalysis, AccessPathDictionary APDictionary,
      GraphReachability<CGNode,CGNode> reach);

  /**
   * kill all paths of s that start with x
   * 
   * @param s -
   *            input set
   * @param x -
   *            path element
   * @return a new set { y.path | y.path in s and y <>x }
   */
  public AccessPathSet kill(AccessPathSet s, PathElement x) {
    return s.pathsWithOtherRoot(x);
  }

  /**
   * kill all paths of s that start with some x in roots
   * 
   * @param s -
   *            input set
   * @param roots -
   *            set of path elements
   * @return a new set { y.path | y.path in s and y not in roots }
   */
  public AccessPathSet kill(AccessPathSet s, Set<PathElement> roots) {
    return s.pathsWithOtherRoots(roots);
  }

  /**
   * add a path x.path for every existing y.path in s
   * 
   * @param s -
   *            input set
   * @param x -
   *            path element
   * @param y -
   *            path
   * @return a new set --- s U { x.path | y.path in s }
   */
  public AccessPathSet assign(AccessPathSet s, PathElement x, AccessPath y) {
    AccessPathSet result = new AccessPathSet(s);
    result.addAll(gen(s, x, y));
    return result;
  }

  /**
   * add a path x.path for every existing y.path in s
   * 
   * @param s -
   *            input set
   * @param x -
   *            path element
   * @param y -
   *            path
   * @return a new set --- { x.path | y.path in s }
   */
  public AccessPathSet gen(AccessPathSet s, PathElement x, AccessPath y) {
    AccessPathSet result = new AccessPathSet(APDictionary);
    AccessPathSet yPaths = s.pathsWithPrefix(y);
    for (Iterator<AccessPath> it = yPaths.iterator(); it.hasNext();) {
      AccessPath currPath = it.next();
      AccessPath newPath = APDictionary.findOrCreate(x);
      if (currPath.length() > y.length()) {
        List<PathElement> currSuffix = currPath.getSuffix(y.length());
        newPath = APDictionary.concat(newPath, currSuffix);
      }
      result.add(newPath);
    }
    return result;
  }

  /**
   * add a path x.path for every existing y.path in s
   * 
   * @param s -
   *            input set
   * @param x -
   *            path element
   * @param ySet -
   *            set of paths
   * @return a new set --- s U { x.path | y.path in s such that y in ySet}
   */
  public AccessPathSet assign(AccessPathSet s, PathElement x, Set<AccessPath> ySet) {
    AccessPathSet result = new AccessPathSet(s);
    for (Iterator<AccessPath> yIterator = ySet.iterator(); yIterator.hasNext();) {
      AccessPath y = yIterator.next();
      result.addAll(gen(s, x, y));
    }
    return result;
  }

  /**
   * add a path x.path for every existing y.path in s
   * 
   * TODO: more optimization is needed
   * 
   * @param s -
   *            input set
   * @param x -
   *            path element
   * @param ySet -
   *            set of path elements
   * @return a new set --- s \ ySet U { x.path | y.path in s such that y in
   *         ySet}
   */
  public AccessPathSet rename(AccessPathSet s, PathElement x, Set<? extends PathElement> ySet) {

    if (s.isEmpty()) {
      return s;
    }

    // most of the time we expect ySet to be small, but s may be bigger.
    AccessPathSet result = new AccessPathSet(s.getAPDictionary());
    for (Iterator<AccessPath> it = s.iterator(); it.hasNext();) {
      AccessPath ap = it.next();
      if (ySet.contains(ap.getHead())) {
        if (ap.length() > 1) {
          result.add(APDictionary.concat(x, ap.getSuffix(1)));
        } else {
          result.add(APDictionary.findOrCreate(x));
        }
      } else {
        result.add(ap);
      }
    }
    return result;
  }

  /**
   * add a path x.path for every existing y.path in s (also for the empty path)
   * 
   * @param s1 -
   *            input set
   * @param s2 -
   *            input set
   * @param x -
   *            path element
   * @param ySet -
   *            set of paths
   * @return a new set --- s1 U { x.path | y.path in (s1 U s2) such that y in
   *         ySet}
   */
  public AccessPathSet assign(AccessPathSet s1, AccessPathSet s2, PathElement x, Set<AccessPath> ySet) {

    if (DEBUG) {
      Trace.println("---------------------------------------------------");
      Trace.println("| assign (two srcs): " + s1 + ", " + s2);
      Trace.println("| x=" + x);
      Trace.println("| ySet=" + ySet);
    }

    AccessPathSet result = new AccessPathSet(s1);
    for (Iterator<AccessPath> yIterator = ySet.iterator(); yIterator.hasNext();) {
      AccessPath y = yIterator.next();
      PathElement yHead = y.getHead();

      AccessPathSet yPaths1 = s1.pathsFrom(yHead);
      AccessPathSet yPaths2 = s2.pathsFrom(yHead);

      AccessPathSet yPaths = new AccessPathSet(APDictionary);
      yPaths.addAll(yPaths1);
      yPaths.addAll(yPaths2);

      for (Iterator<AccessPath> it = yPaths.iterator(); it.hasNext();) {
        AccessPath currPath = it.next();
        AccessPath newPath = APDictionary.findOrCreate(x);
        if (currPath.length() > y.length()) {
          List<PathElement> currSuffix = currPath.getSuffix(y.length());
          newPath = APDictionary.concat(newPath, currSuffix);
        }
        if (DEBUG) {
          Trace.println("| Adding path: " + newPath);
          result.add(newPath);
        }
      }
    }

    if (DEBUG) {
      Trace.println("| Result after adding new paths: " + result);
      Trace.println("---------------------------------------------------");
    }
    return result;
  }

  /**
   * Return the set of all possible access paths that might be aliased with x
   * 
   * @param x
   * @return Set<AccessPath>
   */
  protected AccessPathSet getGlobalAliases(PointerPathElement x) {
    return aliasOracle.getGlobalAliases(x);
  }

  /**
   * return a set of access paths up to depth maximalDepth that may be pointing
   * to the given instance.
   * 
   * @param instance -
   *            an instance which is the target of the access paths
   * @param maximalDepth -
   *            limit interest to paths of up to depth maximalDepth
   */
  public AccessPathSet getAccessPaths(InstanceKey instance, int maximalDepth) {
    assert instance != null && maximalDepth > 0;
    throw new UnsupportedOperationException("NYI");
  }

  /**
   * @return Returns the pointerAnalysis.
   */
  protected PointerAnalysis getPointerAnalysis() {
    return pointerAnalysis;
  }

  /**
   * TODO: stronger typing than Auxiliary
   * 
   * @param aux
   *            auxiliary information which contains a set of access paths that
   *            are interpreted as "must-alias" paths
   * @return the set of access paths contained in this auxiliary
   */
  protected abstract AccessPathSet getMustAccessPaths(Auxiliary aux);

  /**
   * update the must alias set s based on the assignment x.f = y
   * 
   * @return a new set --- s1 := (s \ mustKill) U mustGen
   */
  public AccessPathSet updateMust(AccessPathSet s, AccessPath x_f, PathElement y, int klimit) {
    if (DEBUG) {
      Trace.println("---------------------------------------------------");
      Trace.println("| update must: " + s);
      Trace.println("| X.F=" + x_f);
      Trace.println("| y=" + y);
    }
    AccessPathSet result = new AccessPathSet(s);
    AccessPathSet killSet = mustKill(result, x_f, klimit);
    if (DEBUG) {
      Trace.println("| killSet: " + killSet);
    }
    result.removeAll(killSet);
    if (DEBUG) {
      Trace.println("| Result after removing killSet: " + result);
    }

    AccessPathSet genSet = mustGen(s, x_f, y, klimit);
    if (DEBUG) {
      Trace.println("| genSet: " + genSet);
    }
    result.addAll(genSet);
    if (DEBUG) {
      Trace.println("| Result after adding genSet: " + result);
      Trace.println("---------------------------------------------------");
    }
    return result;
  }

  /**
   * compute the set of must-alias access paths in s that must be killed as a
   * result of an assignment to x.f
   * 
   * @param s
   * @param x_f
   * @param klimit
   * @return { path0.f.path1 \in s | may-alias(x,path0) }
   */
  public AccessPathSet mustKill(AccessPathSet s, AccessPath x_f, int klimit) {
    AccessPathSet result = new AccessPathSet(APDictionary);
    PointerPathElement x = (PointerPathElement) x_f.getHead();
    if (x instanceof StaticFieldPathElement) {
      assert x_f.length() == 1;
      return s.pathsWithPrefix(x_f);
    } else {
      assert x_f.length() == 2;
      // x_f is a reference to an instance field
      PathElement f = x_f.getElementAt(1);

      for (Iterator<AccessPath> it = s.iterator(); it.hasNext();) {
        AccessPath currPath = (AccessPath) it.next();
        for (int i = 1; i < currPath.length(); i++) {
          // Does currPath contain field f?
          if (currPath.getElementAt(i).equals(f)) {
            // Is the part of currPath before f a may-alias of x?
            int mayAliasPrefix = (i == 1 ?
                // Do the simpler computation if the prefix is just a pointer.
                aliased((PointerPathElement) currPath.getHead(), x)
              : aliased(APDictionary.findOrCreate(currPath.getPrefix(i)), x));
            if (mayAliasPrefix != LOGICAL_ZERO) {
              result.add(currPath);
              break;
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * compute the set of must-alias access paths in s that must be GENned as a
   * result of an assignment x.f := y
   * 
   * @return { path0.f.path1 | y.path1 \in s, and must-alias(x,path0) }
   */
  protected AccessPathSet mustGen(AccessPathSet s, AccessPath x_f, PathElement y, int klimit) {
    AccessPathSet result = new AccessPathSet(APDictionary);
    PathElement x = x_f.getHead();
    if (x instanceof StaticFieldPathElement) {
      // handle assignment to static field
      assert x_f.length() == 1;
      // { x.path | y.path in s }
      return gen(s, x, APDictionary.findOrCreate(y));
    } else {
      assert x_f.length() == 2;
      PathElement f = x_f.getElementAt(1);
      AccessPathSet pathsFromY = s.pathsFrom(y);
      // Compute x's must-aliases once, and only if needed.
      Set<AccessPath> xMustAliases = (pathsFromY.isEmpty() ? null : getMustAliases(x));

      for (Iterator<AccessPath> it = pathsFromY.iterator(); it.hasNext();) {
        AccessPath currPath = it.next();
        PathElement anchor = currPath.getHead();
        Assertions.productionAssertion(anchor.equals(y));

        for (Iterator<AccessPath> mustIt = xMustAliases.iterator(); mustIt.hasNext();) {
          AccessPath currMustAlias = mustIt.next();
          AccessPath newPath = APDictionary.concat(currMustAlias, f);
          if (currPath.length() > 1) {
            newPath = APDictionary.concat(newPath, currPath.getSuffix(1));
          }
          result.add(newPath);
        }
      }
    }
    return result;
  }

  /**
   * @return Set <AccessPath> of must aliases of x
   *         <p>
   *         note:
   *         <ul>
   *         <li> x could be must-aliased with any arbitrary access path
   *         <li> result always contains x as a must-alias of itself.
   *         </ul>
   */
  protected Set<AccessPath> getMustAliases(PathElement x) {
    Set<AccessPath> result = HashSetFactory.make(1);
    AccessPath xPath = APDictionary.findOrCreate(x);
    result.add(xPath);
    return result;
  }

  /**
   * Might pointers x and y be aliased?
   * 
   * Return value is in 3-value logic, where
   * <ul>
   * <li> LOGICAL_ZERO means "false"
   * <li> LOGICAL_ONE means "true"
   * <li> LOGICAL_ONEHALF means "maybe"
   * </ul>
   */
  public abstract int aliased(PointerPathElement x, PointerPathElement y);

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
  public abstract int aliased(AccessPath ap, PointerPathElement y);

  /**
   * update the may alias set s based on the assignment x.f = y
   * 
   * @return a new set --- s1 := (s \ mayKill) U mayGen
   */
  public AccessPathSet updateMay(AccessPathSet s, AccessPath x_f, PathElement y, int klimit) {
    if (DEBUG) {

      Trace.println("---------------------------------------------------");
      Trace.println("| update MAY: " + s);
      Trace.println("| X.F=" + x_f);
      Trace.println("| y=" + y);
    }
    AccessPathSet result = new AccessPathSet(s);
    AccessPathSet mayKillSet = mayKill(s, x_f, klimit);
    result.removeAll(mayKillSet);
    if (DEBUG) {
      Trace.println("| mayKillSet: " + mayKillSet);
      Trace.println("| Result after removing killSet: " + result);
    }
    AccessPathSet mayGenSet = mayGen(s, x_f, y, klimit);
    result.addAll(mayGenSet);
    if (DEBUG) {
      Trace.println("| MayGenSet: " + mayGenSet);
      Trace.println("| Result after adding genSet: " + result);
      Trace.println("---------------------------------------------------");
    }
    return result;
  }

  /**
   * compute the set of may-alias access paths in s that must be killed as a
   * result of an assignment to x.f
   * 
   * @param s
   * @param x_f
   * @param klimit
   * @return { path0.f.path1 \in s | must-alias(x,path0) }
   */
  AccessPathSet mayKill(AccessPathSet s, AccessPath x_f, int klimit) {
    AccessPathSet result = new AccessPathSet(APDictionary);
    PointerPathElement x = (PointerPathElement) x_f.getHead();

    if (x instanceof StaticFieldPathElement) {
      // handle static field
      assert x_f.length() == 1;
      return s.pathsWithPrefix(x_f);
    } else {
      assert x_f.length() == 2;
      // x_f is a reference to an instance field
      PathElement f = x_f.getElementAt(1);
      Set<AccessPath> xMustAliases = null; // Compute this once, and only if
      // needed.

      for (Iterator<AccessPath> it = s.iterator(); it.hasNext();) {
        AccessPath currPath = it.next();
        // TODO: Clean up a la mustKill, if this ever gets used.
        if (currPath.length() < 2) {
          continue;
        }
        if (klimit <= 2) {
          if (currPath.getElementAt(1).equals(f)) {
            PointerPathElement anchor = (PointerPathElement) currPath.getHead();
            if (aliased(anchor, x) == LOGICAL_ONE) {
              result.add(currPath);
            }
          }
        } else { // klimit > 2
          for (int i = 1; i < currPath.length(); i++) {
            // Does currPath contain field f?
            if (currPath.getElementAt(i).equals(f)) {
              // Is the part of currPath before f a must-alias of x?
              AccessPath currPathPrefix = APDictionary.findOrCreate(currPath.getPrefix(i));
              if (xMustAliases == null) {
                xMustAliases = getMustAliases(x);
              }
              if (xMustAliases.contains(currPathPrefix)) {
                result.add(currPath);
              }
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * compute the set of may-alias access paths that must be GENned as a result
   * of an assignment x.f := y
   * 
   * @param s
   *            may alias paths BEFORE the assignment
   * @return { path0.f.path1 | y.path1 \in s, and may-alias(x,path0) }
   */
  AccessPathSet mayGen(AccessPathSet s, AccessPath x_f, PathElement y, int klimit) {
    AccessPathSet result = new AccessPathSet(APDictionary);
    PointerPathElement x = (PointerPathElement) x_f.getHead();

    if (x instanceof StaticFieldPathElement) {
      // handle static field
      assert x_f.length() == 1;
      return gen(s, x, APDictionary.findOrCreate(y));
    } else {
      assert x_f.length() == 2;
      PathElement f = x_f.getElementAt(1);
      AccessPathSet pathsFromY = s.pathsFrom(y);
      // TODO: getMayAliases doesn't work.  Fix or clean up, if this ever gets used.
      // Compute x's may-aliases once, and only if needed.
      AccessPathSet xMayAliases = (pathsFromY.isEmpty() ? null : getMayAliases(x));

      for (Iterator<AccessPath> it = pathsFromY.iterator(); it.hasNext();) {
        AccessPath currPath = it.next();
        PathElement anchor = currPath.getHead();
        Assertions.productionAssertion(anchor.equals(y));

        for (Iterator<AccessPath> mayIt = xMayAliases.iterator(); mayIt.hasNext();) {
          AccessPath currMayAlias = mayIt.next();
          AccessPath newPath = APDictionary.concat(currMayAlias, f);
          if (currPath.length() > 1) {
            newPath = APDictionary.concat(newPath, currPath.getSuffix(1));
          }
          result.add(newPath);
        }
      }
    }
    return result;
  }

  /**
   * @param s
   * @return {p \in s | p.length() <= some k}
   */
  public AccessPathSet kLimitPollution(AccessPathSet s, int k) {
    AccessPathSet result = new AccessPathSet(getAPDictionary());
    for (Iterator<AccessPath> it = s.iterator(); it.hasNext();) {
      AccessPath ap = it.next();
      if (ap.length() <= k) {
        result.add(ap);
      }
    }
    return result;
  }

  /**
   * @param s
   * @return {p \in s | p.length() <= some k}
   */
  public AccessPathSet kLimit(AccessPathSet s, int accessPathKLimit) {
    if (accessPathKLimit > 0) {
      AccessPathSet result = new AccessPathSet(getAPDictionary());
      for (Iterator<AccessPath> it = s.iterator(); it.hasNext();) {
        AccessPath ap = it.next();
        if (ap.length() <= accessPathKLimit) {
          result.add(ap);
        }
      }
      return result;
    } else {
      return s;
    }
  }

  public static boolean containsArrayPath(AccessPathSet s) {
    for (Iterator<AccessPath> it = s.iterator(); it.hasNext();) {
      AccessPath ap = it.next();
      for (int i = 1; i < ap.length(); i++) {
        if (ap.getElementAt(i) instanceof ArrayContentsPathElement) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * note that this operation is performed in-place!
   * 
   * @param s
   */
  public static void removeArrayPaths(AccessPathSet s) {
    AccessPathSet toRemove = new AccessPathSet(s.getAPDictionary());
    outer: for (Iterator<AccessPath> it = s.iterator(); it.hasNext();) {
      AccessPath ap = it.next();
      for (int i = 1; i < ap.length(); i++) {
        if (ap.getElementAt(i) instanceof ArrayContentsPathElement) {
          toRemove.add(ap);
          continue outer;
        }
      }
    }
    s.removeAll(toRemove);
  }

  /**
   * 
   * @param x
   * @return Set <AccessPath> of may aliases of x
   *         <p>
   *         note:
   *         <ul>
   *         <li> x could be may-aliased with any arbitrary access path
   *         <li> result always contains x as a may alias of itself.
   *         </ul>
   */
  protected abstract AccessPathSet getMayAliases(PointerPathElement x);

  /**
   * @return Returns the aPDictionary.
   */
  public AccessPathDictionary getAPDictionary() {
    return APDictionary;
  }

}
