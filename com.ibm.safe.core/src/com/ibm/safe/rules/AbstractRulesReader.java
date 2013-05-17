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
package com.ibm.safe.rules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.safe.utils.DirectoryWalk;

abstract class AbstractRulesReader implements IRulesReader {

  private final static String ROOT_NOT_DIRECTORY = "Rules location is not a directory: ";

  private final Collection<IRule> rules = new Stack<IRule>();

  public final IRule[] getRules() {
    return (IRule[]) this.rules.toArray(new IRule[this.rules.size()]);
  }

  protected final void preDirectoryTraversal(final File rootDirectory) throws IOException {
    if (!rootDirectory.isDirectory()) {
      throw new IOException(ROOT_NOT_DIRECTORY + " " + rootDirectory.getPath());
    }
    collectRules(rootDirectory);
  }

  protected final void loadResources(final String ruleFile) throws IOException {
    List<IRule> rules = loadRulesFromFile(ruleFile);
    this.rules.addAll(rules);
  }

  private List<IRule> loadRulesFromFile(String ruleFile) {
    try {
      List<IRule> ruleList = new ArrayList<IRule>();
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(ruleFile);
      doc.getDocumentElement().normalize();
      NodeList typestateRules = doc.getElementsByTagName("typestateRule");
      for (int i = 0; i < typestateRules.getLength(); i++) {
        IRule curr = parseTypestateRule(typestateRules.item(i));
        if (curr != null) {
          curr.setFileName(ruleFile);
          ruleList.add(curr);
          assert !ruleList.isEmpty() : "rule not added";
        }
      }

      NodeList structRules = doc.getElementsByTagName("structuralRule");
      for (int i = 0; i < structRules.getLength(); i++) {
        IRule curr = parseStructuralRule(structRules.item(i));
        if (curr != null) {
          curr.setFileName(ruleFile);
          ruleList.add(curr);
        }
      }
      assert !ruleList.isEmpty() : "rule set is empty!";
      return ruleList;

    } catch (Exception e) {
      e.printStackTrace();
    }

    return Collections.emptyList();
  }

  private IRule parseStructuralRule(Node node) {
    return LightweightRuleParser.parseRule(node);
  }

  private TypestateRule parseTypestateRule(Node node) {
    return TypestateRuleParser.parseRule(node);
  }

  protected void collectRules(final File directory) throws IOException {
    final XMIFileVisitor visitor = new XMIFileVisitor();
    DirectoryWalk.walk(directory, new BasicXMLFilter(), visitor);
    final File[] xmlFiles = visitor.getXMIFiles();

    for (int i = 0; i < xmlFiles.length; i++) {
      loadResources(xmlFiles[i].getAbsolutePath());
    }
  }

}
