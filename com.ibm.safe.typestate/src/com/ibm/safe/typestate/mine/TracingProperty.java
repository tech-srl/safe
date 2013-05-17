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
package com.ibm.safe.typestate.mine;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.safe.dfa.IDFAState;
import com.ibm.safe.dfa.events.IDispatchEvent;
import com.ibm.safe.dfa.events.IDispatchEventImpl;
import com.ibm.safe.dfa.events.IEvent;
import com.ibm.safe.dfa.events.IObjectDeathEvent;
import com.ibm.safe.dfa.events.IProgramExitEventImpl;
import com.ibm.safe.typestate.merge.AbstractUnification;
import com.ibm.safe.typestate.rules.AbstractTypeStateDFA;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.debug.Assertions;

/**
 * A typestate "property" that builds an automaton on the fly.
 * 
 * Every state of the TracingProperty DFA is an AbstractEventTrace. Upon a call
 * to successor, a new state of the DFA is created on-the-fly, which contains an
 * extended AbstractEventTrace.
 * 
 * The name AbstractEventTrace is misleading, as this may represent a DFA that
 * is the union of multiple traces.
 * 
 * @author eyahav, sfink
 * 
 */
public class TracingProperty extends AbstractTypeStateDFA {

  private IDFAState initialState;

  /**
   * If true, then the identifier for a state is a selector (e.g. write(I)V and
   * write(II)V are distinct states). If fase, then the identifier for a state
   * is the method name (e.g. only one "write" state). the latter choice will be
   * more efficient but is less precise;
   */
  public static final boolean SELECTOR_EVENTS = false;

  /**
   * String (event name) -> IEvent
   */
  private final Map<String, IEvent> eventName2Event = HashMapFactory.make();

  /**
   * @param klasses
   *            Collection<IClass>
   */
  public TracingProperty(IClassHierarchy cha, Collection<IClass> klasses) {
    super(cha, klasses);
    populateEventMap(klasses);
  }

  private void populateEventMap(Collection<IClass> klasses) {
    for (Iterator<IClass> it = klasses.iterator(); it.hasNext();) {
      IClass klass = it.next();
      populateEventMap(klass);
    }
  }

  private void populateEventMap(IClass klass) {
    try {
      for (Iterator<IMethod> it = klass.getAllMethods().iterator(); it.hasNext();) {
        IMethod m = it.next();
        if (isInterestingMethod(m)) {
          String name = getEventName(m);
          if (eventName2Event.get(name) == null) {
            IEvent newEvent = new IDispatchEventImpl();
            newEvent.setName(name);
            eventName2Event.put(name, newEvent);
          }
        }
      }
    } catch (Exception e) {
      Assertions.UNREACHABLE();
    }
  }

  /**
   * @return the String representing the event of a call to metho dm
   */
  private String getEventName(IMethod m) {
    return SELECTOR_EVENTS ? m.getSelector().toString() : m.getName().toString();
  }

  private boolean isInterestingMethod(IMethod m) {
    if (isJavaLangObjectMethod(m) || m.isStatic() || m.isInit()) {
      return false;
    }
    if (m.getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)) {
      return !(m.isPrivate() || m.isProtected());
    } else {
      return m.isPublic();
    }
  }

  private boolean isJavaLangObjectMethod(IMethod m) {
    IClass javaLangObject = getClassHierarchy().lookupClass(TypeReference.JavaLangObject);
    return getClassHierarchy().resolveMethod(javaLangObject, m.getSelector()) != null;
  }

  public void createInitial(AbstractUnification merger) {
    initialState = new AbstractHistory(merger);
  }

  public IDFAState initial() {
    assert(initialState != null);
    return initialState;
  }

  /**
   * @param signature
   *            should be a method signature
   */
  private IEvent getEvent(final String signature) {
    String selector = parseSignatureForEventName(signature);
    return eventName2Event.get(selector);
  }

  private String parseSignatureForEventName(final String signature) {
    try {
      if (SELECTOR_EVENTS) {
        return parseForName(signature) + parseForDescriptor(signature);
      } else {
        return parseForName(signature);
      }
    } catch (WalaException e) {
      e.printStackTrace();
      Assertions.UNREACHABLE();
      return null;
    }
  }

  public static String parseForName(String sig) throws WalaException {
    int lastDot = sig.lastIndexOf('.');
    if (lastDot == -1) {
      throw new WalaException("ill-formatted method signature: " + sig);
    }
    int openParen = sig.indexOf('(');
    if (openParen == -1) {
      throw new WalaException("ill-formatted method signature: " + sig);
    }
    return sig.substring(lastDot + 1, openParen);
  }

  public static String parseForDescriptor(String sig) throws WalaException {
    int openParen = sig.indexOf('(');
    if (openParen == -1) {
      throw new WalaException("ill-formatted method signature: " + sig);
    }
    return sig.substring(openParen);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.AbstractTypeStateDFA#match(java.lang.Class,
   *      java.lang.String)
   */
  public IEvent match(final Class eventClass, final String param) {
    if (eventClass.equals(IProgramExitEventImpl.class)) {
      Assertions.UNREACHABLE();
      return null;
    } else {
      return getEvent(param);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.AbstractTypeStateDFA#matchDispatchEvent(com.ibm.wala.ipa.callgraph.CGNode,
   *      java.lang.String)
   */
  public IEvent matchDispatchEvent(CGNode caller, String sig) {
    if (caller.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)) {
      return getEvent(sig);
    } else {
      return null;
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.safe.typestate.ITypeStateDFA#successor(com.ibm.safe.typestate.IDFAState,
   *      com.ibm.safe.emf.typestate.IEvent)
   */
  public IDFAState successor(final IDFAState state, final IEvent event) {
    assert(state instanceof AbstractHistory);
    
    if (event instanceof IDispatchEvent) {
      return ((AbstractHistory) state).extend(event.getName());
    } else if (event instanceof IObjectDeathEvent) {
      return ((AbstractHistory) state).exit();
    } else if (event instanceof IProgramExitEventImpl) {
      return ((AbstractHistory) state).exit();
    } else {
      Assertions.UNREACHABLE(event.getClass().toString());
      return null;
    }
  }

  public Set predecessors(IDFAState state, IEvent automatonLabel) {
    Assertions.UNREACHABLE();
    return null;
  }

  /**
   * name required for reporting statistics
   */
  public String getName() {
    return "TracingProperty";
  }

  public boolean receives(IMethod m) {
    return getEvent(m.getSignature()) != null;
  }

  public boolean observesObjectDeath() {
    return true;
  }

  public boolean observesProgramExit() {
    return true;
  }
}