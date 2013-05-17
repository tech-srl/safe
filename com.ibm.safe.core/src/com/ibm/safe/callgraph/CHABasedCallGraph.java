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
package com.ibm.safe.callgraph;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.utils.ReceiverTypeInference;
import com.ibm.wala.analysis.typeInference.ConeType;
import com.ibm.wala.analysis.typeInference.PointType;
import com.ibm.wala.analysis.typeInference.TypeAbstraction;
import com.ibm.wala.analysis.typeInference.TypeInference;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.impl.ExplicitCallGraph;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ContextInsensitiveSSAInterpreter;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableIntSet;
import com.ibm.wala.util.intset.MutableSharedBitVectorIntSet;

/**
 * @author yahave
 * @author sjfink 
 */
public class CHABasedCallGraph extends ExplicitCallGraph {

	protected static final Context CONTEXT = Everywhere.EVERYWHERE;

	public CHABasedCallGraph(IClassHierarchy cha, AnalysisOptions options,
			AnalysisCache cache) {
		super(cha, options, cache);
		setInterpreter(new ContextInsensitiveSSAInterpreter(options, cache));

		// Create nodes for methods in classes found by CHA.
		for (IClass klass : cha) {
			for (IMethod method : klass.getDeclaredMethods()) {
				// Skip abstract methods, unless they're synthetic (and thus
				// could/should be analyzed).
				if (!method.isAbstract() || method.isSynthetic()) {
					try {
						findOrCreateNode(method, CONTEXT);
					} catch (CancelException e) {
						System.err
								.println("WARNING: findOrCreateNode cancelled for "
										+ method);
						e.printStackTrace();
					}
				}
			}
		}
	}

	// It may be more appropriate to extend BasicCallGraph's NodeImpl
	// but the asymmetry would be very confusing.
	public class CHABasedNode extends ExplicitNode {
		// For now, everything is done over and over the hard way.
		// Once this works, we should use ExplicitNode's data structures
		// to cache results, thus building up a CHA-based call graph.

		/**
		 * @param method
		 */
		protected CHABasedNode(IMethod method, Context C) {
			super(method, C);
			assert(C == CONTEXT);
		}

		/**
		 * Look up possible targets based on CHA information. Essentially, this
		 * is copied from InfiniteRecursionMethodProcessor. TODO: Move the meat
		 * to a method that returns an iterator over methods. Then, we don't
		 * waste collections for counting/converting.
		 */
		@Override
		protected Set<CGNode> getPossibleTargets(CallSiteReference site) {
			// Find the possible classes that could contain the target methods.
			TypeInference ti = TypeInference.make(getIR(), false);
			ReceiverTypeInference rti = new ReceiverTypeInference(ti);
			TypeAbstraction t = rti.getReceiverType(site);

			// Now, find the possible target methods in the classes found above.
			HashSet<CGNode> result = HashSetFactory.make();
			if (t instanceof ConeType) {
				ConeType cone = (ConeType) t;
				if (cone.getType().isInterface()) {
					Set<IClass> implementors = cha.getImplementors(cone
							.getType().getReference());
					for (Iterator<IClass> it = implementors.iterator(); it
							.hasNext();) {
						IClass klass = it.next();
						addTarget(result, klass, site);
					}
				} else {
					Collection<IClass> subTypes = cha.computeSubClasses(t
							.getType().getReference());
					for (Iterator<IClass> it = subTypes.iterator(); it
							.hasNext();) {
						IClass klass = it.next();
						addTarget(result, klass, site);
					}
				}
			} else if (t instanceof PointType) {
				addTarget(result, t.getType(), site);
			} else if (t.equals(TypeAbstraction.TOP)) {
				// TODO: Log type-inference failures or complain somewhere. This
				// is bad!
				return Collections.emptySet();
			} else {
				Assertions.UNREACHABLE("internal error: " + t.getClass());
			}
			return result;
		}

		protected void addTarget(Collection<CGNode> nodes, IClass klass,
				CallSiteReference site) {
			IMethod target = cha.resolveMethod(klass, site.getDeclaredTarget()
					.getSelector());
			try {
				nodes.add(getCallGraph().findOrCreateNode(target, CONTEXT));
			} catch (CancelException e) {
				// TODO Auto-generated catch block
				System.err
						.println("WARNING: The addition of the following node was cancelled: "
								+ target);
				e.printStackTrace();
			}
		}

		@Override
		protected IntSet getPossibleTargetNumbers(CallSiteReference site) {
			MutableIntSet result = new MutableSharedBitVectorIntSet();
			for (CGNode target : getPossibleTargets(site)) {
				result.add(getCallGraph().getNumber(target));
			}
			return result;
		}

		@Override
		protected int getNumberOfTargets(CallSiteReference site) {
			return getPossibleTargets(site).size();
		}

		@Override
		public boolean addTarget(CallSiteReference site, CGNode tNode) {
			Assertions.UNREACHABLE();
			return false;
		}

		/*
		 * @see
		 * com.ibm.wala.ipa.callgraph.impl.BasicCallGraph.NodeImpl#removeTarget
		 * (com.ibm.wala.ipa.callgraph.CGNode)
		 */
		public void removeTarget(CGNode target) {
			Assertions.UNREACHABLE();
		}

		public void clearAllTargets() {
			Assertions.UNREACHABLE();
		}

		@Override
		public boolean equals(Object obj) {
			// We can use object equality since these objects are canonical,
			// as created by the governing CHABasedCallGraph.
			return this == obj;
		}

		@Override
		public int hashCode() {
			// TODO: cache?
			return getMethod().hashCode();
		}

		public CHABasedCallGraph getCallGraph() {
			return CHABasedCallGraph.this;
		}
	}
}
