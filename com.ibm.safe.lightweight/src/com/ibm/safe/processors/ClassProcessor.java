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
 * Created on Jan 22, 2005
 */
package com.ibm.safe.processors;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.util.CancelException;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public interface ClassProcessor {
  /**
   * process method, where actual class processing is done
   * 
   * @param klass
   * @throws CancelException 
   */
  public void process(IClass klass) throws CancelException;

  /**
   * process prolog, allows performing initialization actions before the
   * processing (process) method is called
   * 
   * @param klass
   */
  public void processProlog(IClass klass);

  /**
   * process epilog, allows performing actions after the processing (process)
   * method is called (e.g., processing results)
   * 
   * @param klass
   */
  public void processEpilog(IClass klass);

  public void addMethodProcessor(MethodProcessor mp);

  public Object getResult();
}