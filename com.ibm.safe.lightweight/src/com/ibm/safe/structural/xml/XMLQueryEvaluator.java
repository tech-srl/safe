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
 * Created on Dec 14, 2004
 */
package com.ibm.safe.structural.xml;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.safe.reporting.message.Location;
import com.ibm.safe.rules.RuleLevel;
import com.ibm.safe.rules.StructuralRule;
import com.ibm.safe.structural.impl.StructuralMessage;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class XMLQueryEvaluator {

  protected String filename = null;

  public static StructuralMessage[] execute(StructuralRule rule, Document doc) throws Exception {
    final Collection<StructuralMessage> messages = new ArrayList<StructuralMessage>(10);
    final XPath xpath = XPathFactory.newInstance().newXPath();
    final XPathExpression xpathExpression = xpath.compile(rule.getQuery());
    final NodeList nodeList = (NodeList) xpathExpression.evaluate(doc, XPathConstants.NODESET);
    for (int i = 0, size = nodeList.getLength(); i < size; ++i) {
      final Node ithNode = nodeList.item(i);
      final Node methodNode = getMethodNode(ithNode);

      final String repLoc = rule.getReportLocationQuery().getName().toLowerCase();
      boolean found = false;
      String locationString = null;
      for (Node child = methodNode.getFirstChild(); child != null && !found; child = child.getNextSibling()) {
        if (child.getNodeName().equals(repLoc)) {
          found = true;
          locationString = XMLDOMUtils.getTextContents(child);
        }
      }

      int lineNumber = -1;
      final Node lineAttrNode = ithNode.getAttributes().getNamedItem("line");
      if (lineAttrNode != null) {
        lineNumber = Integer.valueOf(lineAttrNode.getNodeValue()).intValue();
      }

      Location location = null;
      if (rule.getLevel() == RuleLevel.CLASS_LEVEL_LITERAL) {
        location = Location.createClassLocation(locationString, lineNumber);
      } else if (rule.getLevel() == RuleLevel.METHOD_LEVEL_LITERAL) {
        location = Location.createMethodLocation(locationString, lineNumber);
      } else {
        location = Location.createUnknownLocation();
      }

      messages.add(new StructuralMessage(rule, location));
    }
    return messages.toArray(new StructuralMessage[messages.size()]);
  }

  private static Node getMethodNode(final Node resultNode) {
    if (resultNode.getNodeName().equals("method") || resultNode.getNodeName().equals("class")) {
      return resultNode;
    } else {
      return getMethodNode(resultNode.getParentNode());
    }
  }
}