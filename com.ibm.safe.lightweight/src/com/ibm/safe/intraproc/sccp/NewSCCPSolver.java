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
 * Created on Jan 22, 2005
 */
package com.ibm.safe.intraproc.sccp;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.ibm.safe.utils.Trace;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.cfg.Util;
import com.ibm.wala.dataflow.ssa.SSAInference.VariableFactory;
import com.ibm.wala.fixpoint.IVariable;
import com.ibm.wala.shrikeBT.ConditionalBranchInstruction;
import com.ibm.wala.ssa.ConstantValue;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.ssa.Value;
import com.ibm.wala.ssa.SSAInstruction.Visitor;
import com.ibm.wala.ssa.analysis.ExplodedControlFlowGraph;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.BitVectorIntSet;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.MutableIntSet;
import com.ibm.wala.util.intset.MutableSparseIntSet;

/**
 * 
 * @author Eran Yahav (yahave)
 */

public class NewSCCPSolver {

  protected static final boolean DEBUG = false;

  private static final MutableIntSet EMPTY_INTSET = MutableSparseIntSet.makeEmpty();

  /**
   * The governing SSA form
   */
  private IR ir;

  private ExplodedControlFlowGraph cfg;

  private BitVectorIntSet trueCaseEdges = new BitVectorIntSet();

  protected SymbolTable st;

  /**
   * Dataflow variables, one for each value in the symbol table.
   */
  protected IVariable[] vars;

  protected BitVectorIntSet flowWorkList;

  protected BitVectorIntSet ssaWorkList;

  /**
   * set of Edges for which ExecFlag is enabled
   */
  private BitVectorIntSet execFlag;

  private BitVectorIntSet edges = new BitVectorIntSet();

  /**
   * Map of incoming edges for a basic block BasicBlock number -> Set of Edge
   * identifiers
   */
  private Map<Integer, BitVectorIntSet> incomingEdges = HashMapFactory.make();

  /**
   * Map of outgoing CFG edges for a basic block BasicBlock number -> Set of
   * Edge identifiers
   */
  private Map<Integer, BitVectorIntSet> outgoingCFGEdges = HashMapFactory.make();

  /**
   * Map of outgoing SSA edges for a basic block BasicBlock number -> Set of
   * Edge identifiers
   */
  private Map<Integer, BitVectorIntSet> outgoingSSAEdges = HashMapFactory.make();

  private DefUse defUse;

  private ExpressionInstructionVisitor evalVisitor;

  /**
   * dictionary holding a unique identifier (int) for every edge
   */
  private EdgeDictionary edgeDictionary;

  /**
   * 
   */
  private Map<SSAInstruction, ISSABasicBlock> instruction2block = HashMapFactory.make();;

  private Map<SSAInstruction, ISSABasicBlock> instruction2Oldblock = HashMapFactory.make();

  /**
   * A set of blocks that are true-pi instructions
   */
  final private Set<SSAPiInstruction> trueCasePiInstructions = HashSetFactory.make();

  /**
   * create a new solver for a given IR
   * 
   * @param ir
   */
  public NewSCCPSolver(IR ir) {
    assert ir != null;

    if (DEBUG) {
      Trace.println("Creating New SCCP Solver");
      Trace.println(ir.toString());
    }

    this.ir = ir;
    this.cfg = ExplodedControlFlowGraph.make(ir);

    initBlockTable(cfg, instruction2block);
    initBlockTable(ir.getControlFlowGraph(), instruction2Oldblock);

    if (DEBUG) {
      Trace.println("--created ExplodedControlFlowGraph");
      dumpDotFiles();
    }

    edgeDictionary = new EdgeDictionary();

    defUse = new DefUse(ir);
    this.st = ir.getSymbolTable();
    init(ir, this.new SCCPVarFactory());

    evalVisitor = new ExpressionInstructionVisitor();

    if (DEBUG) {
      Trace.println("Created NewSCCP Solver");
    }
  }

  private void initBlockTable(ControlFlowGraph<SSAInstruction,? extends ISSABasicBlock> cfg, Map<SSAInstruction, ISSABasicBlock> map) {
    for (Iterator<? extends ISSABasicBlock> it = cfg.iterator(); it.hasNext();) {
      ISSABasicBlock bb = it.next();
      if (bb instanceof IExplodedBasicBlock) {
        SSAInstruction inst = ((IExplodedBasicBlock) bb).getInstruction();
        if (inst != null) {
          map.put(inst, bb);
        }
      } else {
        for (Iterator<SSAInstruction> instIt = bb.iterator(); instIt.hasNext();) {
          SSAInstruction inst = instIt.next();
          if (inst != null) {
            map.put(inst, bb);
          }
        }
      }
      for (Iterator<SSAPiInstruction> piIt = bb.iteratePis(); piIt.hasNext();) {
        SSAPiInstruction currPi = piIt.next();
        if (currPi != null) {
          map.put(currPi, bb);
        }
      }
      for (Iterator<SSAPhiInstruction> phiIt = bb.iteratePhis(); phiIt.hasNext();) {
        SSAPhiInstruction currPhi = phiIt.next();
        if (currPhi != null) {
          map.put(currPhi, bb);
        }
      }
    }
  }

  public void dumpDotFiles() {
    ExplodedCFGDotWriter.write("c:/temp/original.dt", ir.getControlFlowGraph());
    ExplodedCFGDotWriter.write("c:/temp/expanded.dt", cfg);
  }

  /**
   * initializer for SSA Inference equations.
   */
  protected void init(IR ir, VariableFactory varFactory) {

    createVariables(varFactory);
    createEdges();
    initializeVariables();

    if (DEBUG) {
      dumpDotFile(cfg);
    }

  }

  private void dumpDotFile(ExplodedControlFlowGraph cfg2) {
    // TODO Auto-generated method stub

  }

  /**
   * utility method for finding or creating a set entry in a map
   * 
   * @param m
   *          - map
   * @param key
   *          - set key
   * @return set with corresponding key in the map
   */
  protected BitVectorIntSet findOrCreateSet(Map<Integer, BitVectorIntSet> m, Integer key) {
    BitVectorIntSet result = m.get(key);
    if (result == null) {
      result = new BitVectorIntSet();
      m.put(key, result);
    }
    return result;
  }

  /**
   * creates a new edge via dictionary and add it to various lookup maps
   * 
   * @param src
   *          - edge source
   * @param dest
   *          - edge destination
   * @param isCFG
   *          - is this a CFG edge (false = this is an SSA edge)
   * @return unique identifier for the edge (provided via edge dictionary)
   */
  private int addEdge(int src, int dest, boolean isCFG) {
    int newEdge = edgeDictionary.createEdge(src, dest, isCFG);

    if (DEBUG) {
      Trace.println("AddEdge: [" + newEdge + "]: " + src + " -> " + dest + (isCFG ? "(cfg)" : "(ssa)"));
    }

    edges.add(newEdge);
    BitVectorIntSet incoming = findOrCreateSet(incomingEdges, dest);
    incoming.add(newEdge);
    if (isCFG) {
      BitVectorIntSet outgoing = findOrCreateSet(outgoingCFGEdges, src);
      outgoing.add(newEdge);
    } else {
      BitVectorIntSet outgoing = findOrCreateSet(outgoingSSAEdges, src);
      outgoing.add(newEdge);
    }
    return newEdge;
  }

  protected void createEdges() {
    // create CFG flow edges
    for (Iterator<IExplodedBasicBlock> it = cfg.iterator(); it.hasNext();) {
      IExplodedBasicBlock srcNode = it.next();
      int src = srcNode.getNumber();
      for (Iterator<? extends IExplodedBasicBlock> destIt = cfg.getSuccNodes(srcNode); destIt.hasNext();) {
        IExplodedBasicBlock destNode = (IExplodedBasicBlock) destIt.next();
        int dest = destNode.getNumber();
        Assertions.productionAssertion(src < cfg.getNumberOfNodes() && dest < cfg.getNumberOfNodes());
        if (src == dest) {
          continue;
        }
        int newEdge = addEdge(src, dest, true);
        SSAInstruction srcInst = srcNode.getInstruction();
        if (srcInst instanceof SSAConditionalBranchInstruction) {
          SSACFG origCFG = ir.getControlFlowGraph();
          // int instructionNumber = srcNode.getNumber();

          ISSABasicBlock origBlock = instruction2Oldblock.get(srcInst);
          if (origBlock != null) {

            if (Util.endsWithConditionalBranch(origCFG, origBlock) && hasTakenSuccessor(origCFG, origBlock)) {

              ISSABasicBlock origTaken = (ISSABasicBlock) Util.getTakenSuccessor(origCFG, origBlock);
              int takenNumber = origTaken.getNumber();
              for (Iterator<SSAPiInstruction> piIterator = srcNode.iteratePis(); piIterator.hasNext();) {
                SSAPiInstruction currPi = piIterator.next();
                int successorBlockNumber = currPi.getSuccessor();
                if (takenNumber == successorBlockNumber) {
                  Trace.println("added trueCasePi: " + currPi);
                  trueCasePiInstructions.add(currPi);
                }
              }
            }
          }
          // DONE: add edge to trueCaseMap if it is a positive-branch edge
          if (hasTakenSuccessor(cfg, srcNode)) {
            IExplodedBasicBlock takenDest = (IExplodedBasicBlock) Util.getTakenSuccessor(cfg, srcNode);
            if (takenDest != null && takenDest.equals(destNode)) {
              trueCaseEdges.add(newEdge);
            }
          }
        }
      }
    }

    // create SSA edges
    int mvn = st.getMaxValueNumber();
    for (int i = 0; i <= mvn; i++) {
      SSAInstruction defInst = defUse.getDef(i);
      if (defInst != null) {
        if (DEBUG) {
          Trace.println("CreatingSSAEdges for: " + defInst);
        }

        IExplodedBasicBlock defBlock = getInstructionBlock(defInst);
        for (Iterator<SSAInstruction> it = defUse.getUses(i); it.hasNext();) {
          SSAInstruction useInst = it.next();
          if (DEBUG) {
            Trace.println("--and for: " + useInst);
          }

          IExplodedBasicBlock useBlock = getInstructionBlock(useInst);

          if (defBlock != null) {
            int def = defBlock.getNumber();
            int use = useBlock.getNumber();
            Assertions.productionAssertion(def < cfg.getNumberOfNodes() && use < cfg.getNumberOfNodes());
            if (def != use) {
              addEdge(def, use, false);
            }
          }
        }
      }
    }

  }

  private boolean hasTakenSuccessor(ControlFlowGraph aCFG, IBasicBlock aBlock) {
    IBasicBlock nts = Util.getNotTakenSuccessor(aCFG, aBlock);
    for (Iterator it = aCFG.getSuccNodes(aBlock); it.hasNext();) {
      IBasicBlock s = (IBasicBlock) it.next();
      if (s != nts)
        return true;
    }
    return false;
  }

  private IExplodedBasicBlock getInstructionBlock(SSAInstruction inst) {
    IExplodedBasicBlock result = (IExplodedBasicBlock) instruction2block.get(inst);
    if (result == null) {
      Trace.println("Got null block for instruction " + inst);
    }
    return result;
  }

  protected void initialize() {
    // flow work list contains all edges emanating from entry
    flowWorkList = new BitVectorIntSet();
    // ssa work list is initially empty
    ssaWorkList = new BitVectorIntSet();
    // execFlag initialized to be empty
    execFlag = new BitVectorIntSet();

    IExplodedBasicBlock entryBlock = (IExplodedBasicBlock) cfg.entry();
    assert entryBlock.isEntryBlock();

    int src = entryBlock.getNumber();

    // OMG. The ExplodedCFG has two nodes with number = 0
    // one of them is the entry node...
    // hack the entry node to have ID -1 in our edge map
    // such that block nums in edges are unique
    src = -1;

    for (Iterator it = cfg.getSuccNodes(entryBlock); it.hasNext();) {
      IExplodedBasicBlock destBlock = (IExplodedBasicBlock) it.next();
      int dest = destBlock.getNumber();

      int edge = addEdge(src, dest, true);
      flowWorkList.add(edge);
    }

    if (DEBUG) {
      Trace.println("SCCPSolver:Initialize --- entry = " + entryBlock + "(" + src + ")");
      Trace.println("SCCPSolver:Initialize --- flowWL = " + flowWorkList.size());
    }

    // initialize defined vars to bottom value
    SSAInstruction[] instructions = cfg.getInstructions();
    int instSize = instructions.length;
    for (int i = 0; i < instSize; i++) {
      SSAInstruction inst = (SSAInstruction) instructions[i];
      initDefValue(inst);
    }
    // add pis and phis
    for (Iterator<IExplodedBasicBlock> it = cfg.iterator(); it.hasNext();) {
      ISSABasicBlock bb = (ISSABasicBlock) it.next();
      for (Iterator<SSAPhiInstruction> phiIt = bb.iteratePhis(); phiIt.hasNext();) {
        SSAPhiInstruction phiInst = phiIt.next();
        initDefValue(phiInst);
      }
      for (Iterator<SSAPiInstruction> piIt = bb.iteratePis(); piIt.hasNext();) {
        SSAPiInstruction piInst = piIt.next();
        initDefValue(piInst);
      }
    }
  }

  private void initDefValue(SSAInstruction inst) {
    if (inst != null && inst.hasDef()) {
      int defNum = inst.getDef();
      if (defNum != -1) {
        setValue(defNum, SCCPValue.BOTTOM);
      }
    }
  }

  private int selectAndRemove(BitVectorIntSet bv) {
    IntIterator it = bv.intIterator();
    int edgeId = it.next();
    bv.remove(edgeId);
    return edgeId;
  }

  public void solve() {

    if (DEBUG) {
      Trace.println("SCCPSolver:solve()");
    }

    initialize();

    if (DEBUG) {
      Trace.println("SCCPSolver:Solve() " + ir.getMethod().getName());
      Trace.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-");
      Trace.println("SCCPSolver: flowWorkList = " + flowWorkList.size());
      Trace.println("SCCPSolver: ssaWorkList = " + ssaWorkList.size());
    }

    while (!flowWorkList.isEmpty() || !ssaWorkList.isEmpty()) {

      if (!flowWorkList.isEmpty()) {
        int e = selectAndRemove(flowWorkList);
        Edge edge = edgeDictionary.get(e);

        if (DEBUG) {
          Trace.println("FlowWorkList Got edge" + e + ": " + edge);
        }

        assert edge != null;
        int b = edge.dest;
        assert b >= 0;

        if (!execFlag.contains(e)) {
          execFlag.add(e);
          IExplodedBasicBlock destBlock = (IExplodedBasicBlock) cfg.getNode(b);
          SSAInstruction destInst = destBlock.getInstruction();
          visitPhiInstructions(b, destBlock);
          if (singleEdge(b)) {
            visitInst(b, destInst);
          }
          visitPiInstructions(b, destBlock);
        }
      }
      if (!ssaWorkList.isEmpty()) {
        int e = selectAndRemove(ssaWorkList);
        Edge edge = edgeDictionary.get(e);

        if (DEBUG) {
          Trace.println("ssaWorkList Got edge" + e + ": " + edge);
        }

        assert edge != null;
        int b = edge.dest;
        Assertions.productionAssertion(b >= 0 && b < cfg.getNumberOfNodes());
        IExplodedBasicBlock destBlock = (IExplodedBasicBlock) cfg.getNode(b);
        // int destInstIndex = destBlock.getFirstInstructionIndex();
        SSAInstruction destInst = destBlock.getInstruction();

        visitPhiInstructions(b, destBlock);

        if (hasEdge(b)) {
          visitInst(b, destInst);
        }

        visitPiInstructions(b, destBlock);

      }
    }
    if (DEBUG) {
      printSolution();
    }

    if (DEBUG) {
      Trace.println("SCCPSolver:solve() Ended");
    }
  }

  private void visitPhiInstructions(int blockNum, IExplodedBasicBlock destBlock) {
    for (Iterator<SSAPhiInstruction> phiIt = destBlock.iteratePhis(); phiIt.hasNext();) {
      SSAPhiInstruction currPhi = phiIt.next();
      handlePhiDefinition(blockNum, currPhi);
    }
  }

  private void visitPiInstructions(int blockNum, IExplodedBasicBlock destBlock) {
    for (Iterator<SSAPiInstruction> piIt = destBlock.iteratePis(); piIt.hasNext();) {
      SSAPiInstruction currPi = piIt.next();
      handlePiDefinition(blockNum, currPi);
    }
  }

  private void handlePhiDefinition(int blockNum, SSAPhiInstruction inst) {
    Trace.println("HandlePhiDefinition: " + inst);
    visitPhi(blockNum, inst);
  }

  private void handlePiDefinition(int blockNum, SSAPiInstruction inst) {

    Trace.println("HandlePiDefinition: " + inst);

    int defNum = inst.getDef();
    if (defNum != -1) {
      SCCPValue lhsVal = getValue(defNum);

      Assertions.productionAssertion(lhsVal != null, "got null constant");

      SCCPValue val = latticeEvaluate(inst);
      if (!val.equals(lhsVal)) {
        lhsVal = lhsVal.join(val);
        setValue(defNum, lhsVal);
        if (getValue(defNum).equals(lhsVal)) {
          if (DEBUG) {
            Trace.println("Adding SSA Successors");
          }
          ssaWorkList.addAll(getSSASuccessors(blockNum));
        }
      }
    }
  }

  protected void printSolution() {
    if (!DEBUG) {
      return;
    }
    Trace.println("Results for: " + ir.getMethod().getSignature());
    Trace.println("=======================================================");

    int size = vars.length;
    for (int i = 1; i < size; i++) {
      SCCPVariable var = getVariable(i);
      Trace.println("Result: " + var);
    }
    // also print (again) constants identified by SSA
    int maxVal = st.getMaxValueNumber();
    for (int i = 0; i <= maxVal; i++) {
      Value val = st.getValue(i);
      if (val != null && val instanceof ConstantValue) {
        Trace.println("Result(SSACNST): " + i + " = " + val);
      }
    }
    Trace.println("=======================================================");
  }

  /**
   * get constant values identified for this method
   * 
   * @return a Map of Integer to SCCPValue (Value-number as Integer ->
   *         SCCPValue)
   */
  public Map<Integer, SCCPValue> getConstantValues() {
    Map<Integer, SCCPValue> result = HashMapFactory.make();

    int size = vars.length;
    for (int i = 1; i < size; i++) {
      result.put(i, getValue(i));
    }

    // also add constants identified by SSA
    int maxVal = st.getMaxValueNumber();
    for (int i = 0; i <= maxVal; i++) {
      Value val = st.getValue(i);
      if (val != null && val instanceof ConstantValue) {
        ConstantValue cVal = (ConstantValue) val;

        if (cVal.isNullConstant()) {
          result.put(new Integer(i), SCCPValue.createValue(null));
        } else {
          Object value = cVal.getValue();
          if (value != null) {
            result.put(new Integer(i), SCCPValue.createValue(value));
          }
        }

      }
    }
    return result;
  }

  protected SCCPValue join(SCCPValue lhs, SCCPValue rhs) {
    return lhs.join(rhs);
  }

  protected void visitPhi(int b, SSAPhiInstruction phi) {

    int lhs = phi.getDef();
    int uses = phi.getNumberOfUses();

    if (DEBUG) {
      Trace.println("Visiting " + phi);
    }

    SCCPValue lhsVal = getValue(lhs).copy();

    for (int j = 0; j < uses; j++) {
      int currUse = phi.getUse(j);
      if (currUse != -1) {
        SCCPValue currVal = getValue(currUse);
        if (currVal != null && lhsVal != null) {
          // if it is null, assume value is bottom and need not be joined
          lhsVal = join(lhsVal, currVal);
        }
      }
    }
    if (!lhsVal.equals(getValue(lhs))) {
      setValue(lhs, lhsVal);
      if (DEBUG) {
        Trace.println("Phi result updated: " + lhsVal + " got value:" + getValue(lhs));
      }
      // after setting, make sure it indeed got set
      // otherwise, value is coming from seed and we will loop forever
      if (getValue(lhs).equals(lhsVal)) {
        if (DEBUG) {
          Trace.println("Adding SSA Successors");
        }
        ssaWorkList.addAll(getSSASuccessors(b));
      }

    }
    // simply add successors to flowWorkList
    flowWorkList.addAll(getCFGSuccessors(b));
  }

  protected void visitInst(int b, SSAInstruction inst) {
    if (DEBUG) {
      Trace.println("visitInst " + inst);
    }

    if (inst == null) {
      flowWorkList.addAll(getCFGSuccessors(b));
      return;
    }

    SCCPValue val = latticeEvaluate(inst);

    checkInstruction(inst);

    if (val == null) {
      // instruction does not have a value associated with it
      // simply add successors to flowWorkList
      flowWorkList.addAll(getCFGSuccessors(b));
    } else if (inst.hasDef()) {
      int defNum = inst.getDef();
      if (defNum != -1) {
        SCCPValue lhsVal = getValue(defNum);

        Assertions.productionAssertion(lhsVal != null, "got null constant");

        if (!val.equals(lhsVal)) {
          lhsVal = lhsVal.join(val);
          setValue(defNum, lhsVal);
          if (getValue(defNum).equals(lhsVal)) {
            if (DEBUG) {
              Trace.println("Adding SSA Successors");
            }
            ssaWorkList.addAll(getSSASuccessors(b));
          }
        }
      }
    }

    // TODO: special treatment for binexp, unexp as described in page 367
    if (val == SCCPValue.TOP) {
      flowWorkList.addAll(getCFGSuccessors(b));
    } else if (val != SCCPValue.BOTTOM) {
      if (DEBUG) {
        Trace.println("Value is not bottom! " + val);
      }
      MutableIntSet cfgSucc = getCFGSuccessors(b);

      if (cfgSucc.size() > 1) {
        // if this is a boolean expression, we can do better
        // TODO: Should observe condition values here!
        for (IntIterator it = cfgSucc.intIterator(); it.hasNext();) {
          int edge = it.next();
          // TODO: refine to add only matching edges with cond-values
          if (val instanceof SCCPBooleanValue) {
            SCCPBooleanValue bVal = (SCCPBooleanValue) val;
            boolean boolVal = bVal.getBooleanValue().booleanValue();
            if ((boolVal && trueCaseEdges.contains(edge)) || (!boolVal && !trueCaseEdges.contains(edge))) {
              // TODO: add the edge
              flowWorkList.add(edge);
              if (DEBUG) {
                Trace.println("Edge is added: " + edge);
              }

            } else {
              if (DEBUG) {
                Trace.println("Edge _not_ added: " + edge);
              }
            }
          } else {
            if (DEBUG) {
              Trace.println("Edge is added, non boolean condition: " + edge);
            }
            flowWorkList.add(edge);
          }
          // TODO: when above is fixed, remove this edge-adding code
        }
      } else if (cfgSucc.size() == 1) {
        for (IntIterator it = cfgSucc.intIterator(); it.hasNext();) {
          int edge = it.next();
          flowWorkList.add(edge);
        }
      }
    }

  }

  protected void checkInstruction(SSAInstruction inst) {
    // subclasses may override this to add checking capabilties to computation
  }

  protected MutableIntSet getSSASuccessors(int node) {
    MutableIntSet result = outgoingSSAEdges.get(node);
    return (result != null ? result : EMPTY_INTSET);
  }

  protected MutableIntSet getCFGSuccessors(int node) {
    MutableIntSet result = outgoingCFGEdges.get(node);
    return (result != null ? result : EMPTY_INTSET);
  }

  protected SCCPValue latticeEvaluate(SSAInstruction inst) {
    if (DEBUG) {
      Trace.println("latticEval:" + inst + " | " + inst.toString(st));
    }
    evalVisitor.init();
    if (DEBUG) {
      Trace.println("diving to visit:" + inst + " | " + inst.toString(st));
    }
    inst.visit(evalVisitor);

    return evalVisitor.getResult();
  }

  private boolean singleEdge(int b) {
    int count = 0;
    MutableIntSet incoming = incomingEdges.get(b);
    for (IntIterator it = incoming.intIterator(); it.hasNext();) {
      int e = it.next();
      if (execFlag.contains(e)) {
        count++;
        if (count > 1) {
          if (DEBUG) {
            Trace.println("Not single edge");
          }
          return false;
        }
      }
    }
    if (DEBUG) {
      Trace.println("Is single edge");
    }
    return (count == 1);
  }

  private boolean hasEdge(int b) {
    MutableIntSet incoming = incomingEdges.get(b);
    for (IntIterator it = incoming.intIterator(); it.hasNext();) {
      int e = it.next();
      if (execFlag.contains(e)) {
        if (DEBUG) {
          Trace.println("hasEdge = true");
        }
        return true;
      }
    }
    if (DEBUG) {
      Trace.println("hasEdge = false");
    }
    return false;
  }

  /**
   * Create a dataflow variable for each value number
   */
  private void createVariables(VariableFactory factory) {
    vars = new IVariable[st.getMaxValueNumber() + 1];
    for (int i = 1; i < vars.length; i++) {
      vars[i] = factory.makeVariable(i);
    }

  }

  /**
   * @param valueNumber
   * @return the dataflow variable representing the value number, or null if
   *         none found.
   */
  protected SCCPVariable getVariable(int valueNumber) {
    if (DEBUG) {
      Trace.println("getVariable for " + valueNumber + " returns " + vars[valueNumber]);
    }
    return (SCCPVariable) vars[valueNumber];
  }

  /**
   * Return a string representation of the system
   * 
   * @return a string representation of the system
   */
  public String toString() {
    StringBuffer result = new StringBuffer("Type inference : \n");
    for (int i = 0; i < vars.length; i++) {
      result.append("v").append(i).append("  ").append(vars[i]).append("\n");
    }
    return result.toString();
  }

  protected void initializeVariables() {
    int maxValNum = st.getMaxValueNumber();

    for (int i = 1; i <= maxValNum; i++) {
      SCCPVariable v = getVariable(i);
      if (st.isParameter(i)) {
        v.setValue(SCCPValue.TOP);
        continue;
      }

      Value val = st.getValue(i);

      if (val == null) {
        v.setValue(SCCPValue.BOTTOM);
      } else if (val instanceof ConstantValue) {
        ConstantValue cval = (ConstantValue) val;
        Object theValue = cval.getValue();
        v.setValue(theValue);
      }
    }
  }

  public static final class EdgeDictionary {
    protected Map<Edge, Integer> edge2int = HashMapFactory.make();

    protected Map<Integer, Edge> int2edge = HashMapFactory.make();

    private int id = 0;

    public int createEdge(int src, int dest, boolean isCFG) {
      Edge newEdge = new Edge(src, dest, isCFG);
      Integer key = edge2int.get(newEdge);
      if (key == null) {
        key = new Integer(id++);
        edge2int.put(newEdge, key);
        int2edge.put(key, newEdge);
      }
      return key;
    }

    public Edge get(int edgeId) {
      return int2edge.get(edgeId);
    }
  }

  public static class Edge {
    public int src;

    public int dest;

    public boolean isCFG;

    public Edge(int src, int dest, boolean isCFG) {
      this.src = src;
      this.dest = dest;
      this.isCFG = isCFG;
    }

    public boolean isCFG() {
      return isCFG;
    }

    public boolean isSSA() {
      return !isCFG;
    }

    public boolean equals(Object other) {
      if (!(other instanceof Edge)) {
        return false;
      }
      Edge otherEdge = (Edge) other;
      return (otherEdge.src == src && otherEdge.dest == dest && otherEdge.isCFG == isCFG);
    }

    public int hashCode() {
      return 31 * src + dest;
    }

    public String toString() {
      return "(" + src + "," + dest + "," + (isCFG ? "CFG" : "SSA") + ")";
    }

  }

  private class SCCPVarFactory implements VariableFactory {

    public IVariable makeVariable(int valueNumber) {
      return new SCCPVariable(valueNumber, SCCPValue.BOTTOM, 797 * valueNumber);
    }
  }

  public SCCPValue getValue(int valueNumber) {
    assert getVariable(valueNumber) != null : "null variable for value number " + valueNumber;
    return (getVariable(valueNumber)).getValue();
  }

  public void setValue(int valueNumber, SCCPValue val) {
    assert getVariable(valueNumber) != null : "null variable for value number " + valueNumber;
    Trace.println("Setting the value of " + valueNumber + " to " + val);
    getVariable(valueNumber).setValue(val);
  }

  public class ExpressionInstructionVisitor extends Visitor {
    private SCCPValue result = SCCPValue.TOP;

    public void init() {
      result = SCCPValue.TOP;
    }

    public SCCPValue getResult() {
      return result;
    }

    public void visitBinaryOp(SSABinaryOpInstruction instruction) {

      // DONE: add logic for computing SCCPValue of the expression
      if (NewSCCPSolver.DEBUG) {
        Trace.println(instruction.toString());

        int rhs1 = instruction.getUse(0);
        int rhs2 = instruction.getUse(1);
        int lhs = instruction.getDef();

        SCCPValue rhs1ProgramValue = getConstantValue(rhs1);
        SCCPValue rhs2ProgramValue = getConstantValue(rhs2);

        result = (rhs1ProgramValue != null) ? rhs1ProgramValue.evaluateBinaryOp(rhs2ProgramValue) : SCCPValue.TOP;

        // DONE: we need some nasty case splitting handling various kinds of
        // constants and operators
      }

    }

    public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
      // DONE: add logic for computing SCCPValue of the expression
      if (NewSCCPSolver.DEBUG) {
        Trace.println(instruction.toString());
      }
      int rhs1 = instruction.getUse(0);
      // int lhs = instruction.getDef();

      // SCCPVariable rhs1Var = getVariable(rhs1);
      // SCCPVariable lhsVar = getVariable(lhs);

      SCCPValue rhs1ProgramValue = getConstantValue(rhs1);
      if (rhs1ProgramValue != null) {
        result = rhs1ProgramValue;
      }
    }

    public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
      int rhs1 = instruction.getUse(0);
      int rhs2 = instruction.getUse(1);
      ConditionalBranchInstruction.IOperator operator = instruction.getOperator();
      SCCPValue rhs1Value = getValue(rhs1);
      SCCPValue rhs2Value = getValue(rhs2);

      if (rhs1Value == SCCPValue.TOP || rhs2Value == SCCPValue.TOP) {
        // join of anything with TOP is TOP
        result = SCCPValue.TOP;
      } else {
        if (rhs1Value != null && rhs2Value != null && rhs2Value != SCCPValue.TOP || rhs2Value != SCCPValue.TOP) {
          result = evaluateConditionalBranch(rhs1Value, rhs2Value, operator);
        } else {
          result = SCCPValue.TOP;
        }
      }

    }

    public void visitGet(SSAGetInstruction instruction) {
      // int lhs = instruction.getDef();
      // FieldReference fieldRef = instruction.getDeclaredField();
      // int ref = instruction.getRef();
      if (instruction.isStatic()) {
        // TODO: fill
      } else {
        // TODO: fill
      }
    }

    public void visitPi(SSAPiInstruction instruction) {

      int piLhs = instruction.getDef();
      int piRhs = instruction.getVal();
      SCCPValue piRhsProgramValue = getConstantValue(piRhs);
      SCCPValue piRhsConstant = (piRhsProgramValue != null) ? piRhsProgramValue : getValue(piRhs);

      SSAInstruction cause = instruction.getCause();
      if (cause instanceof SSAConditionalBranchInstruction) {

        SSAConditionalBranchInstruction cond = (SSAConditionalBranchInstruction) cause;
        ConditionalBranchInstruction.IOperator operator = cond.getOperator();

        int rhs1 = cause.getUse(0);
        int rhs2 = cause.getUse(1);

        Assertions.productionAssertion(rhs1 == piRhs || rhs2 == piRhs, "cause condition does not contain pi variable");

        SCCPValue rhs1ProgramValue = getConstantValue(rhs1);
        SCCPValue rhs2ProgramValue = getConstantValue(rhs2);

        SCCPValue rhs1Constant = (rhs1ProgramValue != null) ? rhs1ProgramValue : getValue(rhs1);
        SCCPValue rhs2Constant = (rhs2ProgramValue != null) ? rhs2ProgramValue : getValue(rhs2);

        boolean rhs1IsConstant = (rhs1Constant != null) && (rhs1Constant != SCCPValue.TOP) && (rhs1Constant != SCCPValue.BOTTOM);
        boolean rhs2IsConstant = (rhs2Constant != null) && (rhs2Constant != SCCPValue.TOP) && (rhs2Constant != SCCPValue.BOTTOM);

        SCCPValue constant = null;

        if (!rhs1IsConstant && !rhs2IsConstant) {
          result = null;
          return;
        } else if (rhs1IsConstant && rhs2IsConstant) {
          handleConstantCondition(instruction, rhs1Constant, rhs2Constant);
          return;
        } else if (rhs1IsConstant && !rhs2IsConstant) {
          constant = rhs1Constant;
        } else if (!rhs1IsConstant && rhs2IsConstant) {
          constant = rhs2Constant;
        }

        Assertions.productionAssertion(constant != null, "got null constant");

        Trace.println("Conditional Operator: " + operator);

        switch ((ConditionalBranchInstruction.Operator) operator) {
        case EQ:
          if (isTruePi(instruction)) {
            if (DEBUG) {
              Trace.println("EQ-PI: set value of " + piLhs + " to " + constant);
            }
            setValue(piLhs, constant);
          } else {
            if (DEBUG) {
              Trace.println("EQ-PI: set value of " + piLhs + " to TOP");
            }
            setValue(piLhs, SCCPValue.TOP);
          }
          break;
        case NE:
          if (!isTruePi(instruction)) {
            if (DEBUG) {
              Trace.println("NEQ-PI: set value of " + piLhs + " to " + constant);
            }
            setValue(piLhs, constant);
          } else {
            if (DEBUG) {
              Trace.println("NEQ-PI: set value of " + piLhs + " to TOP");
            }
            setValue(piLhs, SCCPValue.TOP);
          }
          break;
        }

        result = getValue(piLhs);

      } else {
        if (piRhsConstant != null && piRhsConstant != SCCPValue.TOP && piRhsConstant != SCCPValue.BOTTOM) {
          setValue(piLhs, piRhsConstant);

          result = getValue(piLhs);

        }
      }
    }

    private void handleConstantCondition(SSAPiInstruction instruction, SCCPValue rhs1Constant, SCCPValue rhs2Constant) {
      SSAInstruction cause = instruction.getCause();
      SSAConditionalBranchInstruction cond = (SSAConditionalBranchInstruction) cause;
      ConditionalBranchInstruction.IOperator operator = cond.getOperator();
      int piLhs = instruction.getDef();

      boolean isTruePi = isTruePi(instruction);
      boolean constantsEqual = rhs1Constant.equals(rhs2Constant);

      switch ((ConditionalBranchInstruction.Operator) operator) {
      case EQ:
        Trace.println("EQ:");
        Trace.println("ConstantEqual:" + constantsEqual);
        boolean isConstant = (constantsEqual && isTruePi) || (!constantsEqual && !isTruePi);

        Trace.println("isConstant:" + isConstant);

        setValue(piLhs, isConstant ? rhs1Constant : SCCPValue.BOTTOM);
        break;
      case NE:
        Trace.println("NEQ:");
        Trace.println("ConstantEqual:" + constantsEqual);
        boolean isAssign = (!constantsEqual && isTruePi) || (constantsEqual && !isTruePi);

        Trace.println("isAssign:" + isAssign);

        setValue(piLhs, isAssign ? rhs1Constant : SCCPValue.BOTTOM);
        break;
      }
      result = getValue(piLhs);
    }

    private boolean isTruePi(SSAPiInstruction instruction) {
      boolean result = trueCasePiInstructions.contains(instruction);
      if (DEBUG) {
        Trace.println("Is true pi instruction? " + instruction + " =  " + result);
      }
      return result;
    }

    public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
    }

    private SCCPValue getConstantValue(int valNumber) {
      SCCPValue result = null;
      Value value = st.getValue(valNumber);
      if (value != null) {
        if (value instanceof ConstantValue) {
          ConstantValue constVal = (ConstantValue) value;
          result = SCCPValue.createValue(constVal.getValue());

          if (NewSCCPSolver.DEBUG) {
            Trace.println("Got constant value for " + valNumber + " = " + constVal.getValue());
            Trace.println("Result " + result.getClass() + " = " + result);
          }
        }
      }
      return result;
    }

    private SCCPValue evaluateConditionalBranch(SCCPValue op1, SCCPValue op2, ConditionalBranchInstruction.IOperator operator) {
      switch ((ConditionalBranchInstruction.Operator) operator) {
      case EQ:
        boolean outcome = op1.equals(op2);
        if (NewSCCPSolver.DEBUG) {
          Trace.println("EQ:" + op1 + " =?= " + op2 + " -> " + outcome);
          if (op1.value != null) {
            Trace.println("op1: " + op1.value.getClass());
          }
          if (op2.value != null) {
            Trace.println("op2: " + op2.value.getClass());
          }
        }
        return SCCPValue.createValue(outcome);
      case NE:
        return SCCPValue.createValue(!op1.equals(op2));
      default:
        return SCCPValue.TOP;
      }
    }

  }
}
