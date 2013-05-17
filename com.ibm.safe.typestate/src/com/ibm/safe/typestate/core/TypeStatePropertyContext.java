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
 * Created on Dec 27, 2004
 */
package com.ibm.safe.typestate.core;

import java.util.Collection;
import java.util.Iterator;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;

/**
 * This provides a context from a typestate property. Technically, it keeps the
 * type-resolution machinery outside the limits of the typestate property
 * itself.
 * 
 * @author Eran Yahav (yahave)
 */
public class TypeStatePropertyContext {

  /**
   * Should the given instance type be tracked?
   * 
   * @param type
   *          - concrete type of a given instance
   * @return true of this type should be tracked, false otherwise
   * 
   *         DONE: in the future, should query type hierarchy to automatically
   *         identify when objects of subtypes of the property-type are being
   *         considered. [Eran:Dec-09-2004] TODO: this should be made more
   *         efficient via usage of class hierarchy API
   * 
   */
  public static boolean isTrackedType(IClassHierarchy classHierarchy, Collection<IClass> propertyTypes, IClass type) {
    boolean result = false;

    for (Iterator<IClass> iter = propertyTypes.iterator(); iter.hasNext() && result == false;) {
      IClass propertyType = iter.next();
      if (propertyType.isInterface()) {
        try {
          // result = classHierarchy.isSubclassOf(type, propertyType);
          if (!type.isInterface()) {
            result = type.getAllImplementedInterfaces().contains(propertyType);
          } else {
            result = (type.equals(propertyType) || type.getAllImplementedInterfaces().contains(propertyType));
          }
        } catch (Exception e) {
          result = false;
        }
      } else {
        result = classHierarchy.isSubclassOf(type, propertyType);
      }
    }
    return result;
  }

  /**
   * return the IClass of the given typeName
   * 
   * @param typeName
   *          - a typename used in the tracked typestate proeprty
   * @return an IClass object corresponding to the given typename note: an
   *         IClass may be either a class or an interface TODO: fix this to use
   *         efficient class lookup.
   */
  public static IClass getPropertyTrackedType(IClassHierarchy classHierarchy, String typeName) {

    /**
     * for some reason, the code below does not find the class, check with Steve
     */
    // TypeReference typeRef = TypeReference.findOrCreate(
    // AnalysisScope.getApplicationLoader(), typeName);
    //			
    // TypeReference.findOrCreate(
    // ClassLoaderReference.Extension, typeName);
    // if (typeRef == null) {
    // typeRef = TypeReference.findOrCreate(
    // ClassLoaderReference.Primordial, typeName);
    // }
    // if (typeRef != null) {
    // IClass theClass = classHierarchy.lookupClass(typeRef);
    // }
    for (IClass currentClass : classHierarchy) {
      if (currentClass.getName().toString().equals(typeName)) {
        return currentClass;
      }
    }
    return null;
  }

}