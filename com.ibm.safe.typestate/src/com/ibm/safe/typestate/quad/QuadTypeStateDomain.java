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
/*
 * Created on Dec 7, 2004
 */
package com.ibm.safe.typestate.quad;

import java.util.Iterator;

import com.ibm.safe.accesspath.AccessPath;
import com.ibm.safe.accesspath.AccessPathSet;
import com.ibm.safe.accesspath.AccessPathSetTransformers;
import com.ibm.safe.accesspath.LocalPathElement;
import com.ibm.safe.accesspath.PathElement;
import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.typestate.ap.TemporaryParameterPointerKey;
import com.ibm.safe.typestate.ap.must.MustAuxiliary;
import com.ibm.safe.typestate.ap.must.MustMerge;
import com.ibm.safe.typestate.ap.must.mustnot.MustMustNotAuxiliary;
import com.ibm.safe.typestate.ap.must.mustnot.MustMustNotMerge;
import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.safe.typestate.core.TypeStateDomain;
import com.ibm.safe.typestate.options.TypeStateOptions;
import com.ibm.safe.typestate.rules.ITypeStateDFA;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.util.debug.Assertions;

/**
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 * 
 *         a domain of a subject, a unique bit, a type state, and some auxiliary
 *         info
 */
public class QuadTypeStateDomain extends TypeStateDomain {

  private final static boolean VERBOSE = true;

  /**
   * This is used for debugging
   */
  private final PointerAnalysis pointsTo;

  /**
   * @param dfa
   *          governing type state automaton
   */
  public QuadTypeStateDomain(ITypeStateDFA dfa, TypeStateOptions options, PointerAnalysis pointsTo) {
    super(dfa, options);
    this.pointsTo = pointsTo;
  }

  /**
   * Add an Object to the set of mapped objects.
   * 
   * @return the integer to which the object is mapped.
   */
  public int add(InstanceKey instance, IDFAState state, boolean isUnique, Auxiliary aux) {
    QuadFactoid f = new QuadFactoid(instance, state, isUnique, aux);
    if (VERBOSE && getMappedIndex(f) == -1) {
      verbosePrint(f);
    }
    return add(f);
  }

  /**
   * do some verbose printing and expensive assertion checking
   */
  private void verbosePrint(QuadFactoid f) {

    MustAuxiliary m = (MustAuxiliary) f.aux;
    if (f.aux instanceof MustMustNotAuxiliary) {
      MustMustNotAuxiliary mn = (MustMustNotAuxiliary) f.aux;
      System.err.println("Factoid: " + add(f) + " " + m.getMustPaths().size() + " " + mn.getMustNotPaths().size());
      System.err.println(f);

      Trace.println(add(f) + " " + m.getMustPaths().size() + " " + mn.getMustNotPaths().size());
      Trace.println(f);
    } else {
      System.err.println(add(f) + " " + m.getMustPaths().size());
      System.err.println(f);

      Trace.println(add(f) + " " + m.getMustPaths().size());
      Trace.println(f);
    }

    if (m.isComplete() && m.getMustPaths().size() == 0) {
      Assertions.UNREACHABLE();
    }
    AccessPathSet must = m.getMustPaths();

    if (!m.isComplete() && AccessPathSetTransformers.containsArrayPath(must)) {
      Assertions.UNREACHABLE("don't support array paths with incomplete must");
    }

    if (m instanceof MustMustNotAuxiliary) {
      MustMustNotAuxiliary mn = (MustMustNotAuxiliary) f.aux;
      AccessPathSet mustNot = mn.getMustNotPaths();
      checkDisjoint(must, mustNot);
      checkConsistent(mustNot, f.instance);

      if (AccessPathSetTransformers.containsArrayPath(mustNot)) {
        Assertions.UNREACHABLE("don't support array paths in must not");
      }
      if (m.isComplete() && mustNot.size() > 0) {
        Assertions.UNREACHABLE("no point carrying mustNot in complete factoid");
      }

    }

    checkConsistent(must, f.instance);

  }

  private void checkConsistent(AccessPathSet s, InstanceKey ik) {
    for (Iterator<AccessPath> it = s.iterator(); it.hasNext();) {
      AccessPath ap = it.next();
      checkConsistent(ap, ik);
    }
  }

  private void checkConsistent(AccessPath ap, InstanceKey ik) {
    if (ap.length() == 1) {
      PathElement head = ap.getHead();
      if (head instanceof LocalPathElement) {
        PointerKey pk = ((LocalPathElement) head).getPointerKey();
        if (!(pk instanceof TemporaryParameterPointerKey)) {
          if (!pointsTo.getPointsToSet(pk).contains(ik)) {
            Assertions.UNREACHABLE();
          }
        }
      }
    }
  }

  private void checkDisjoint(AccessPathSet must, AccessPathSet mustNot) {
    for (Iterator<AccessPath> it = mustNot.iterator(); it.hasNext();) {
      AccessPath ap = it.next();
      if (must.contains(ap)) {
        Assertions.UNREACHABLE();
      }
    }
  }

  /**
   * find or create a factoid if it doesn't already exist
   */
  public int findOrCreate(InstanceKey instance, IDFAState state, boolean isUnique, Auxiliary aux) {
    return add(instance, state, isUnique, aux);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.TypeStateDomain#getIndexForStateDelta(com.ibm.safe
   * .typestate.base.BaseFactoid, com.ibm.safe.emf.typestate.IState)
   */
  public int getIndexForStateDelta(BaseFactoid inputFact, IDFAState succState) {
    QuadFactoid i = (QuadFactoid) inputFact;
    QuadFactoid f = new QuadFactoid(i.instance, succState, i.isUnique(), i.aux);
    if (VERBOSE && getMappedIndex(f) == -1) {
      verbosePrint(f);
    }
    return add(f);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.safe.typestate.TypeStateDomain#getIndexForInitialState(com.ibm.
   * wala.ipa.callgraph.propagation.InstanceKey)
   */
  public int getIndexForInitialState(InstanceKey ik) {
    Assertions.UNREACHABLE();
    return 0;
  }

  /**
   * a horrible hack TODO: fix the class hierarchy
   * 
   * @see com.ibm.wala.dataflow.IFDS.TabulationDomain#isWeakerThan(int, int)
   */
  public boolean isWeakerThan(int d1, int d2) {

    assert d1 != d2;

    if (d1 == 0) {
      return true;
    } else if (d2 == 0) {
      return false;
    } else {
      QuadFactoid f1 = (QuadFactoid) getMappedObject(d1);
      QuadFactoid f2 = (QuadFactoid) getMappedObject(d2);
      if (f1.aux instanceof MustMustNotAuxiliary) {
        return MustMustNotMerge.isWeaker(f1, f2);
      } else if (f1.aux instanceof MustAuxiliary) {
        return MustMerge.isWeaker(f1, f2);
      } else {
        return false;
      }
    }
  }
}