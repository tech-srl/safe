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
package com.ibm.safe.intraproc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.ibm.safe.processors.BaseMethodProcessor;
import com.ibm.safe.reporting.message.Location;
import com.ibm.safe.rules.StructuralRule;
import com.ibm.safe.structural.impl.StructuralMessage;
import com.ibm.safe.utils.ReceiverTypeInference;
import com.ibm.wala.analysis.typeInference.ConeType;
import com.ibm.wala.analysis.typeInference.PointType;
import com.ibm.wala.analysis.typeInference.TypeAbstraction;
import com.ibm.wala.analysis.typeInference.TypeInference;
import com.ibm.wala.cfg.CFGSanitizer;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.CodeScanner;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisCacheImpl;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.CompoundPiPolicy;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.InstanceOfPiPolicy;
import com.ibm.wala.ssa.NullTestPiPolicy;
import com.ibm.wala.ssa.SSAPiNodePolicy;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.traverse.DFS;

/**
 * @author sfink
 * @author yahave
 */
public class InfiniteRecursionMethodProcessor extends BaseMethodProcessor {

  private static final int UNKNOWN_LINE_NUMBER = -1;

  private ClassHierarchy classHierarchy;

  private Set<StructuralMessage> result = HashSetFactory.make();

  /**
   * The rule related to this particular processor.
   */
  private StructuralRule rule;

  /**
   * Initializes rule field and let the other fields to their default value.
   */
  public InfiniteRecursionMethodProcessor(final StructuralRule structuralRule, IClassHierarchy cha) {
    super(cha);
    this.rule = structuralRule;
  }

  /**
   * @param c -
   *            context class
   * @param context -
   *            class hierarchy
   */
  public void setup(IClass c, Object context) {
    classHierarchy = (ClassHierarchy) context;
  }

  public void processProlog(IMethod method) {
  }

  public void process(IMethod method) {
    if (method.isAbstract() || method.isNative()) {
      return;
    }
    try {
      boolean mayInfiniteRecursion = InfiniteRecursionMethodProcessor.checkMethod(method, classHierarchy);
      if (mayInfiniteRecursion) {
        Location currLocation = Location.createMethodLocation(method.getDeclaringClass().getName(), method.getSelector(),
            UNKNOWN_LINE_NUMBER);
        StructuralMessage msg = new StructuralMessage(rule, currLocation);

        result.add(msg);
      }
    } catch (WalaException e) {
      // throw new RuntimeException(e);
      // silently ignore
    }

  }

  public Object getResult() {
    return result;
  }

  /**
   * @param m
   * @param cha
   * @return true iff m looks like it suffers from infinite recursion
   * @throws WalaException
   */
  public static boolean checkMethod(IMethod m, ClassHierarchy cha) throws WalaException {
    try {
      if (m.isAbstract()) {
        return false;
      }

      IR ir = null;
      ReceiverTypeInference rti = null;

      // compute the call site that must be self-recursive
      HashSet<CallSiteReference> selfCalls = new HashSet<CallSiteReference>(5);
      for (CallSiteReference site : CodeScanner.getCallSites(m)) {
        // first check that the method selector matches
        if (!site.getDeclaredTarget().getSelector().equals(m.getSelector())) {
          continue;
        }
        MethodReference target = site.getDeclaredTarget();
        if (site.isSpecial() || site.isStatic()) {
          IClass klass = cha.lookupClass(target.getDeclaringClass());
          if (klass == null) {
            // clearly not a self-call.
            continue;
          }
          IMethod t = cha.resolveMethod(klass, target.getSelector());
          if (t != null && t.equals(m)) {
            selfCalls.add(site);
          }
        } else {
          if (ir == null) {
            ir = makeIR(m, cha);
            rti = makeRTI(ir);
          }
          TypeAbstraction t = rti.getReceiverType(site);
          if (t != null) {
            Collection<IMethod> possibleTargets = getPossibleTargets(t, cha, site);
            if (possibleTargets.size() == 1) {
              IMethod targetMethod = possibleTargets.iterator().next();
              if (targetMethod != null && targetMethod.equals(m)) {
                selfCalls.add(site);
              }
            }
          }
        }
      }

      if (selfCalls.isEmpty()) {
        return false;
      }
      if (ir == null) {
        ir = makeIR(m, cha);
      }
      // compute a "cleaned CFG", ignoring undeclared exceptional edges to
      // exit()
      Graph<ISSABasicBlock> G = CFGSanitizer.sanitize(ir, cha);

      // compute set of basic blocks containing self-calls
      final Set<IBasicBlock> recursiveBlocks = HashSetFactory.make();
      for (Iterator<CallSiteReference> it = selfCalls.iterator(); it.hasNext();) {
        CallSiteReference site = it.next();
        IBasicBlock[] b = ir.getBasicBlocksForCall(site);
        for (int i = 0; i < b.length; i++) {
          recursiveBlocks.add(b[i]);
        }
      }

      // check if you can reach the exit without traversing a block holding a
      // recursive call
      // if NOT, then this method looks like it suffers from infinite recursion
      Predicate<IBasicBlock> f = new Predicate<IBasicBlock>() {
        public boolean test(IBasicBlock o) {
          return !recursiveBlocks.contains(o);
        }
      };
      Collection<ISSABasicBlock> reachable = DFS.getReachableNodes(G, Collections.singleton(ir.getControlFlowGraph().entry()), f);
      return (!reachable.contains(ir.getControlFlowGraph().exit()));
    } catch (Exception e) {
      e.printStackTrace();
      throw new WalaException(e.getLocalizedMessage(), e);
    }
  }

  private static Collection<IMethod> getPossibleTargets(TypeAbstraction t, ClassHierarchy cha, CallSiteReference site)
      throws WalaException {
    HashSet<IMethod> result = HashSetFactory.make();
    if (t instanceof ConeType) {
      ConeType cone = (ConeType) t;
      if (cone.getType().isInterface()) {
        Set<IClass> implementors = cha.getImplementors(cone.getType().getReference());
        for (Iterator<IClass> it = implementors.iterator(); it.hasNext();) {
          IClass klass = it.next();
          IMethod target = cha.resolveMethod(klass, site.getDeclaredTarget().getSelector());
          result.add(target);
        }
      } else {
        Collection<IClass> subTypes = cha.computeSubClasses(t.getType().getReference());
        for (Iterator<IClass> it = subTypes.iterator(); it.hasNext();) {
          IClass klass = it.next();
          IMethod target = cha.resolveMethod(klass, site.getDeclaredTarget().getSelector());
          result.add(target);
        }
      }
    } else if (t instanceof PointType) {
      IMethod target = cha.resolveMethod(t.getType(), site.getDeclaredTarget().getSelector());
      result.add(target);
    } else if (t.equals(TypeAbstraction.TOP)) {
      // type inference failed. give up
      return Collections.emptySet();
    } else {
      throw new WalaException("internal error: " + t.getClass());
    }
    return result;
  }

  private static ReceiverTypeInference makeRTI(IR ir) throws WalaException {
    TypeInference t = TypeInference.make(ir, false);
    return new ReceiverTypeInference(t);
  }

  private static IR makeIR(IMethod m, ClassHierarchy cha) throws WalaException {
    AnalysisOptions options = new AnalysisOptions();
    SSAPiNodePolicy policy = CompoundPiPolicy.createCompoundPiPolicy(InstanceOfPiPolicy.createInstanceOfPiPolicy(), NullTestPiPolicy.createNullTestPiPolicy());
    options.getSSAOptions().setPiNodePolicy(policy);
    IR ir = new AnalysisCacheImpl().getSSACache().findOrCreateIR(m, Everywhere.EVERYWHERE, options.getSSAOptions());
    return ir;
  }

}
