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
package com.ibm.safe.internal.reporting;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import com.ibm.safe.internal.runners.AbstractSolverRunner.AnalysisKind;
import com.ibm.safe.internal.runners.AbstractSolverRunner.AnalysisStatus;
import com.ibm.safe.io.ZipUtils;
import com.ibm.safe.metrics.IMetrics;
import com.ibm.safe.metrics.ProgramStatistics;
import com.ibm.safe.perf.NamedTimer;
import com.ibm.safe.perf.PerformanceTracker;
import com.ibm.safe.perf.SolverPerfTracker;
import com.ibm.safe.perf.TimeoutStopwatch;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.reporting.message.MethodLocation;
import com.ibm.safe.reporting.message.SignatureUtils;
import com.ibm.safe.rules.IRule;
import com.ibm.safe.utils.SafeEclipseUtils;
import com.ibm.safe.utils.SafeLogger;
import com.ibm.wala.classLoader.IClass;

public final class XMLReporter implements IReporter {

  public XMLReporter(final String anXMLFileName) throws ParserConfigurationException {
    this.xmlFileName = anXMLFileName;
    this.document = createDocument();

    final Element analysisResults = this.document.createElement(XMLReporterConstants.ANALYSIS_RESULT_TAG);
    final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
    analysisResults.setAttribute(XMLReporterConstants.DATE_TAG, dateFormat.format(new Date()));
    this.document.appendChild(analysisResults);
    this.document.insertBefore(createProcessingInstruction(), analysisResults);
  }

  // --- Interface methods implementation

  public void process(final IClass clazz) {
    // Do nothing !
  }

  public void produceFinalReport() throws Exception {
    if (this.document.getDocumentElement() != null) {
      for (Iterator<Message> iter = this.findings.iterator(); iter.hasNext();) {
        final Message finding = iter.next();

        final Element messageTag = createMessageTag(finding);
        addMessageTag(messageTag, finding);
      }

      if (getRulesActivatedTag() != null) {
        addRulesInformation();
      }

      final File xmlFile = new File(this.xmlFileName);
      if (!xmlFile.exists()) {
        if (!xmlFile.getParentFile().exists()) {
          xmlFile.getParentFile().mkdirs();
        }
        xmlFile.createNewFile();
      }

      final Transformer transformer = TransformerFactory.newInstance().newTransformer();
      DOMSource source = new DOMSource(this.document);
      transformer.transform(source, new StreamResult(xmlFile));

      copyRequiredFiles(xmlFile.getParent());

      System.out.println("XML results have been created at " + this.xmlFileName);
    }
  }

  public void reportException(final Throwable exception) {
    final Element excptElement = this.document.createElement(XMLReporterConstants.EXCEPTION_TAG);

    final StringWriter strWriter = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(strWriter);
    exception.printStackTrace(printWriter);
    printWriter.close();

    excptElement.appendChild(this.document.createCDATASection(strWriter.toString()));

    this.document.getDocumentElement().appendChild(excptElement);
  }

  public void reportMessage(final Message message) {
    this.findings.add(message);
  }

  public void reportNumberOfFindings(final int numberOfFindings) {
    // Do nothing !
  }

  public void reportNumberOfRulesActivated(final int numberOfRules) {
    // Do nothing !
  }

  public void reportRuleLoading(final IRule rule) {
    Node rulesActivatedTag = getRulesActivatedTag();
    if (rulesActivatedTag == null) {
      rulesActivatedTag = this.document.createElement(XMLReporterConstants.RULES_ACTIVATED_TAG);
      this.document.getDocumentElement().appendChild(rulesActivatedTag);
    }
    final Element ruleActivatedTag = this.document.createElement(XMLReporterConstants.RULE_ACTIVATED_TAG);
    ruleActivatedTag.appendChild(this.document.createTextNode(rule.getName()));

    rulesActivatedTag.appendChild(ruleActivatedTag);
  }

  public void reportRuleInstances(final IRule rule, int instances) {
    // TODO: fill this to record number of instances in XML doc
  }

  public void reportStatistics(final ProgramStatistics programStat) {
    final Element statistics = this.document.createElement(XMLReporterConstants.STATS_TAG);
    statistics.setAttribute(XMLReporterConstants.STATS_ATTR, "structural"); //$NON-NLS-1$

    addStatisticsEntry(statistics, programStat.getName(ProgramStatistics.NUM_CLASSES), String.valueOf(programStat
        .getEntry(ProgramStatistics.NUM_CLASSES)));
    addStatisticsEntry(statistics, programStat.getName(ProgramStatistics.NUM_METHODS), String.valueOf(programStat
        .getEntry(ProgramStatistics.NUM_METHODS)));
    addStatisticsEntry(statistics, programStat.getName(ProgramStatistics.TOTAL_LOB), String.valueOf(programStat
        .getEntry(ProgramStatistics.TOTAL_LOB)));

    this.document.getDocumentElement().appendChild(statistics);
  }

  public void reportStatistics(final IMetrics typeStateMetrics) {
  }

  public void reportPerformanceTracking(final PerformanceTracker perfoTracker) {
    final NamedTimer[] timers = perfoTracker.getTimers();
    if (timers.length == 0)
      return;

    final Element timersElement = this.document.createElement(XMLReporterConstants.TIMERS_TAG);
    timersElement.setAttribute(XMLReporterConstants.TIMERS_NAME_TAG, perfoTracker.getTrackerKind().toString());

    long totalTime = 0;

    if (perfoTracker instanceof SolverPerfTracker) {
      final SolverPerfTracker spt = (SolverPerfTracker) perfoTracker;
      for (int i = 0; i < timers.length; ++i) {
        totalTime += timers[i].getElapsedMillis();
        int total = spt.getTotalInstances(timers[i].getName());
        int processed = spt.getProcessedInstances(timers[i].getName());
        addTimer(timersElement, timers[i], processed, total);
      }
    } else {
      for (int i = 0; i < timers.length; i++) {
        addTimer(timersElement, timers[i]);
        totalTime += timers[i].getElapsedMillis();
      }
    }

    final Element totalTimeElement = this.document.createElement(XMLReporterConstants.TOTAL_TIME);
    totalTimeElement.appendChild(this.document.createTextNode(String.valueOf(totalTime)));
    timersElement.appendChild(totalTimeElement);

    this.document.getDocumentElement().appendChild(timersElement);
  }

  public void startAnalysis(final AnalysisKind nature) {
    final Element start = this.document.createElement(XMLReporterConstants.START_ANALYSIS_TAG);
    start.setAttribute(XMLReporterConstants.ANALYSIS_NATURE_ATTR, nature.toString());
    start.appendChild(this.document.createTextNode(String.valueOf(System.currentTimeMillis())));

    this.document.getDocumentElement().appendChild(start);
  }

  public void stopAnalysis(final AnalysisKind nature) {
    final Element stop = this.document.createElement(XMLReporterConstants.STOP_ANALYSIS_TAG);
    stop.setAttribute(XMLReporterConstants.ANALYSIS_NATURE_ATTR, nature.toString());
    stop.appendChild(this.document.createTextNode(String.valueOf(System.currentTimeMillis())));

    this.document.getDocumentElement().appendChild(stop);
  }

  public void reportAnalysisStatus(final AnalysisStatus status) {
    final Element statusElement = this.document.createElement(XMLReporterConstants.ANALYSIS_STATUS_TAG);
    statusElement.setAttribute(XMLReporterConstants.ANALYSIS_STATUS_ATTR, status.toString());
    this.document.getDocumentElement().appendChild(statusElement);
  }

  public void version(final String versionNumber) {
    final Element version = this.document.createElement(XMLReporterConstants.VERSION_TAG);
    version.appendChild(this.document.createTextNode(versionNumber));

    this.document.getDocumentElement().appendChild(version);
  }

  // --- Private code

  private void addMessageTag(final Element messageTag, final Message message) {
    final Element rootNode = this.document.getDocumentElement();

    final NodeList messagesTags = rootNode.getChildNodes();
    for (int i = messagesTags.getLength(); --i >= 0;) {
      final Node ithNode = messagesTags.item(i);
      final Node attrNode = ithNode.getAttributes().getNamedItem(XMLReporterConstants.MESSAGE_TYPE_ATTR);
      if ((attrNode != null) && attrNode.getNodeValue().equals(message.getMessageType())) {
        // Find appropriate messages tag. So let's add this new message.
        ithNode.appendChild(messageTag);
        return; // Task finished.
      }
    }

    // No appropriate messages tag created, so let's do the work.
    rootNode.appendChild(createMessagesTag(message, messageTag));
  }

  // private void addProcessingInstruction(final XMLSerializer serializer)
  // throws SAXException {
  // final StringBuffer dataBuf = new StringBuffer(" href=\""); //$NON-NLS-1$
  // dataBuf.append(STYLESHEET_FILENAME).append("\" type=\"text/xsl\"");
  // //$NON-NLS-1$
  // serializer.processingInstruction("xml-stylesheet", dataBuf.toString());
  // //$NON-NLS-1$
  // }

  private ProcessingInstruction createProcessingInstruction() {
    final StringBuffer dataBuf = new StringBuffer(" href=\""); //$NON-NLS-1$
    dataBuf.append(STYLESHEET_FILENAME).append("\" type=\"text/xsl\""); //$NON-NLS-1$
    return this.document.createProcessingInstruction("xml-stylesheet", dataBuf.toString());
  }

  private void addRulesInformation() {
    final Element rulesMatchedTag = this.document.createElement(XMLReporterConstants.RULES_MATCHED_TAG);
    final Set<IRule> rules = new HashSet<IRule>(this.findings.size());
    for (Iterator<Message> iter = this.findings.iterator(); iter.hasNext();) {
      final IRule rule = iter.next().getRule();
      if (!rules.contains(rule)) {
        rules.add(rule);
        rulesMatchedTag.appendChild(createRuleTag(rule));
      }
    }
    this.document.getDocumentElement().appendChild(rulesMatchedTag);
  }

  private void addStatisticsEntry(final Element parentNode, final String statName, final String value) {
    final Element entry = this.document.createElement(XMLReporterConstants.STAT_ENTRY_TAG);
    final Element name = this.document.createElement(XMLReporterConstants.STAT_NAME_TAG);
    name.appendChild(this.document.createTextNode(statName));
    entry.appendChild(name);

    final Element statValue = this.document.createElement(XMLReporterConstants.STAT_VALUE_TAG);
    statValue.appendChild(this.document.createTextNode(value));
    entry.appendChild(statValue);

    parentNode.appendChild(entry);
  }

  private void addTimer(final Element parentNode, final NamedTimer namedTimer, int processed, int total) {
    Element timer = createTimerElement(parentNode, namedTimer);

    final Element totalInstances = this.document.createElement(XMLReporterConstants.TOTAL_INSTANCES_TAG);
    totalInstances.appendChild(this.document.createTextNode(String.valueOf(total)));
    timer.appendChild(totalInstances);

    final Element processedInstances = this.document.createElement(XMLReporterConstants.PROCESSED_INSTANCES_TAG);
    processedInstances.appendChild(this.document.createTextNode(String.valueOf(processed)));
    timer.appendChild(processedInstances);

    parentNode.appendChild(timer);

  }

  private void addTimer(final Element parentNode, final NamedTimer namedTimer) {
    Element timer = createTimerElement(parentNode, namedTimer);
    parentNode.appendChild(timer);
  }

  private Element createTimerElement(final Element parentNode, final NamedTimer namedTimer) {
    final Element timer = this.document.createElement(XMLReporterConstants.TIMER_TAG);

    final Element name = this.document.createElement(XMLReporterConstants.TIMER_NAME_TAG);
    name.appendChild(this.document.createTextNode(namedTimer.getName()));
    timer.appendChild(name);

    final Element timerValue = this.document.createElement(XMLReporterConstants.TIMER_VALUE_TAG);
    timerValue.appendChild(this.document.createTextNode(String.valueOf(namedTimer.getElapsedMillis())));
    timer.appendChild(timerValue);

    if (namedTimer instanceof TimeoutStopwatch) {
      TimeoutStopwatch timeoutTracker = (TimeoutStopwatch) namedTimer;
      final Element timerTimedout = this.document.createElement(XMLReporterConstants.TIMER_TIMEOUT_TAG);
      timerTimedout.appendChild(this.document.createTextNode(Boolean.toString(timeoutTracker.timedOut())));
      timer.appendChild(timerTimedout);
    }
    return timer;
  }

  private void copyRequiredFiles(final String outputDir) {
    final URL url = getClass().getClassLoader().getResource(SCRIPTS_ZIP_FILENAME);
    if (url != null) {
      try {
        ZipUtils.uncompress(SafeEclipseUtils.getFileFromURL(url), outputDir);
      } catch (IOException except) {
        SafeLogger.severe("Unzipping of " + SCRIPTS_ZIP_FILENAME + " failed.", except);
      }
    } // Do nothing otherwise !
  }

  private Document createDocument() throws ParserConfigurationException {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.newDocument();
  }

  private Element createMessageTag(final Message message) {
    final Element messageTag = this.document.createElement(XMLReporterConstants.MESSAGE_TAG);
    final boolean messageHasRule = (message.getRule() != null);

    if (messageHasRule) {
      final Element severity = this.document.createElement(XMLReporterConstants.SEVERITY_TAG);
      severity.appendChild(this.document.createTextNode(message.getRule().getSeverity().toString()));
      messageTag.appendChild(severity);
    }

    final Element messageText = this.document.createElement(XMLReporterConstants.MESSAGE_TEXT_TAG);
    messageText.appendChild(this.document.createTextNode(message.getText()));
    messageTag.appendChild(messageText);

    final Element clazz = this.document.createElement(XMLReporterConstants.CLASS_TAG);
    clazz.appendChild(this.document.createTextNode(SignatureUtils.getClassName(message.getLocation())));
    clazz.setAttribute(XMLReporterConstants.JVM_SIG_ATTR, message.getLocation().getLocationClass());
    messageTag.appendChild(clazz);

    if (message.getLocation().isFieldMember()) {
      final Element field = this.document.createElement(XMLReporterConstants.FIELD_TAG);
      field.appendChild(this.document.createTextNode(message.getLocation().getSourceLocation()));
      messageTag.appendChild(field);
    } else if (message.getLocation().isMethodMember()) {
      final Element method = this.document.createElement(XMLReporterConstants.METHOD_TAG);
      final MethodLocation methodLoc = (MethodLocation) message.getLocation();
      method.appendChild(this.document.createTextNode(SignatureUtils.getMethodSignature(methodLoc, true)));
      method.setAttribute(XMLReporterConstants.JVM_SIG_ATTR, methodLoc.getByteCodeLocation());
      messageTag.appendChild(method);
    }

    final Element line = this.document.createElement(XMLReporterConstants.LINE_TAG);
    line.appendChild(this.document.createTextNode(String.valueOf(message.getLocation().getLocationLineNumber())));
    messageTag.appendChild(line);

    if (message.getLocation().getByteCodeIndex() != -1) {
      final Element bcIndex = this.document.createElement(XMLReporterConstants.BC_INDEX_TAG);
      bcIndex.appendChild(this.document.createTextNode(String.valueOf(message.getLocation().getByteCodeIndex())));
      messageTag.appendChild(bcIndex);
    }

    if (message.getLocation().getAdditionalInformation() != null) {
      final Element addInfo = this.document.createElement(XMLReporterConstants.ADDINFO_TAG);
      addInfo.appendChild(this.document.createTextNode(message.getLocation().getAdditionalInformation().toString()));
      messageTag.appendChild(addInfo);
    }

    return messageTag;
  }

  private Element createMessagesTag(final Message message, final Element messageTag) {
    final Element messagesTag = this.document.createElement(XMLReporterConstants.MESSAGES_TAG);

    messagesTag.setAttribute(XMLReporterConstants.MESSAGE_TYPE_ATTR, message.getMessageType());
    messagesTag.appendChild(messageTag);

    return messagesTag;
  }

  private Element createRuleTag(final IRule rule) {
    final Element ruleTag = this.document.createElement(XMLReporterConstants.RULE_TAG);
    ruleTag.setAttribute(XMLReporterConstants.RULE_NAME_ATTR, rule.getName());

    final Element severity = this.document.createElement(XMLReporterConstants.RULE_SEVERITY_TAG);
    severity.appendChild(this.document.createTextNode(rule.getSeverity().toString()));
    ruleTag.appendChild(severity);

    final Element level = this.document.createElement(XMLReporterConstants.RULE_LEVEL_TAG);
    level.appendChild(this.document.createTextNode(rule.getLevel().toString()));
    ruleTag.appendChild(level);

    final Element desc = this.document.createElement(XMLReporterConstants.RULE_DESCRIPTION_TAG);
    String ruleDesc = rule.getDescription();
    if (ruleDesc != null) {
      desc.appendChild(this.document.createCDATASection(formatProcess(ruleDesc)));
      ruleTag.appendChild(desc);
    }
    final Element example = this.document.createElement(XMLReporterConstants.RULE_EXAMPLE_TAG);
    String exampleString = rule.getExample();
    if (exampleString != null) {
      example.appendChild(this.document.createCDATASection(formatProcess(exampleString)));
      ruleTag.appendChild(example);
    }
    final Element action = this.document.createElement(XMLReporterConstants.RULE_ACTION_TAG);
    String actionString = rule.getAction();
    if (actionString != null) {
      action.appendChild(this.document.createCDATASection(formatProcess(actionString)));
      ruleTag.appendChild(action);
    }
    return ruleTag;
  }

  private String formatProcess(final String content) {
    assert content != null;
    return content.trim();
  }

  private Node getRulesActivatedTag() {
    final NodeList childNodes = this.document.getDocumentElement().getChildNodes();
    for (int i = 0, size = childNodes.getLength(); i < size; ++i) {
      if (childNodes.item(i).getNodeName().equals(XMLReporterConstants.RULES_ACTIVATED_TAG)) {
        return childNodes.item(i);
      }
    }
    return null;
  }

  private final Document document;

  private final String xmlFileName;

  private TreeSet<Message> findings = new TreeSet<Message>(new FindingComparator());

  private static final String STYLESHEET_FILENAME = "main_page.xsl"; //$NON-NLS-1$

  private static final String SCRIPTS_ZIP_FILENAME = "scripts.zip"; //$NON-NLS-1$

}
