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
package com.ibm.safe.reporting.message;

import org.eclipse.jdt.core.Signature;

public final class SignatureUtils {
  public static final String CLINIT_MSG = "Static initialization part";

  public static String getClassName(final Location location) {
    final String className = location.getLocationClass();
    return className.substring(1).replaceAll("[/$]", ".");
  }

  public static String getMethodSignature(final MethodLocation location, final boolean withReturnType) {
    String methodSignature = location.getLocationMethodSignature().replace('/', '.');
    if (methodSignature.length() == 0)
      return methodSignature;

    final int signatureStartIndex = methodSignature.indexOf('(');
    if (signatureStartIndex < 0) {
      return methodSignature;
    }

    String methodName = methodSignature.substring(0, signatureStartIndex);
    methodName = methodName.substring(methodName.lastIndexOf('.') + 1);

    if (methodName.equals("<clinit>")) { //$NON-NLS-1$
      return CLINIT_MSG;
    }

    boolean includeReturnType = withReturnType;
    if (methodName.equals("<init>")) { //$NON-NLS-1$
      final String className = getClassName(location);
      methodName = className.substring(className.lastIndexOf('.') + 1);
      includeReturnType = false;
    }
    methodSignature = methodSignature.substring(signatureStartIndex);
    return Signature.toString(methodSignature, methodName, null /* parameterNames */, true /* fullyQualifiedName */,
        includeReturnType);
  }

}
