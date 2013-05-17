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
/*
 * Created on Dec 16, 2004
 */
package com.ibm.safe.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class XMLFileReader {
  /**
   * reads an XML file and returns the resulting DOM document
   */
  public static Document read(String fileName) throws ParserConfigurationException, SAXException, IOException {
    Document doc;
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    dbf.setIgnoringComments(true);

    DocumentBuilder db = dbf.newDocumentBuilder();

    OutputStreamWriter errorWriter = new OutputStreamWriter(System.err);
    db.setErrorHandler(new MyErrorHandler(new PrintWriter(errorWriter, true)));

    doc = db.parse(new File(fileName));
    return doc;
  }

  /**
   * XML Parser error handler
   * 
   * @author Eran Yahav yahave
   */
  public static class MyErrorHandler implements ErrorHandler {
    /** Error handler output goes here */
    private PrintWriter out;

    /**
     * create a new error handler
     * 
     * @param pw -
     *            the printer writer
     */
    MyErrorHandler(PrintWriter pw) {
      this.out = pw;
    }

    /**
     * Returns a string describing parse exception details
     * 
     * @param spe -
     *            the parse exception
     * @return string describing parse exception
     */
    private String getParseExceptionInfo(final SAXParseException spe) {
      String systemId = spe.getSystemId();
      if (systemId == null) {
        systemId = "null";
      }
      String info = "URI=" + systemId + " Line=" + spe.getLineNumber() + ": " + spe.getMessage();
      return info;
    }

    /**
     * warning in XML parsing
     * 
     * @param spe -
     *            the parse exception thrown
     */
    public void warning(SAXParseException spe) {
      out.println("Warning: " + getParseExceptionInfo(spe));
    }

    /**
     * error in XML parsing
     * 
     * @param spe -
     *            the parse exception thrown
     * @throws SAXException -
     *             XML parsing exception
     */
    public void error(SAXParseException spe) throws SAXException {
      String message = "Error: " + getParseExceptionInfo(spe);
      throw new SAXException(message);
    }

    /**
     * fatal error in XML parsing
     * 
     * @param spe -
     *            the parse exception thrown
     * @throws SAXException -
     *             XML parsing exception
     */
    public void fatalError(SAXParseException spe) throws SAXException {
      String message = "Fatal Error: " + getParseExceptionInfo(spe);
      throw new SAXException(message);
    }
  }

}