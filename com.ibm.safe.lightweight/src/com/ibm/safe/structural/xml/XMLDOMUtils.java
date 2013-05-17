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
 * Created on Dec 20, 2004
 */
package com.ibm.safe.structural.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class XMLDOMUtils {

  /**
   * (A useful utility method from IBM developerworks)
   */
  public static String getTextContents(Node node) {
    NodeList childNodes;
    StringBuffer contents = new StringBuffer();

    childNodes = node.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      if (childNodes.item(i).getNodeType() == Node.TEXT_NODE) {
        contents.append(childNodes.item(i).getNodeValue());
      }
    }
    return contents.toString();
  }

  /**
   * creates two elements: - one for the tag - one for the value and appends the
   * value as the child of the tag element returns a reference to the tag
   * element. Both elements are created in the document.
   */
  public static Element createTaggedElement(Document doc, String tag, String value) {
    Element tagElement = doc.createElement(tag);
    Text valueElement = doc.createTextNode(value);
    tagElement.appendChild(valueElement);
    return tagElement;
  }

  public static Element createTaggedElement(Document doc, String tag, int value) {
    return createTaggedElement(doc, tag, String.valueOf(value));
  }

}