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

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.typestate.quad.Auxiliary;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.BimodalMutableIntSet;
import com.ibm.wala.util.intset.BitVectorIntSet;
import com.ibm.wala.util.intset.IntIterator;

/**
 * A set of non-empty access paths. This is implemented as a separate class to
 * allow later implementations to improve this by e.g., using BDDs to
 * efficiently encode sets of access paths.
 * 
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 * @author Alexey Loginov
 */
public class AccessPathSet implements Auxiliary {

  /**
   * Maximum width (size) of an AccessPathSet (no limit if the value is
   * negative). Enforced by methods add and addAll.
   */
  private static int maxSetWidth = -1;

  public static int getMaxSetWidth() {
    return maxSetWidth;
  }

  public static void setMaxSetWidth(int maxWidth) {
    maxSetWidth = maxWidth;
  }

  /**
   * canonical ids of the access paths in this set.
   */
  private BimodalMutableIntSet contents = new BimodalMutableIntSet();

  private final AccessPathDictionary APDictionary;

  public AccessPathSet(AccessPathDictionary APDictionary) {
    this.APDictionary = APDictionary;
  }

  public AccessPathSet(AccessPathSet baseSet) {
    this.APDictionary = baseSet.APDictionary;
    addAll(baseSet);
  }

  public AccessPathSet(AccessPathDictionary APDictionary, AccessPath ap) {
    this.APDictionary = APDictionary;
    add(ap);
  }

  private AccessPathSet(AccessPathDictionary dictionary, BimodalMutableIntSet set) {
    this.APDictionary = dictionary;
    this.contents = set;
  }

  public AccessPathSet pathsFrom(PathElement source) {
    BimodalMutableIntSet c = new BimodalMutableIntSet();
    c.addAllInIntersection(contents, APDictionary.getPathsFrom(source));
    return new AccessPathSet(APDictionary, c);
  }

  public AccessPathSet pathsWithPrefix(AccessPath prefixPath) {
    assert prefixPath != null : "cannot get paths from null prefix";

    AccessPathSet result = new AccessPathSet(APDictionary);
    for (Iterator<AccessPath> it = pathsFrom(prefixPath.getHead()).iterator(); it.hasNext();) {
      AccessPath ap = it.next();
      if (ap.hasPrefix(prefixPath)) {
        result.add(ap);
      }
    }
    return result;
  }

  /**
   * return paths with prefix other than the one provided.
   */
  public AccessPathSet pathsWithOtherRoot(PathElement root) {
    BimodalMutableIntSet c = new BimodalMutableIntSet(contents);
    c.removeAll(APDictionary.getPathsFrom(root));
    return new AccessPathSet(APDictionary, c);
  }

  /**
   * return paths with prefix other than the one provided.
   */
  public AccessPathSet pathsWithOtherRoots(Set<PathElement> roots) {
    assert roots != null : "cannot get paths from null roots";

    BimodalMutableIntSet c = new BimodalMutableIntSet(contents);
    for (Iterator<PathElement> it = roots.iterator(); it.hasNext();) {
      PathElement p = it.next();
      c.removeAll(APDictionary.getPathsFrom(p));
    }
    return new AccessPathSet(APDictionary, c);
  }

  /**
   * the size is the total number of actual values, regardless of starting-node
   * classification
   */
  public int size() {
    return contents.size();
  }

  public boolean isEmpty() {
    return contents.isEmpty();
  }

  /**
   * does the PathSet contain a give access path?
   * 
   * @return true if path is in the set, false otherwise
   */
  public boolean contains(AccessPath ap) {
    assert ap != null : "null cannot be a member of an AccessPathSet";
    return contents.contains(ap.id());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#iterator()
   */
  public Iterator<AccessPath> iterator() {
    return new Iterator<AccessPath>() {
      IntIterator it = contents.intIterator();

      public boolean hasNext() {
        return it.hasNext();
      }

      public AccessPath next() {
        return APDictionary.getAccessPath(it.next());
      }

      public void remove() {
        Assertions.UNREACHABLE();
      }
    };
  }

  /**
   * adds a path to the AccessPathSet
   * 
   * precondition: path is not empty
   * 
   * @param ap
   *          - access-path to be added
   * @return true when a path has been added, false otherwise
   */
  public boolean add(AccessPath ap) {

    assert ap != null : "AccessPathSet does not allow adding 'null' element";

    if (maxSetWidth >= 0) { // there is a set-size limit
      return (contents.size() < maxSetWidth ? contents.add(ap.id()) : false);
    } else { // no set-size limit
      return contents.add(ap.id());
    }
  }

  /**
   * removes a path from the AccessPathSet
   * 
   * precondition: path is not empty
   */
  public boolean remove(AccessPath ap) {
    assert ap != null : "AccessPathSet does not allow removing 'null' element";
    return contents.remove(ap.id());
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#containsAll(java.util.Collection)
   */
  public boolean containsAll(AccessPathSet c) {
    assert c != null : "cannot check containment with null collection";
    return size() >= c.size() && contents.containsAll(c.contents);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#addAll(java.util.Collection)
   */
  public boolean addAll(AccessPathSet c) {
    assert c != null : "cannot add elements of a null collection";

    if (maxSetWidth >= 0) { // there is a set-size limit
      int oldSize = contents.size();
      for (Iterator<AccessPath> iter = c.iterator(); iter.hasNext();) {
        add(iter.next());
      }
      return (contents.size() > oldSize);

    } else { // no set-size limit
      return contents.addAll(c.contents);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#retainAll(java.util.Collection)
   */
  public boolean retainAll(Collection<AccessPath> c) {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#removeAll(java.util.Collection)
   */
  public boolean removeAll(AccessPathSet c) {
    assert c != null : "cannot remove elements of a null collection";
    return contents.removeAll(c.contents);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#clear()
   */
  public void clear() {
    contents = new BimodalMutableIntSet();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object o) {
    if (!(o instanceof AccessPathSet)) {
      return false;
    } else {
      return contents.sameValue(((AccessPathSet) o).contents);
    }
  }

  /*
   * HashCode as a value!!! must be consistent with equals() TODO: cache?
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new BitVectorIntSet(contents).getBitVector().hashCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    StringBuffer result = new StringBuffer();
    for (Iterator<AccessPath> it = iterator(); it.hasNext();) {
      Object item = it.next();
      result.append(item);
      // result.append(item + " of class: " + item.getClass());
      result.append("\n");
    }
    return result.toString();
  }

  /**
   * @return Returns the aPDictionary.
   */
  public AccessPathDictionary getAPDictionary() {
    return APDictionary;
  }

}