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
package com.ibm.safe.core.tests;
// package com.ibm.safe.base;
//
// import java.io.PrintWriter;
// import java.io.StringWriter;
// import java.net.URL;
// import java.util.Collection;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.Stack;
//
// import junit.framework.TestCase;
//
// import com.ibm.capa.core.CapaException;
// import com.ibm.capa.util.properties.PropertiesManager;
// import com.ibm.wala.classLoader.IClass;
// import com.ibm.wala.client.EngineStopwatch;
// import com.ibm.safe.controller.ISafeController;
// import com.ibm.safe.controller.SAFEJavaApplication;
// import com.ibm.safe.controller.SafeControllerFactory;
// import com.ibm.safe.emf.rules.IRule;
// import com.ibm.safe.emf.rules.RuleKind;
// import com.ibm.safe.internal.exceptions.SafeException;
// import com.ibm.safe.internal.reporting.CompositeReporter;
// import com.ibm.safe.internal.reporting.StandardOutputReporter;
// import com.ibm.safe.properties.SafeProperties;
// import com.ibm.safe.properties.SafePropertiesManager;
// import com.ibm.safe.reporting.IReporter;
// import com.ibm.safe.runners.AnalysisNature;
// import
// com.ibm.safe.structural.statistics.StatisticsClassProcessor.ProgramStatistics;
//
// /**
// * Common class for all SAFE test cases that want to test SAFE engines
// * functionalities.
// *
// * @author egeay
// */
// public abstract class AbstractSafeEngine extends TestCase {
//
// // --- Overridden methods
//
// public void setUp() {
// setOption(SafeProperties.RULES_DIRS, createRulesDirsOption());
// setOption(SafeProperties.MODULES_DIRS, "../testdata/jars"); //$NON-NLS-1$
// setBooleanOption(SafeProperties.VERBOSE_MODE);
// getClass().getClassLoader().setDefaultAssertionStatus(true);
// }
//
// // --- Test methods
//
// public final void testNumberOfFindings() {
// try {
// final PropertiesManager propManager =
// SafePropertiesManager.createManager(this.options);
// final ISafeController controller =
// SafeControllerFactory.createController(propManager);
// final IRule[] rules = controller.getRules();
//
// final CompositeReporter reporter = new CompositeReporter();
// reporter.addReporter(new TestOrientedReporter());
// reporter.addReporter(new StandardOutputReporter());
// controller.execute(rules, reporter);
// final StringBuffer buf = new StringBuffer("Expected "); //$NON-NLS-1$
// buf.append(getExpectedNumberOfFindings()).append(", got ") //$NON-NLS-1$
// .append(this.nbFindings);
// assertTrue(buf.toString(), this.nbFindings == getExpectedNumberOfFindings());
//
// doSupplementaryTests();
// } catch (SafeException except) {
// gotSafeExceptionFired(except);
// } catch (Exception except) {
// gotExceptionFired(except);
// }
// }
//  
//  
// // --- Private code
//
// protected void doSupplementaryTests() throws CapaException, SafeException,
// Exception {
// // By default, no supplementary tests. Overridde it if you want so.
// }
//
// protected int getExpectedNumberOfFindings() {
// // No findings by default.
// return 0;
// }
//
// protected void gotExceptionFired(final Exception except) {
// // By default should not be called.
// fail(getStackStrace(except));
// }
//
// protected void gotSafeExceptionFired(final SafeException except) {
// // By default should not be called.
// fail(getStackStrace(except));
// }
//
// protected final Collection getMessages() {
// return this.messages;
// }
//
// protected final void setBooleanOption(final String optionName) {
// this.options.put(optionName, new Object());
// }
//
// protected final void setOption(final String optionName, final String
// optionArgument) {
// this.options.put(optionName, optionArgument);
// }
//
// private String createRulesDirsOption() {
// final URL url =
// SAFEJavaApplication.class.getClassLoader().getResource("safe.properties");
// //$NON-NLS-1$
// assertTrue(url != null);
// final String rootPath = url.getFile().substring(0,
// url.getFile().lastIndexOf("com.ibm.safe")); //$NON-NLS-1$
// final StringBuffer buf = new StringBuffer(rootPath);
// buf.append("com.ibm.safe/nl/en;").append(rootPath) //$NON-NLS-1$
// .append("com.ibm.safe.tests/nl/en"); //$NON-NLS-1$
// return buf.toString();
// }
//
// private String getStackStrace(final Throwable exception) {
// final StringWriter strWriter = new StringWriter();
// final PrintWriter printWriter = new PrintWriter(strWriter);
// exception.printStackTrace(printWriter);
// printWriter.close();
// return strWriter.toString();
// }
//
// public final class TestOrientedReporter implements IReporter {
//
// // --- Interface methods implementation
//
// public void process(final IClass clazz) {
// // Do nothing here !
// }
//
// public void produceFinalReport() throws Exception {
// // Do nothing here !
// }
//
// public void reportMessage(final Message message) {
// AbstractSafeEngine.this.messages.add(message);
// }
//
// public void reportNumberOfFindings(final int numberOfFindings) {
// AbstractSafeEngine.this.nbFindings = numberOfFindings;
// }
//
// public void reportNumberOfRulesActivated(final int numberOfRules, final
// RuleKind ruleKind) {
// // Do nothing here !
// }
//
// public void reportPerformanceTracking(final EngineStopwatch perfoTracker) {
// // Do nothing here !
// }
//
// public void reportStatistics(final ProgramStatistics programStat) {
// // Do nothing here !
// }
//
// public void reportRuleLoading(final IRule rule) {
// // Do nothing here !
// }
//
// public void startAnalysis(final AnalysisNature nature) {
// // Do nothing here !
// }
//
// public void stopAnalysis(final AnalysisNature nature) {
// // Do nothing here !
// }
//
// public void version(final String versionNumber) {
// // Do nothing here !
// }
//
// }
//
// protected int nbFindings;
//
// private Collection messages = new Stack();
//
// protected final Map options = new HashMap(20);
//
// }
