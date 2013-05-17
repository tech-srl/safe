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
package com.ibm.safe.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ContainerContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;

/**
 * A context selector based on the 0-1-container builder, that also
 * disambiguates other container classes relevant to our current set of
 * typestate properties
 * 
 * @author sfink
 * @author eyahav
 */
public class CustomContextSelector extends ContainerContextSelector {

  private final static TypeName JavaIoFilterInputStreamName = TypeName.string2TypeName("Ljava/io/FilterInputStream");

  public final static TypeReference JavaIoFilterInputStreamRef = TypeReference.findOrCreate(ClassLoaderReference.Primordial,
      JavaIoFilterInputStreamName);

  private final static TypeName JavaIoFilterOutputStreamName = TypeName.string2TypeName("Ljava/io/FilterOutputStream");

  public final static TypeReference JavaIoFilterOutputStreamRef = TypeReference.findOrCreate(ClassLoaderReference.Primordial,
      JavaIoFilterOutputStreamName);

  private final static TypeName JavaNetSocketName = TypeName.string2TypeName("Ljava/net/Socket");

  public final static TypeReference JavaNetSocketRef = TypeReference.findOrCreate(ClassLoaderReference.Primordial,
      JavaNetSocketName);

  private final IClass JavaIoFilterInputStream;

  private final IClass JavaIoFilterOutputStream;

  private final IClass JavaNetSocket;

  private final IClass JavaLangThread;

  private final static boolean SPLIT_USER_COLLECTIONS = false;

  private final static boolean SPLIT_SOCKETS = false;

  private final IClass JavaUtilCollection;

  private final IClass JavaUtilMap;

  /**
   * @param cha
   * @param delegate
   */
  public CustomContextSelector(ClassHierarchy cha, ZeroXInstanceKeys delegate) {
    super(cha, delegate);
    JavaIoFilterInputStream = cha.lookupClass(JavaIoFilterInputStreamRef);
    JavaIoFilterOutputStream = cha.lookupClass(JavaIoFilterOutputStreamRef);
    JavaNetSocket = cha.lookupClass(JavaNetSocketRef);
    JavaLangThread = cha.lookupClass(TypeReference.JavaLangThread);
    JavaUtilCollection = cha.lookupClass(TypeReference.JavaUtilCollection);
    JavaUtilMap = cha.lookupClass(TypeReference.JavaUtilMap);
    Assertions.productionAssertion(JavaIoFilterInputStream != null);
    Assertions.productionAssertion(JavaIoFilterOutputStream != null);
    Assertions.productionAssertion(JavaNetSocket != null);
    Assertions.productionAssertion(JavaLangThread != null);
    Assertions.productionAssertion(JavaUtilCollection != null);
    Assertions.productionAssertion(JavaUtilMap != null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.wala.ipa.callgraph.propagation.cfa.ContainerContextSelector#isContainer(com.ibm.wala.classLoader.IClass)
   */
  protected boolean isContainer(IClass C) {
    return super.isContainer(C) || isCustomContainer(C, getClassHierarchy());
  }

  /**
   * @param C
   * @return true iff C is a container class
   */
  private boolean isCustomContainer(IClass C, IClassHierarchy cha) {
    if (ClassLoaderReference.Primordial.equals(C.getClassLoader().getReference())
        && JavaIoFilterInputStream.getName().getPackage().equals(C.getReference().getName().getPackage())) {
      if (cha.isSubclassOf(C, JavaIoFilterInputStream) || cha.isSubclassOf(C, JavaIoFilterOutputStream)) {
        return true;
      }
    }

    if (SPLIT_SOCKETS) {
      if (ClassLoaderReference.Primordial.equals(C.getClassLoader().getReference())
          && JavaNetSocket.getName().getPackage().equals(C.getReference().getName().getPackage())) {
        if (cha.isSubclassOf(C, JavaNetSocket)) {
          return true;
        }
      }
    }
    if (SPLIT_USER_COLLECTIONS) {

      if (cha.implementsInterface(C, JavaUtilCollection) || cha.implementsInterface(C, JavaUtilMap)) {
        return true;
      }
    }
    return false;
  }
}
