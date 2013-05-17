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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.safe.dfa.IDFA;
import com.ibm.safe.typestate.base.BaseFactoid;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.MapUtil;

/**
 * @author sfink
 * @author Sharon Shoham
 * @author Eran Yahav
 * 
 *         An object to manage reports from abstract trace mining
 */
public class TraceReporter {

  private final static boolean VERBOSE = true;

  private final String outputDirectory;

  private final String programName;

  private final String type;

  private final String filename;

  private final String solver;

  private final String merger;

  public TraceReporter(String solver, String merger, String type, String programName, String outputDirectory, String filename) {
    this.solver = solver;
    this.merger = merger;
    this.type = type;
    this.programName = programName;
    this.outputDirectory = outputDirectory;
    this.filename = filename;
  }

  /**
   * Map: IDFA -> Collection<InstanceKey>
   */
  Map<IDFA, Set<InstanceKey>> map = HashMapFactory.make();

  /**
   * Record a particular factoid, holding an abstract trace, reaches the program
   * exit.
   */
  public void record(BaseFactoid inputFact) {
    if (VERBOSE) {
      System.err.println("Reporting trace:");
      System.err.println(inputFact.toString());
    }
    AbstractHistory t = (AbstractHistory) inputFact.state;
    Set<InstanceKey> instances = MapUtil.findOrCreateSet(map, t.getDfa());
    instances.add(inputFact.instance);
  }

  /**
   * Persist the collected factoids to a file.
   * 
   * @throws WalaException
   */
  public void persist() throws WalaException {
    TraceContainer container = new TraceContainer();
    for (Iterator<Map.Entry<IDFA, Set<InstanceKey>>> it = map.entrySet().iterator(); it.hasNext();) {
      Map.Entry<IDFA, Set<InstanceKey>> e = it.next();
      IDFA dfa = e.getKey();
      Set<InstanceKey> instances = e.getValue();
      AbstractTrace trace = makeTrace(dfa, instances);
      container.add(trace);
    }
    writeToFile(container);
  }

  private void writeToFile(TraceContainer container) throws WalaException {
    assert container != null;

    File f = new File(outputDirectory + File.separator + filename);
    try {
      if (!f.exists()) {
        File parent = f.getParentFile();
        if (parent != null && !parent.exists()) {
          parent.mkdirs();
        }
        f.createNewFile();
      }

      PrintWriter out = new PrintWriter(new FileOutputStream(f));
      out.write(container.asXMLString());
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
      throw new WalaException(getClass() + " failure to write to " + f.getAbsolutePath());
    }

    System.out.println("trace results have been created at " + f.getAbsolutePath());
  }

  private AbstractTrace makeTrace(IDFA dfa, Set<InstanceKey> instances) {
    AbstractTrace trace = new AbstractTrace();
    trace.setDfa(TracePersist.toEMF(dfa));
    trace.setProgram(programName);
    trace.setSolver(solver);
    trace.setMerger(merger);
    trace.setType(type);
    trace.addInstances(instances);
    return trace;
  }
}