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
 * Created on Dec 16, 2004
 */
package com.ibm.safe.structural;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ibm.safe.controller.ISafeSolver;
import com.ibm.safe.intraproc.ConstantConditionInstructionProcessor;
import com.ibm.safe.intraproc.InfiniteRecursionMethodProcessor;
import com.ibm.safe.intraproc.SCCPMethodProcessor;
import com.ibm.safe.lightweight.options.IStructuralOptions;
import com.ibm.safe.metrics.ProgramStatistics;
import com.ibm.safe.processors.AccessControlProgramProcessor;
import com.ibm.safe.processors.BaseClassProcessor;
import com.ibm.safe.processors.BaseProgramProcessor;
import com.ibm.safe.reporting.IReporter;
import com.ibm.safe.reporting.message.ISolverResult;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.rules.StructuralRule;
import com.ibm.safe.secure.accessibility.AccessibilityConstants;
import com.ibm.safe.structural.statistics.StatisticsClassProcessor;
import com.ibm.safe.structural.xml.ClassXMLProcessor;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.CancelException;
import java.util.function.Predicate;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class StructuralSolver implements ISafeSolver, AccessibilityConstants {

	protected CallGraph cg;

	protected IClassHierarchy classHierarchy;

	protected PointerAnalysis pointerAnalysis;

	private IStructuralOptions structuralOptions;

	private IReporter reporter;

	private Predicate<IClass> classFilter;

	private static final String NULL_DEREF_RULE_NAME = "Potential null dereference"; //$NON-NLS-1$

	private static final String CONSTANT_CONDITION_RULE_NAME = "Suspicious condition over a constant value"; //$NON-NLS-1$

	private static final String INFINITE_RECURSION_RULE_NAME = "Potential infinite recursion"; //$NON-NLS-1$

	private static final boolean REVERT_TO_OLD = false;

	public StructuralSolver(IClassHierarchy classHierarchy,
			Predicate<IClass> classFilter, CallGraph cg,
			PointerAnalysis pointerAnalysis,
			IStructuralOptions structuralSafeOptions, IReporter safeReporter) {
		assert (classHierarchy != null);
		this.cg = cg;
		this.pointerAnalysis = pointerAnalysis;
		this.classHierarchy = classHierarchy;
		this.structuralOptions = structuralSafeOptions;
		this.reporter = safeReporter;
		this.classFilter = classFilter;
	}

	@SuppressWarnings("unchecked")
	public ISolverResult perform(final IProgressMonitor monitor)
			throws CancelException {
		SafeStructuralSolverResult result = new SafeStructuralSolverResult();

		assert (classHierarchy != null);
		BaseProgramProcessor bpp = new BaseProgramProcessor(classHierarchy, cg,
				reporter, this.classFilter, monitor);
		BaseClassProcessor bcp = bpp.getBaseClassProcessor();
		ClassXMLProcessor cxp = new ClassXMLProcessor(classHierarchy, cg,
				structuralOptions);
		bpp.addClassProcessor(cxp);

		StatisticsClassProcessor csp = null;

		ProgramStatistics programStatistics = null;
		if (this.structuralOptions.shouldCollectStatistics()) {
			programStatistics = new ProgramStatistics();
			csp = new StatisticsClassProcessor(classHierarchy, cg,
					programStatistics);
			bpp.addClassProcessor(csp);
		}

		final SCCPMethodProcessor sndmp = new SCCPMethodProcessor(
				classHierarchy);

		final StructuralRule constantConditionRule = getConstantConditionRule();
		final ConstantConditionInstructionProcessor ccip = new ConstantConditionInstructionProcessor(
				constantConditionRule);
		if (constantConditionRule != null) {
			sndmp.addInstructionProcessor(ccip);
		}

		final StructuralRule nullDerefRule = getNullDeRefRule();

		if (constantConditionRule != null) {
			bcp.addMethodProcessor(sndmp);
		}

		final StructuralRule infiniteRecursionRule = getInfiniteRecursionRule();
		final InfiniteRecursionMethodProcessor irmp = new InfiniteRecursionMethodProcessor(
				infiniteRecursionRule, classHierarchy);
		if (infiniteRecursionRule != null) {
			bcp.addMethodProcessor(irmp);
		}

		bpp.process();

		result.addMessages((Set<Message>) sndmp.getResult());
		result.addMessages((Set<Message>) cxp.getResult());
		result.addMessages((Set<Message>) irmp.getResult());

		if (findRuleByName(PUBLIC_METHOD_PROTECTED) != null) {
			AccessControlProgramProcessor acpp = new AccessControlProgramProcessor(
					classHierarchy, this.structuralOptions.getRules(),
					classFilter);
			acpp.process();
			result.addMessages((Set<Message>) acpp.getResult());
		}

		if (this.structuralOptions.shouldCollectStatistics()) {
			this.reporter.reportStatistics(programStatistics);
		}

		return result;
	}

	private StructuralRule findRuleByName(final String ruleName) {
		final StructuralRule[] rules = this.structuralOptions.getRules();
		for (int i = 0; i < rules.length; i++) {
			if (rules[i].getName().equals(ruleName)) {
				return rules[i];
			}
		}
		return null;
	}

	/*
	 * Ugly hack :-/ The XML model should be updated to have those rules working
	 * via XPATH queries.
	 */
	private StructuralRule getInfiniteRecursionRule() {
		return findRuleByName(INFINITE_RECURSION_RULE_NAME);
	}

	/*
	 * Ugly hack :-/ The XML model should be updated to have those rules working
	 * via XPATH queries.
	 */
	private StructuralRule getConstantConditionRule() {
		return findRuleByName(CONSTANT_CONDITION_RULE_NAME);
	}

	/*
	 * Ugly hack :-/ The XML model should be updated to have those rules working
	 * via XPATH queries.
	 */
	private StructuralRule getNullDeRefRule() {
		return findRuleByName(NULL_DEREF_RULE_NAME);
	}

	/**
	 * @return Returns the reporter.
	 */
	public IReporter getReporter() {
		return reporter;
	}
}