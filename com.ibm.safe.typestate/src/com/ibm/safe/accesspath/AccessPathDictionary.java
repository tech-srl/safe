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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.SimpleVector;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.BitVectorIntSet;
import com.ibm.wala.util.intset.IntSet;

/**
 * @author sfink
 * @author eyahav
 * 
 * A dictionary used to canonicalize access paths
 */
public class AccessPathDictionary {

  /**
   * do paranoid assertion checking?
   */
  private final boolean PARANOID = false;

  /**
   * A mapping from APKey -> AccessPath
   */
  private final Map<APKey, AccessPath> map = HashMapFactory.make();

  /**
   * A mapping from integer (id) -> AccessPath. should be more efficient than
   * MutableMap ? since each AccessPath carries its unique id.
   */
  private SimpleVector<AccessPath> vector = new SimpleVector<AccessPath>();

  /**
   * Map : PathElement -> BitVectorIntSet, the set of access path ids that start
   * with a certain path element
   */
  private final Map<PathElement, BitVectorIntSet> startsWith = HashMapFactory.make();

  /**
   * Reset all state to free up space.
   */
  public void clear() {
    map.clear();
    vector = new SimpleVector<AccessPath>();
    startsWith.clear();
  }

  /**
   * @return canonical AccessPath object
   */
  public AccessPath findOrCreate(PathElement initialElement) {
    APKey k = new APKey(initialElement);
    AccessPath result = map.get(k);
    if (result == null) {
      int id = vector.getMaxIndex() + 1;
      result = new AccessPath(k, id);
      map.put(k, result);
      vector.set(id, result);
      findOrCreateStartsWith(initialElement).add(id);
    }
    return result;
  }

  /**
   * @param elements
   *            List<PathElement>
   * @return canonical AccessPath object
   */
  public AccessPath findOrCreate(List<PathElement> elements) {
    if (PARANOID) {
      for (Iterator<PathElement> it = elements.iterator(); it.hasNext();) {
        if (!(it.next() instanceof PathElement)) {
          Assertions.UNREACHABLE();
        }
      }
    }
    APKey k = new APKey(elements);
    AccessPath result = map.get(k);
    if (result == null) {
      int id = vector.getMaxIndex() + 1;
      result = new AccessPath(k, id);
      map.put(k, result);
      vector.set(id, result);
      findOrCreateStartsWith(result.getHead()).add(id);
    }
    return result;
  }

  BitVectorIntSet findOrCreateStartsWith(PathElement p) {
    BitVectorIntSet result = startsWith.get(p);
    if (result == null) {
      result = new BitVectorIntSet();
      startsWith.put(p, result);
    }
    return result;
  }

  /**
   * @author sfink
   * 
   * The canonical value of an access path
   */
  public class APKey {

    /**
     * list of path elements
     */
    private List<PathElement> pathElements;

    private final int hashCode;

    /**
     * create a new accesspath with an initial path element
     * 
     * @param initialElement -
     *            initial path element
     */
    APKey(PathElement initialElement) {
      pathElements = new ArrayList<PathElement>();
      pathElements.add(initialElement);
      if (PARANOID) {
        pathElements = Collections.unmodifiableList(pathElements);
      }
      hashCode = computeHashCode();
    }

    /**
     * create an accesspath from a list of elements
     * 
     * @param elements -
     *            list of PathElements
     */
    APKey(List<PathElement> elements) {
      assert elements != null :  "Cannot create accesspath from null list";
      pathElements = new ArrayList<PathElement>(elements);
      if (PARANOID) {
        pathElements = Collections.unmodifiableList(pathElements);
      }
      hashCode = computeHashCode();
    }

    private int computeHashCode() {
      // javadoc for List.hashCode() says the following:
      // Returns the hash code value for this list. The hash code of a list is
      // defined to be the result of the following calculation:
      //
      // hashCode = 1;
      // Iterator i = list.iterator();
      // while (i.hasNext()) {
      // Object obj = i.next();
      // hashCode = 31*hashCode + (obj==null ? 0 : obj.hashCode());
      // }

      return pathElements.hashCode();
    }

    /**
     * is the access path empty?
     * 
     * @return true when the path is empty, false otherwise
     */
    boolean isEmpty() {
      return pathElements.isEmpty();
    }

    /**
     * @return the pathElements
     */
    public List<PathElement> getPathElements() {
      return pathElements;
    }

    /**
     * get a prefix of an accesspath up to (not including) a given index
     * 
     * @param i -
     *            index up to (not including) which suffix should be taken
     * @return return a prefix of an accesspath from a given index (List<PathElement>)
     */
    public List<PathElement> getPrefix(int i) {
      assert i <= pathElements.size() :  "attempt to get prefix beyond path length";
      return new ArrayList<PathElement>(pathElements.subList(0, i));
    }

    /**
     * get a suffix of an accesspath from a given index
     * 
     * @param i -
     *            index from which suffix should be taken
     * @return return a suffix of an accesspath from a given index (List<PathElement>)
     */
    public List<PathElement> getSuffix(int i) {
      assert i < pathElements.size() : "attempt to get suffix beyond path length";
      return new ArrayList<PathElement>(pathElements.subList(i, pathElements.size()));
    }

    /**
     * is this path an anchored path? (i.e., does it start with a variable name)
     */
    public boolean isAnchored() {
      if (pathElements.isEmpty()) {
        return false;
      } else {
        PathElement pe = pathElements.get(0);
        return (pe.isAnchor());
      }
    }

    /**
     * get the path-element at the head of the accesspath
     * 
     * @return path-element at head of accesspath
     */
    public PathElement getHead() {
      return pathElements.get(0);
    }

    /**
     * length of access-path
     * 
     * @return length of access path
     */
    public int length() {
      return pathElements.size();
    }

    /**
     * get path element at the given index
     * 
     * @param i -
     *            index into access path
     * @return path-element at the given index
     */
    public PathElement getElementAt(int i) {
      return pathElements.get(i);
    }

    /**
     * return a string representation of the access path
     * 
     * @return human readable representation of the access path
     */
    public String toString() {
      StringBuffer result = new StringBuffer();
      for (Iterator<PathElement> it = pathElements.iterator(); it.hasNext();) {
        Object curr = it.next();
        result.append(curr.toString());
      }
      return result.toString();
    }

    /**
     * is this path equals to another object?
     * 
     * @param other -
     *            other object to be compared with
     * @return true when the other accesspath is equals to this one, false
     *         otherwise
     */
    public boolean equals(Object other) {
      if (!(other instanceof APKey)) {
        return false;
      }
      APKey otherPath = (APKey) other;
      if (pathElements != null) {
        return pathElements.equals(otherPath.pathElements);
      } else {
        return otherPath.pathElements == null;
      }

    }

    /**
     * return the hascode for the accesspath
     * 
     * @return accesspath hashcode
     */
    public int hashCode() {
      return hashCode;
    }

    /**
     * @param prefixPath
     * @return true iff this access-path starts with the given prefix
     */
    public boolean hasPrefix(AccessPath prefixPath) {
      if (length() < prefixPath.length()) {
        return false;
      } else {
        for (int i = 0; i < prefixPath.length(); i++) {
          if (!getElementAt(i).equals(prefixPath.getElementAt(i))) {
            return false;
          }
        }
        return true;
      }
    }

    /**
     * @return true iff this access-path ends with the given suffix
     */
    public boolean hasSuffix(AccessPath suffixPath) {
      if (length() < suffixPath.length()) {
        return false;
      } else {
        for (int i = 0; i < suffixPath.length(); i++) {
          if (!getElementAt(length() - i).equals(suffixPath.getElementAt(length() - i))) {
            return false;
          }
        }
        return true;
      }
    }
  }

  public AccessPath concat(AccessPath ap, List<PathElement> suffix) {
    if (PARANOID) {
      for (Iterator<PathElement> it = suffix.iterator(); it.hasNext();) {
        assert(it.next() instanceof PathElement);
      }
    }
    List<PathElement> l = new ArrayList<PathElement>(ap.key.pathElements);
    l.addAll(suffix);
    return findOrCreate(l);
  }

  public AccessPath concat(AccessPath ap, PathElement f) {
    List<PathElement> l = new ArrayList<PathElement>(ap.key.pathElements);
    l.add(f);
    return findOrCreate(l);
  }

  public AccessPath concat(PathElement p, AccessPath ap) {
    List<PathElement> l = new ArrayList<PathElement>();
    l.add(p);
    l.addAll(ap.key.pathElements);
    return findOrCreate(l);
  }

  public AccessPath concat(PathElement p1, PathElement p2) {
    List<PathElement> l = new ArrayList<PathElement>();
    l.add(p1);
    l.add(p2);
    return findOrCreate(l);
  }

  public AccessPath concat(PathElement p, List<PathElement> list) {
    List<PathElement> l = new ArrayList<PathElement>();
    l.add(p);
    l.addAll(list);
    return findOrCreate(l);
  }

  public AccessPath concat(AccessPath ap1, AccessPath ap2) {
    List<PathElement> l = new ArrayList<PathElement>(ap1.key.pathElements);
    l.addAll(ap2.key.pathElements);
    return findOrCreate(l);
  }

  public AccessPath getAccessPath(int i) {
    return (AccessPath) vector.get(i);
  }

  public IntSet getPathsFrom(PathElement source) {
    return findOrCreateStartsWith(source);
  }

}
