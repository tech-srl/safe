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

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.safe.cha.J2SEClassHierarchyEngine;
import com.ibm.safe.io.XMLUtil;
import com.ibm.safe.lightweight.options.IStructuralOptions;
import com.ibm.safe.processors.ClassProcessor;
import com.ibm.safe.processors.MethodProcessor;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.rules.StructuralRule;
import com.ibm.safe.structural.impl.StructuralMessage;
import com.ibm.safe.utils.SafeLogger;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashSetFactory;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class ClassXMLProcessor implements ClassProcessor {

  // private static final String MODIFIER_PUBLIC = "public";

  // private static final String MODIFIER_PROTECTED = "protected";

  private static final String MODIFIER_PRIVATE = "private";

  private static final String MODIFIER_STATIC = "static";

  private static final String MODIFIER_ABSTRACT = "abstract";

  // private static final String MODIFIER_FINAL = "final";

  private static final String MODIFIER_NATIVE = "native";

  private static final String MODIFIER_SYNCHRONIZED = "synchronized";

  // private static final String MODIFIER_TRANSIENT = "transient";

  // private static final String MODIFIER_VOLATILE = "volatile";

  // private static final String MODIFIER_STRICTFP = "strictfp";

  private static final String CLASS_TAG = "class";

  private static final String NAME_TAG = "name";

  private static final String MODIFIERS_TAG = "modifiers";

  private static final String MODIFIER_TAG = "modifier";

  private static final String EXTENDS_TAG = "extends";

  private static final String IMPLEMENTS_LIST_TAG = "implements-list";

  private static final String IMPLEMENTS_TAG = "implements";

  private static final String PARAMETER_TAG = "parameter";

  private static final String ID_TAG = "id";

  private static final String TYPE_TAG = "type";

  private static final String DEFINE_METHODS_TAG = "define-methods";

  private static final String DEFINE_FIELDS_TAG = "define-fields";

  private static final String METHOD_TAG = "method";

  private static final String FIELD_TAG = "field";

  // private static final String INSTR_TAG = "instr";

  // private static final String BODY_TAG = "body";

  private static final String STATIC_FIELD_TAG = "static-field";

  private static final String SIGNATURE_TAG = "signature";

  private static final String RETURN_TYPE_TAG = "returntype";

  private static final char SLASH_REPLACEMENT = '.';

  private MethodXMLProcessor methodXMLModel;

  /** underlying class hierarchy */
  protected IClassHierarchy classHierarchy;

  /** underlying callgraph */
  protected CallGraph callGraph;

  private Collection<? extends Message> result;

  private Set<Message> allMessages;

  private IStructuralOptions structuralOptions;

  public ClassXMLProcessor(IClassHierarchy hierarchy, CallGraph callGraph, IStructuralOptions structuralSafeOptions) {
    this.classHierarchy = hierarchy;
    this.callGraph = callGraph;
    this.allMessages = HashSetFactory.make();
    methodXMLModel = new MethodXMLProcessor(hierarchy, callGraph);
    this.structuralOptions = structuralSafeOptions;
  }

  public void process(IClass currentClass) {
    Set<StructuralMessage> messages = HashSetFactory.make();

    if (J2SEClassHierarchyEngine.isApplicationClass(currentClass)) {
      Document classDoc;
      try {
        classDoc = analyzeClass(currentClass);
      } catch (Exception e1) {
        e1.printStackTrace();
        throw new RuntimeException("Something went terribly wrong :(");
      }

      final StructuralRule[] rules = this.structuralOptions.getRules();
      for (int i = 0; i < rules.length; i++) {
        try {
          if (rules[i].getQuery() != null) {
            StructuralMessage[] msg = XMLQueryEvaluator.execute(rules[i], classDoc);
            for (int j = 0; j < msg.length; j++) {
              messages.add(msg[j]);
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException("Something went terribly wrong :(");
        }
      }
    }

    this.result = messages;
  }

  public void processProlog(IClass currentClass) {

  }

  public void processEpilog(IClass currentClass) {
    allMessages.addAll(result);
  }

  public void addMethodProcessor(MethodProcessor mp) {
    throw new UnsupportedOperationException("Cannot add method processors to this class processor");
  }

  public Document analyzeClass(IClass klass) {
    /**
     * DOM document to hold class information
     */
    Document document;
    /**
     * builder factory for DOM objects
     */
    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      DOMImplementation domImplementation = documentBuilder.getDOMImplementation();

      document = domImplementation.createDocument("safe", "safe", null);
      Element root = document.getDocumentElement();

      Element classElement = document.createElement(CLASS_TAG);
      Element methodsRoot = document.createElement(DEFINE_METHODS_TAG);
      Element fieldsRoot = document.createElement(DEFINE_FIELDS_TAG);
      // Element bodyRoot = document.createElement(BODY_TAG);

      String classSimpleName = simplify(klass.getName().toString());
      IClass superClass = klass.getSuperclass();
      if (superClass != null) {
        String superClassName = superClass.getName().toString();
        classElement.appendChild(XMLDOMUtils.createTaggedElement(document, EXTENDS_TAG, superClassName));
        addImplementsList(klass, classElement, document);
      }

      classElement.appendChild(XMLDOMUtils.createTaggedElement(document, NAME_TAG, classSimpleName));

      root.appendChild(classElement);
      classElement.appendChild(methodsRoot);
      addMethodDefinitions(klass, document, methodsRoot);
      classElement.appendChild(fieldsRoot);
      addFieldDefinitions(klass, document, fieldsRoot);

      if (this.structuralOptions.shouldDumpXML()) {
        final String xmlDir = this.structuralOptions.getXMLDumpingDir();
        if (directoryCreated(xmlDir)) {
          final StringBuffer buf = new StringBuffer(xmlDir);
          buf.append(File.separatorChar).append(classSimpleName).append(".xml"); //$NON-NLS-1$
          final StreamResult outStream = new StreamResult(buf.toString());
          XMLUtil.writeXMLDocument(document, outStream);
        }
      }

      return document;

    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      throw new RuntimeException("DOM builder error.");
    } catch (TransformerConfigurationException e) {
      throw new RuntimeException("Error writing XML output");
    } catch (TransformerException e) {
      throw new RuntimeException("Error writing XML output");
    }
  }

  private boolean directoryCreated(final String xmlDir) {
    final File dir = new File(xmlDir);
    if (!dir.exists() && !dir.mkdirs()) {
      SafeLogger.severe("Unable to create XML directory " + xmlDir + ". Writing of XML file aborted.");
      return false;
    }
    return true;
  }

  public void addImplementsList(IClass klass, Element classElement, Document doc) {
    if (!klass.isInterface() && !klass.getAllImplementedInterfaces().isEmpty()) {
      Element implementsElement = doc.createElement(IMPLEMENTS_LIST_TAG);
      classElement.appendChild(implementsElement);

      for (Iterator<IClass> it = klass.getAllImplementedInterfaces().iterator(); it.hasNext();) {
        IClass curr = it.next();
        String intName = curr.getName().toString().substring(1).replace('/', '.');
        implementsElement.appendChild(XMLDOMUtils.createTaggedElement(doc, IMPLEMENTS_TAG, intName));
      }
    }
  }

  public void addFieldDefinitions(IClass klass, Document doc, Element fieldsRoot) {

    for (Iterator<IField> instanceFieldIterator = klass.getDeclaredInstanceFields().iterator(); instanceFieldIterator.hasNext();) {
      IField iFld = instanceFieldIterator.next();
      FieldReference fld = iFld.getReference();

      // signature, name, type, is it used?
      String fieldName = simplify(fld.getName().toString());
      String typeString = simplify(fld.getFieldType().toString());
      String fieldSignature = simplify(fld.getSignature());

      Element fieldElement = doc.createElement(FIELD_TAG);

      fieldElement.appendChild(XMLDOMUtils.createTaggedElement(doc, SIGNATURE_TAG, fieldSignature));
      fieldElement.appendChild(XMLDOMUtils.createTaggedElement(doc, NAME_TAG, fieldName));
      fieldElement.appendChild(XMLDOMUtils.createTaggedElement(doc, RETURN_TYPE_TAG, typeString));

      fieldsRoot.appendChild(fieldElement);
    }

    for (Iterator<IField> staticFieldIterator = klass.getDeclaredStaticFields().iterator(); staticFieldIterator.hasNext();) {
      IField iFld = staticFieldIterator.next();
      FieldReference sfld = iFld.getReference();

      // signature, name, type, is it used?
      String fieldName = simplify(sfld.getName().toString());
      String typeString = simplify(sfld.getFieldType().toString());
      String fieldSignature = simplify(sfld.getSignature());

      Element fieldElement = doc.createElement(STATIC_FIELD_TAG);

      fieldElement.appendChild(XMLDOMUtils.createTaggedElement(doc, SIGNATURE_TAG, fieldSignature));
      fieldElement.appendChild(XMLDOMUtils.createTaggedElement(doc, NAME_TAG, fieldName));
      fieldElement.appendChild(XMLDOMUtils.createTaggedElement(doc, RETURN_TYPE_TAG, typeString));

      fieldsRoot.appendChild(fieldElement);
    }

  }

  public void addMethodDefinitions(IClass klass, Document doc, Element methodsRoot) {

    try {
      for (Iterator<IMethod> methodIterator = klass.getDeclaredMethods().iterator(); methodIterator.hasNext();) {
        IMethod method = methodIterator.next();
        Element methodElement = addMethod(klass, method, doc);
        methodsRoot.appendChild(methodElement);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Create a method's sub-tree in the DOM document Method subtree structure: -
   * Method -- name -- modifiers -- return type -- signature (redundant
   * information for easier querying)
   */
  private Element addMethod(IClass klass, IMethod method, Document doc) {
    String methodName = simplify(method.getName().toString());
    String returnTypeString = simplify(method.getReference().getReturnType().getName().toString());
    String methodSignature = simplify(method.getSignature());

    Element methodElement = doc.createElement(METHOD_TAG);

    methodElement.appendChild(XMLDOMUtils.createTaggedElement(doc, SIGNATURE_TAG, methodSignature));
    methodElement.appendChild(XMLDOMUtils.createTaggedElement(doc, NAME_TAG, methodName));
    methodElement.appendChild(XMLDOMUtils.createTaggedElement(doc, RETURN_TYPE_TAG, returnTypeString));

    Set<String> modifiers = getMethodModifiers(method);
    if (!modifiers.isEmpty()) {
      Element modifiersListElement = doc.createElement(MODIFIERS_TAG);
      methodElement.appendChild(modifiersListElement);

      for (Iterator<String> modIt = modifiers.iterator(); modIt.hasNext();) {
        String mod = modIt.next();
        modifiersListElement.appendChild(XMLDOMUtils.createTaggedElement(doc, MODIFIER_TAG, mod));
      }
    }

    int params = method.getNumberOfParameters();
    for (int i = 0; i < params; i++) {

      TypeReference typeRef = method.getParameterType(i);

      Element param = doc.createElement(PARAMETER_TAG);

      param.appendChild(XMLDOMUtils.createTaggedElement(doc, TYPE_TAG, simplify(typeRef.getName().toString())));
      param.setAttribute(ID_TAG, String.valueOf(i));

      methodElement.appendChild(param);
    }

    methodXMLModel.setup(method, doc);
    methodXMLModel.processProlog(method);
    methodXMLModel.process(method);
    Element bodyElement = (Element) methodXMLModel.getResult();
    // Element bodyElement = methodXMLModel.processMethod(doc, klass, method);

    if (bodyElement != null) {
      methodElement.appendChild(bodyElement);
    }

    return methodElement;
  }

  public Set<String> getMethodModifiers(IMethod method) {
    Set<String> result = HashSetFactory.make();
    if (method.isSynchronized()) {
      result.add(MODIFIER_SYNCHRONIZED);
    }
    if (method.isPrivate()) {
      result.add(MODIFIER_PRIVATE);
    }
    if (method.isStatic()) {
      result.add(MODIFIER_STATIC);
    }
    if (method.isAbstract()) {
      result.add(MODIFIER_ABSTRACT);
    }
    if (method.isNative()) {
      result.add(MODIFIER_NATIVE);
    }
    return result;
  }

  public Object getResult() {
    return allMessages;
  }

  public Set<? extends Message> getMessages() {
    return allMessages;
  }

  private String simplify(String str) {
    return str.replace('/', SLASH_REPLACEMENT);
  }

  // private boolean isFieldUsed(IClass klass, FieldReference fld) {
  // boolean result = false;
  //
  // WarningSet warnings = new WarningSet();
  // for (Iterator it = klass.getDeclaredMethods(); it.hasNext();) {
  // IMethod method = (IMethod) it.next();
  // IR ir =
  // IRCache.findOrCreate(method,classHierarchy,SSAOptions.defaultOptions(),warnings);
  //
  // for (Iterator instIt = ir.iterateAllInstructions(); instIt.hasNext();) {
  // SSAInstruction instr = (SSAInstruction) instIt.next();
  // //@TODO: finish this [EY]
  // }
  // }
  //
  // return result;
  // }

}
