/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.ibm.safe.rules;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LightweightRuleParser {

  public static StructuralRule parseRule(Node node) {
    StructuralRule r = new StructuralRule();

    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node curr = children.item(i);
      String nodeName = curr.getNodeName();
      if (nodeName.equals("query")) {
        r.setQuery(parseQuery(curr));
      } else if (nodeName.equals("reportLocationQuery")) {
        ReportLocation repLocQuery = parseReportLocationQuery(curr);
        r.setReportLocationQuery(repLocQuery);
      } else if (nodeName.equals("attributes")) {
        NamedNodeMap attr = curr.getAttributes();
        Node name = attr.getNamedItem("name");
        Node severity = attr.getNamedItem("severity");
        Node level = attr.getNamedItem("level");
        if (name != null) {
          r.setName(name.getNodeValue());
        }
        if (severity != null) {
          r.setSeverity(RuleSeverity.getByName(severity.getNodeValue()));
        }
        if (level != null) {
          r.setLevel(RuleLevel.getByName(level.getNodeValue()));
        }
      }
    }
    assert (r.getName() != null);
    return r;
  }

  private static String parseQuery(Node node) {
    NodeList children = node.getChildNodes();
    assert (children.getLength() == 1);
    Node curr = children.item(0);
    String result = curr.getNodeValue();
    return result;
  }

  private static ReportLocation parseReportLocationQuery(Node node) {
    NodeList children = node.getChildNodes();
    assert (children.getLength() == 1);
    Node curr = children.item(0);
    ReportLocation result = ReportLocation.getByName(curr.getNodeValue());
    return result;
  }

}
