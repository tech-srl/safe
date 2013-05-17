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

import java.util.List;

import com.ibm.safe.accesspath.AccessPathDictionary.APKey;

/**
 * Access path is a sequence of PathElements (order of elements is important).
 * 
 */
public class AccessPath {

  /**
   * The key holds the state of this Access Path
   */
  AccessPathDictionary.APKey key;

  /**
   * Each access path has a unique id, managed by the AccessPathDictionary
   */
  private final int id;

  protected AccessPath(APKey key, int id) {
    this.key = key;
    this.id = id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.accesspath.AccessPathDictionary.APKey#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    // THESE ARE CANONICAL!!!
    return this == other;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.accesspath.AccessPathDictionary.APKey#getElementAt(int)
   */
  public PathElement getElementAt(int i) {
    return key.getElementAt(i);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.accesspath.AccessPathDictionary.APKey#getHead()
   */
  public PathElement getHead() {
    return key.getHead();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.accesspath.AccessPathDictionary.APKey#hashCode()
   */
  public int hashCode() {
    return id * 8293;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.accesspath.AccessPathDictionary.APKey#hasPrefix(com.ibm.safe.accesspath.AccessPath)
   */
  public boolean hasPrefix(AccessPath prefixPath) {
    return key.hasPrefix(prefixPath);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.accesspath.AccessPathDictionary.APKey#hasSuffix(com.ibm.safe.accesspath.AccessPath)
   */
  public boolean hasSuffix(AccessPath suffixPath) {
    return key.hasSuffix(suffixPath);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.accesspath.AccessPathDictionary.APKey#isAnchored()
   */
  public boolean isAnchored() {
    return key.isAnchored();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.accesspath.AccessPathDictionary.APKey#length()
   */
  public int length() {
    return key.length();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.accesspath.AccessPathDictionary.APKey#toString()
   */
  public String toString() {
    return key.toString();
  }

  public boolean isEmpty() {
    return key.isEmpty();
  }

  public List<PathElement> getPrefix(int i) {
    return key.getPrefix(i);
  }

  public List<PathElement> getSuffix(int i) {
    return key.getSuffix(i);
  }

  /**
   * @return Returns the key for this access path
   */
  public APKey key() {
    return key;
  }

  /**
   * @return Returns the unique integer identifier for this access path
   */
  public int id() {
    return id;
  }
}
