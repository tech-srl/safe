/*******************************************************************************
 * Copyright (c) 2002-2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.metrics;

public class ClassStatistics {

  public String className;

  public int numberOfInstanceFields;

  public int numberOfFinalInstanceFields;

  public int numberOfStaticFields;

  public int numberOfFinalStaticFields;

  public int numberOfMethods;

  public int numberOfSynchronizedMethods;

  public int numberOfStaticMethods;

  public long numberOfByteCodeLocs;

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("Class: " + className + "\n");
    result.append("---------------------------------\n");
    result.append("Instance fields: " + numberOfInstanceFields);
    result.append("\n");
    result.append("Final instance fields: " + numberOfFinalInstanceFields);
    result.append("\n");
    result.append("Static fields:" + numberOfStaticFields);
    result.append("\n");
    result.append("Final static fields: " + numberOfFinalStaticFields);
    result.append("\n");
    result.append("Methods: " + numberOfMethods);
    result.append("\n");
    result.append("Synchronized Methods: " + numberOfSynchronizedMethods);
    result.append("\n");
    result.append("Static Methods: " + numberOfStaticMethods);
    result.append("\n");
    result.append("LOBs: " + numberOfByteCodeLocs);
    result.append("\n");
    return result.toString();
  }

}