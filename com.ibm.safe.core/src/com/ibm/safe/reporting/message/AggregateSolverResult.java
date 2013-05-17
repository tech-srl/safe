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
package com.ibm.safe.reporting.message;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;

/**
 * @author Eran Yahav (yahave)
 * @author Stephen Fink
 * 
 * Results from a separating solver; a collection of SafeSolverResult. We also
 * maintain a set of potential instances to be processed to complete the
 * construction of this result, and a set of instances that have been already
 * processed, or skipped.
 * 
 * We are keeping actual collections an not just numbers for tracibility. This
 * can be later optimized to only saving the counts, but the overhead of keeping
 * actual collections seems to be negligible and justified by ability to see how
 * instance-keys progress through the system.
 * 
 */
public class AggregateSolverResult implements ISolverResult {
  /**
   * instances that should be potentially processed to construct this full
   * result
   */
  private Set<InstanceKey> potentialInstances = HashSetFactory.make();

  /**
   * InstanceKey -> <SafeSolverResult>
   */
  private Map<InstanceKey, ISolverResult> instanceResults = HashMapFactory.make();

  /**
   * instances that were not checked, but skipped (due to check with benign
   * oracle)
   */
  private Set<InstanceKey> skippedInstances = HashSetFactory.make();

  /**
   * if this is frequently called, should cache expanded set of results
   * 
   * @reutrn a set of messages which is the union of messages in all aggregated
   *         results
   */
  public Set<Message> getMessages() {
    Set<Message> messages = HashSetFactory.make();
    for (Iterator<ISolverResult> it = instanceResults.values().iterator(); it.hasNext();) {
      ISolverResult res = it.next();
      messages.addAll(res.getMessages());
    }
    return messages;
  }

  public void addMessages(Set<? extends Message> messageSet) {
    throw new RuntimeException("operation not supported");
  }

  /**
   * allow solver to add a result for a processed instance
   */
  public void addInstanceResult(InstanceKey instance, ISolverResult result) {
    instanceResults.put(instance, result);
  }

  public Iterator<InstanceKey> iterateInstances() {
    return instanceResults.keySet().iterator();
  }

  public ISolverResult getInstanceResult(InstanceKey key) {
    return instanceResults.get(key);
  }

  /**
   * 
   * @param instance
   */
  public void addSkippedInstance(InstanceKey instance) {
    skippedInstances.add(instance);
  }

  /**
   * @param instances
   */
  public void addPotentialInstances(Collection<InstanceKey> instances) {
    assert instances != null;
    potentialInstances.addAll(instances);
  }

  /**
   * @return number of processed instances
   */
  public int processedInstancesNum() {
    return instanceResults.size();
  }

  /**
   * @return total number of potential instances to be processed in this result
   */
  public int totalInstancesNum() {
    return potentialInstances.size();
  }

  /**
   * @return number of instances that have been skipped
   */
  public int skippedInstances() {
    return skippedInstances.size();
  }

  /**
   * @return string of all messages in this result
   */
  public String toString() {
    Set<Message> messages = getMessages();
    StringBuffer result = new StringBuffer();
    for (Iterator<Message> it = messages.iterator(); it.hasNext();) {
      result.append(it.next().toString());
      result.append("\n");
    }
    return result.toString();
  }
}
