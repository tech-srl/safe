/*******************************************************************************
 * Copyright (c) 2002 - 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
 /*
 * $Id: IReadFieldEventImpl.java,v 1.3 2010/09/02 01:08:06 eyahav Exp $
 */
package com.ibm.safe.dfa.events;



/**
 * <!-- begin-user-doc --> An event that happens when the TypeState property
 * type reads a specific field <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 * <li>{@link com.ibm.safe.dfa.events.IReadFieldEventImpl#getField <em>Field</em>}</li>
 * </ul>
 * </p>
 * 
 * @generated
 */
public class IReadFieldEventImpl extends IEventImpl implements IReadFieldEvent {
  /**
   * The default value of the '{@link #getField() <em>Field</em>}' attribute.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @see #getField()
   * @generated
   * @ordered
   */
  protected static final String FIELD_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getField() <em>Field</em>}' attribute.
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @see #getField()
   * @generated
   * @ordered
   */
  protected String field = FIELD_EDEFAULT;

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  protected IReadFieldEventImpl() {
    super();
  }

  public String getField() {
    return field;
  }

  public void setField(String newField) {
    field = newField;
  }

  /**
   * <!-- begin-user-doc --> <!-- end-user-doc -->
   * 
   * @generated
   */
  public String toString() {
    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (field: ");
    result.append(field);
    result.append(')');
    return result.toString();
  }

  public boolean equals(final Object rhsObject) {
    if (rhsObject == null)
      return false;
    if (!getClass().equals(rhsObject.getClass())) {
      return false;
    }
    if (this.field == null) {
      return (this.field == ((IReadFieldEventImpl) rhsObject).field) && super.equals(rhsObject);
    } else {
      return (super.equals(rhsObject) && this.field.equals(((IReadFieldEventImpl) rhsObject).field));
    }
  }

  public int hashCode() {
    return (this.field != null) ? this.field.hashCode() + super.hashCode() : super.hashCode();
  }

  public boolean match(final String elementToMatch) {
    return this.field.equals(elementToMatch);
  }

} 
