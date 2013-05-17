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

import java.util.ArrayList;
import java.util.List;

public class TraceContainer {

  List<AbstractTrace> traces = new ArrayList<AbstractTrace>();
  
  public void add(AbstractTrace trace) {
    traces.add(trace);
  }

  public String asXMLString() {
    StringBuffer result = new StringBuffer();
    for (AbstractTrace t : traces) {
     result.append(t.getDfa().asGraph().toString()); 
    }
    return result.toString();
  }

  public static TraceContainer readFromXMLString(String tcxml) {
    TraceContainer tc = new TraceContainer();
    
    return tc;
  }
  
}
