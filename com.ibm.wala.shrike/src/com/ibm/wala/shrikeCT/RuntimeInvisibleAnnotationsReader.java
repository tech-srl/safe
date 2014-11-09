/*******************************************************************************
 * Copyright (c) 2002,2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.wala.shrikeCT;

/**
 * This class reads RuntimeInvisibleAnnotations attributes.
 * 
 * @author sjfink
 */
public final class RuntimeInvisibleAnnotationsReader extends AnnotationsReader {

  public static final String attrName = "RuntimeInvisibleAnnotations";

  public RuntimeInvisibleAnnotationsReader(ClassReader.AttrIterator iter) throws InvalidClassFileException {
    super(iter, attrName);
  }
}