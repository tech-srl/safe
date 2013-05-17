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
package com.ibm.safe.internal.reporting;

import java.util.Comparator;

import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.reporting.message.SignatureUtils;

public final class FindingComparator implements Comparator<Message> {

  // --- Interface methods implementation

  public int compare(final Message leftObject, final Message rightObject) {
    // assert leftObject instanceof Message && rightObject instanceof Message;
    final Message leftMessage = leftObject;
    final Message rightMessage = rightObject;
    final int ruleComp = (leftMessage.toString().compareTo(rightMessage.toString()));
    if (ruleComp != 0) {
      return ruleComp;
    }
    final int classComp = SignatureUtils.getClassName(leftMessage.getLocation()).compareTo(
        SignatureUtils.getClassName(rightMessage.getLocation()));
    if (classComp != 0) {
      return classComp;
    }
    // TODO: Switch to using getLocationLineNumber() AND getByteCodeIndex() (if not -1).
    if (leftMessage.getLocation().getLocationLineNumber() == rightMessage.getLocation().getLocationLineNumber()) {
      return 0;
    } else {
      if (leftMessage.getLocation().getLocationLineNumber() < rightMessage.getLocation().getLocationLineNumber()) {
        return -1;
      } else {
        return 1;
      }
    }
  }

}
