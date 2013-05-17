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
package com.ibm.safe.typestate.rules;

import java.util.Collection;
import java.util.Iterator;

import com.ibm.safe.dfa.events.IDispatchEvent;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/**
 * Abstract supertypes for type state dfa implementations
 * 
 * @author sfink
 * @author yahave
 */
public abstract class AbstractTypeStateDFA implements ITypeStateDFA {

  /**
   * Governing class hierarchy
   */
  private final IClassHierarchy cha;

  /**
   * The types of the property, set by external resolution. A collection of
   * IClass objecs; For typeStateProperty this is a singelton For multiple
   * objects propoerties this may contain more than one type
   * 
   */
  private final Collection<IClass> types;

  /**
   * does this dfa observe the program exit event?
   */
  private boolean observesProgramExit;

  /**
   * does this dfa observe the object death event?
   */
  private boolean observesObjectDeath;

  protected AbstractTypeStateDFA(IClassHierarchy cha, Collection<IClass> types) {
    this.cha = cha;
    this.types = types;
  }

  /**
   * Does the property receive (i.e., understands) the given event ?
   * 
   * @param m
   *            the target of a dispatch event
   * @return True if event is supported by automaton (in its alphabet)
   */
  public boolean receives(final IMethod m) {
    boolean isType = false;
    IClass c = m.getDeclaringClass();
    for (Iterator<IClass> it = types.iterator(); it.hasNext();) {
      IClass t = it.next();
      if (t.isInterface()) {
        if (cha.implementsInterface(c, t)) {
          isType = true;
          break;
        }
      } else {
        if (cha.isSubclassOf(c, t) || cha.isSubclassOf(t, c)) {
          isType = true;
          break;
        }
      }
    }
    if (isType) {
      return matchDispatchEvent(m.getSignature()) != null;
    } else {
      return false;
    }
  }

  /**
   * Type has to be resolved and set by the external client who has knowledge of
   * the class-hierarchy.
   * 
   * @return The types on which TypeState automaton is defined.
   */
  public Collection<IClass> getTypes() {
    return this.types;
  }

  /**
   * Initializes the type on which TypeState automaton is defined.
   * 
   * @param aType
   *            The type to set.
   */
  public void addType(final IClass aType) {
    this.types.add(aType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.ITypeStateDFA#matchDispatchEvent(java.lang.String)
   */
  public IEvent matchDispatchEvent(CGNode caller, final String sig) {
    return match(IDispatchEvent.class, sig);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.ITypeStateDFA#matchDispatchEvent(java.lang.String)
   */
  public IEvent matchDispatchEvent(final String sig) {
    return match(IDispatchEvent.class, sig);
  }

  public abstract IEvent match(final Class eventClass, final String param);

  /**
   * @return Returns the observesProgramExit.
   */
  public boolean observesProgramExit() {
    return observesProgramExit;
  }

  /**
   * @param observesProgramExit
   *            The observesProgramExit to set.
   */
  public void setObservesProgramExit(boolean observesProgramExit) {
    this.observesProgramExit = observesProgramExit;
  }

  /**
   * @return Returns the observesObjectDeath.
   */
  public boolean observesObjectDeath() {
    return observesObjectDeath;
  }

  /**
   * @param observesObjectDeath
   *            The observesObjectDeath to set.
   */
  public void setObservesObjectDeath(boolean observesObjectDeath) {
    this.observesObjectDeath = observesObjectDeath;
  }

  protected IClassHierarchy getClassHierarchy() {
    return cha;
  }

}
