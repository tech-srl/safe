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
package com.ibm.safe.internal.entrypoints;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.core.Signature;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.ibm.safe.internal.exceptions.SetUpException;

public final class XMLEntryPointsReader implements IEntryPointsReader {

  public XMLEntryPointsReader(final File xmlFile) throws ParserConfigurationException, SAXException, Exception, SetUpException {
    collectEntryPoints(createDocument(xmlFile));
  }

  // --- Interface methods implementation

  public EntryPointDefinition[] getEntryPointDefinitions() {
    return this.entryPoints.toArray(new EntryPointDefinition[this.entryPoints.size()]);
  }

  // --- Private code

  private EntryPointDefinition collectEntryPoint(final Node entryPointNode) {
    final NodeList childNodes = entryPointNode.getChildNodes();
    final EntryPointDefinition entryPointDef = new EntryPointDefinition();
    for (int i = childNodes.getLength(); --i >= 0;) {
      final Node childNode = childNodes.item(i);
      if (childNode.getNodeType() == Node.ELEMENT_NODE) {
        collectEntryPointElement(entryPointDef, childNode);
      }
    }
    if (entryPointDef.getMethodName() == null) {
      entryPointDef.setMethodName(MainClassesEntryPointsReader.MAIN_METHOD_NAME);
      entryPointDef.setMethodDescriptor(MainClassesEntryPointsReader.MAIN_METHOD_DESCRIPTOR);
    } else {
      entryPointDef.setMethodDescriptor(createMethodDescriptor());
    }
    return entryPointDef;
  }

  private void collectEntryPointElement(final EntryPointDefinition entryPointDef, final Node entryPointElement) {
    if (CLASS_NAME_TAG.equals(entryPointElement.getNodeName())) {
      entryPointDef.setClassName(entryPointElement.getFirstChild().getNodeValue().trim());
    } else if (METHOD_NAME_TAG.equals(entryPointElement.getNodeName())) {
      entryPointDef.setMethodName(entryPointElement.getFirstChild().getNodeValue().trim());
    } else if (PARAMETERS_TYPE_TAG.equals(entryPointElement.getNodeName())) {
      final NodeList childNodes = entryPointElement.getChildNodes();
      for (int i = 0, size = childNodes.getLength(); i < size; ++i) {
        final Node childNode = childNodes.item(i);
        if (childNode.getNodeType() == Node.ELEMENT_NODE) {
          final String value = childNode.getFirstChild().getNodeValue().trim();
          if (value.length() > 0) {
            this.parametersTypes.add(Signature.createTypeSignature(value, true /* isResolved */));
          }
        }
      }
    } else if (RETURN_TYPE_TAG.equals(entryPointElement.getNodeName())) {
      this.returnType = Signature
          .createTypeSignature(entryPointElement.getFirstChild().getNodeValue().trim(), true /* isResolved */);
    }
  }

  private void collectEntryPoints(final Document xmlDocument) {
    final Node root = xmlDocument.getDocumentElement();
    final NodeList childNodes = root.getChildNodes();
    for (int i = childNodes.getLength(); --i >= 0;) {
      final Node childNode = childNodes.item(i);
      if (childNode.getNodeType() == Node.ELEMENT_NODE) {
        this.entryPoints.add(collectEntryPoint(childNode));
      }
    }
  }

  private Document createDocument(final File xmlFile) throws ParserConfigurationException, SAXException, IOException,
      SetUpException {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setValidating(true);
    factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
    factory.setAttribute(JAXP_SCHEMA_SOURCE, getXMLSchemaFile());
    DocumentBuilder builder = factory.newDocumentBuilder();
    builder.setErrorHandler(new BasicHandler());
    return builder.parse(xmlFile);
  }

  private String createMethodDescriptor() {
    final StringBuffer buf = new StringBuffer();
    buf.append('(');
    for (Iterator<String> iter = this.parametersTypes.iterator(); iter.hasNext();) {
      buf.append(iter.next().replace('.', '/'));
    }
    buf.append(')');

    if (this.returnType == null) {
      buf.append(Signature.C_VOID);
    } else {
      buf.append(this.returnType);
    }

    return buf.toString();
  }

  private File getXMLSchemaFile() throws SetUpException {
    final URL url = getClass().getClassLoader().getResource(XML_SCHEMA_FILENAME);
    if (url == null) {
      throw new SetUpException("Unable to find XML schema named " + XML_SCHEMA_FILENAME);
    }
    return new File(url.getFile());
  }

  private static class BasicHandler extends DefaultHandler {

    // --- Overridden methods

    public void error(final SAXParseException except) throws SAXException {
      throw except;
    }

  }

  private final Collection<EntryPointDefinition> entryPoints = new ArrayList<EntryPointDefinition>(10);

  private final Collection<String> parametersTypes = new ArrayList<String>(10);

  private String returnType;

  private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$

  private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage"; //$NON-NLS-1$

  private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource"; //$NON-NLS-1$

  private static final String XML_SCHEMA_FILENAME = "entrypoints_schema.xsd"; //$NON-NLS-1$

  private static final String CLASS_NAME_TAG = "class-name"; //$NON-NLS-1$

  private static final String METHOD_NAME_TAG = "method-name"; //$NON-NLS-1$

  private static final String PARAMETERS_TYPE_TAG = "parameters-type"; //$NON-NLS-1$

  private static final String RETURN_TYPE_TAG = "return-type"; //$NON-NLS-1$

}
