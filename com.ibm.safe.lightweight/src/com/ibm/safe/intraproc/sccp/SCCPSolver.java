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

import com.ibm.safe.intraproc.sccp.ExpandedControlFlowGraph.SingleInstructionBasicBlock;
import com.ibm.safe.utils.Trace;
import com.ibm.wala.dataflow.ssa.SSAInference.VariableFactory;
import com.ibm.wala.fixpoint.IVariable;
import com.ibm.wala.shrikeBT.ConditionalBranchInstruction;
import com.ibm.wala.ssa.ConstantValue;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAArrayLengthInstruction;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAComparisonInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAConversionInstruction;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstanceofInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.ssa.Value;
import com.ibm.wala.ssa.SSAInstruction.Visitor;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.BitVectorIntSet;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.MutableIntSet;
import com.ibm.wala.util.intset.MutableSparseIntSet;

/**
 * 
 * @author Eran Yahav (yahave)
 */

// TODO: migrate all SSA-edge functionality into the ExpandedControlFlowGraph
// [EY]
@Deprecated
public class SCCPSolver {

  protected static final boolean DEBUG = false;

  private static final MutableIntSet EMPTY_INTSET = MutableSparseIntSet.makeEmpty();

  /**
   * The governing SSA form
   */
  private IR ir;

  private ExpandedControlFlowGraph cfg;

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
   * create a new solver for a given IR
   * 
   * @param ir
   */
  public SCCPSolver(IR ir) {
    assert ir != null;

    if (DEBUG) {
      Trace.println("Creating SCCP Solver");
      Trace.println(ir.toString());
    }

    this.ir = ir;
    this.cfg = new ExpandedControlFlowGraph(ir);

    if (DEBUG) {
      Trace.println("--created ExpandedControlFlowGraph");
    }

    edgeDictionary = new EdgeDictionary();

    defUse = new DefUse(ir);
    this.st = ir.getSymbolTable();
    init(ir, this.new SCCPVarFactory());

    evalVisitor = new ExpressionInstructionVisitor();

    if (DEBUG) {
      Trace.println("Created SCCP Solver");
    }
  }

  /**
   * initializer for SSA Inference equations.
   */
  protected void init(IR ir, VariableFactory varFactory) {

    createVariables(varFactory);
    createEdges();
    initializeVariables();

    if (DEBUG) {
      cfg.dumpDotFile();
    }

  }

  /**
   * utility method for finding or creating a set entry in a map
   * 
   * @param m -
   *            map
   * @param key -
   *            set key
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
   * @param src -
   *            edge source
   * @param dest -
   *            edge destination
   * @param isCFG -
   *            is this a CFG edge (false = this is an SSA edge)
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
    for (Iterator it = cfg.iterator(); it.hasNext();) {
      SingleInstructionBasicBlock srcNode = (SingleInstructionBasicBlock) it.next();
      int src = srcNode.getNumber();
      for (Iterator destIt = cfg.getSuccNodes(srcNode); destIt.hasNext();) {
        SingleInstructionBasicBlock destNode = (SingleInstructionBasicBlock) destIt.next();
        int dest = destNode.getNumber();
        Assertions.productionAssertion(src < cfg.getNumberOfNodes() && dest < cfg.getNumberOfNodes());
        int newEdge = addEdge(src, dest, true);
        SSAInstruction srcInst = srcNode.getInstruction();
        if (srcInst instanceof SSAConditionalBranchInstruction) {
          // DONE: add edge to trueCaseMap if it is a positive-branch edge
          if (!cfg.isFallThroughTarget(srcNode, destNode)) {
            trueCaseEdges.add(newEdge);
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

        SingleInstructionBasicBlock defBlock = cfg.getInstructionBlock(defInst);
        for (Iterator<SSAInstruction> it = defUse.getUses(i); it.hasNext();) {
          SSAInstruction useInst = it.next();
          if (DEBUG) {
            Trace.println("--and for: " + useInst);
          }

          SingleInstructionBasicBlock useBlock = cfg.getInstructionBlock(useInst);

          int def = defBlock.getNumber();
          int use = useBlock.getNumber();
          Assertions.productionAssertion(def < cfg.getNumberOfNodes() && use < cfg.getNumberOfNodes());
          addEdge(def, use, false);
        }
      }
    }

  }

  protected void initialize() {
    // flow work list contains all edges emanating from entry
    flowWorkList = new BitVectorIntSet();
    // ssa work list is initially empty
    ssaWorkList = new BitVectorIntSet();
    // execFlag initialized to be empty
    execFlag = new BitVectorIntSet();

    SingleInstructionBasicBlock entryBlock = (SingleInstructionBasicBlock) cfg.entry();
    int src = entryBlock.getNumber();

    for (Iterator it = cfg.getSuccNodes(entryBlock); it.hasNext();) {
      SingleInstructionBasicBlock destBlock = (SingleInstructionBasicBlock) it.next();
      int dest = destBlock.getNumber();
      int edge = edgeDictionary.createEdge(src, dest, true);
      flowWorkList.add(edge);
    }

    if (DEBUG) {
      Trace.println("SCCPSolver:Initialize --- entry = " + entryBlock + "(" + src + ")");
      Trace.println("SCCPSolver:Initialize --- flowWL = " + flowWorkList.size());
    }

    // initialize defined vars to bottom value
    for (Iterator it = cfg.getAllInstructions().iterator(); it.hasNext();) {
      SSAInstruction inst = (SSAInstruction) it.next();
      if (inst != null && inst.hasDef()) {
        int defNum = inst.getDef();
        if (defNum != -1) {
          setValue(defNum, SCCPValue.BOTTOM);
        }
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

        assert(edge != null);
        int b = edge.dest;
        assert(b >= 0);

        if (!execFlag.contains(e)) {
          execFlag.add(e);
          SingleInstructionBasicBlock destBlock = (SingleInstructionBasicBlock) cfg.getNode(b);
          // int destInstIndex = destBlock.getFirstInstructionIndex();
          SSAInstruction destInst = destBlock.getInstruction();

          if (destInst instanceof SSAPhiInstruction) {
            SSAPhiInstruction phi = (SSAPhiInstruction) destInst;
            visitPhi(b, phi);
          } else if (singleEdge(b)) {
            visitInst(b, destInst);
          }
        }

      }
      if (!ssaWorkList.isEmpty()) {
        int e = selectAndRemove(ssaWorkList);
        Edge edge = edgeDictionary.get(e);

        if (DEBUG) {
          Trace.println("ssaWorkList Got edge" + e + ": " + edge);
        }

        assert(edge != null);
        int b = edge.dest;
        Assertions.productionAssertion(b >= 0 && b < cfg.getNumberOfNodes());
        SingleInstructionBasicBlock destBlock = (SingleInstructionBasicBlock) cfg.getNode(b);
        // int destInstIndex = destBlock.getFirstInstructionIndex();
        SSAInstruction destInst = destBlock.getInstruction();

        if (destInst instanceof SSAPhiInstruction) {
          SSAPhiInstruction phi = (SSAPhiInstruction) destInst;
          visitPhi(b, phi);
        } else if (hasEdge(b)) {
          visitInst(b, destInst);
        }
      }
    }
    if (DEBUG) {
      printSolution();
    }

    if (DEBUG) {
      Trace.println("SCCPSolver:solve() Ended");
    }
  }

  protected void printSolution() {
    if (!DEBUG) {
      return;
    }
    Trace.println("Results for: " + ir.getMethod().getSignature());
    Trace.println("=======================================================");
    for (Iterator<SSAInstruction> it = cfg.getAllInstructions().iterator(); it.hasNext();) {
      SSAInstruction inst = it.next();
      if (inst != null && inst.hasDef()) {
        int defNum = inst.getDef();
        SCCPVariable defVar = getVariable(defNum);
        Trace.println("Result: " + defVar);
      }
    }
    // also print constants identified by SSA
    int maxVal = st.getMaxValueNumber();
    for (int i = 0; i <= maxVal; i++) {
      Value val = st.getValue(i);
      if (val != null && val instanceof ConstantValue) {
        Trace.println("Result(*): " + i + " = " + val);
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
    for (Iterator it = cfg.getAllInstructions().iterator(); it.hasNext();) {
      SSAInstruction inst = (SSAInstruction) it.next();
      if (inst != null && inst.hasDef()) {
        int defNum = inst.getDef();
        if (defNum != -1) {
          result.put(new Integer(defNum), getValue(defNum));
        }
      }
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
      // addCFGSuccessors(flowWorkList, b);
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
          ssaWorkList.addAll(getSSASuccessors(b));
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

    // for (IntIterator it = edges.intIterator(); it.hasNext();) {
    // int e = it.next();
    // Edge curr = EdgeDictionary.get(e);
    // if (curr.dest == b && execFlag.contains(e)) {
    // count++;
    // if (count > 1) {
    // return false;
    // }
    // }
    // }
    // return (count == 1);
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
    // for (IntIterator it = edges.intIterator(); it.hasNext();) {
    // int e = it.next();
    // Edge curr = EdgeDictionary.get(e);
    // if (curr.dest == b && execFlag.contains(e)) {
    // return true;
    // }
    // }
    // return false;
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
    assert vars != null : "null vars array";
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

    public void visitGoto(SSAGotoInstruction instruction) {
    }

    public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
    }

    public void visitArrayStore(SSAArrayStoreInstruction instruction) {
    }

    public void visitBinaryOp(SSABinaryOpInstruction instruction) {

      // DONE: add logic for computing SCCPValue of the expression
      if (SCCPSolver.DEBUG) {
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
      if (SCCPSolver.DEBUG) {
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

    public void visitConversion(SSAConversionInstruction instruction) {
    }

    public void visitComparison(SSAComparisonInstruction instruction) {
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

    public void visitSwitch(SSASwitchInstruction instruction) {
    }

    public void visitReturn(SSAReturnInstruction instruction) {
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

    public void visitPut(SSAPutInstruction instruction) {
    }

    public void visitInvoke(SSAInvokeInstruction instruction) {
    }

    public void visitNew(SSANewInstruction instruction) {
    }

    public void visitArrayLength(SSAArrayLengthInstruction instruction) {
    }

    public void visitThrow(SSAThrowInstruction instruction) {
    }

    public void visitMonitor(SSAMonitorInstruction instruction) {
    }

    public void visitCheckCast(SSACheckCastInstruction instruction) {
    }

    public void visitInstanceof(SSAInstanceofInstruction instruction) {
    }

    public void visitPhi(SSAPhiInstruction instruction) {

    }

    public void visitPi(SSAPiInstruction instruction) {

      int piLhs = instruction.getDef();
      int piRhs = instruction.getVal();
      SCCPValue piRhsProgramValue = getConstantValue(piRhs);
      SCCPValue piRhsConstant = (piRhsProgramValue != null) ? piRhsProgramValue : getValue(piRhs);

      if (piRhsConstant != null && piRhsConstant != SCCPValue.TOP && piRhsConstant != SCCPValue.BOTTOM) {
        setValue(piLhs, piRhsConstant);

        result = getValue(piLhs);
        return;
      }

      SSAInstruction cause = instruction.getCause();
      if (cause instanceof SSAConditionalBranchInstruction) {

        SSAConditionalBranchInstruction cond = (SSAConditionalBranchInstruction) cause;
        ConditionalBranchInstruction.IOperator operator = cond.getOperator();

        boolean isTruePi = cfg.getTrueCasePiInstructions().contains(instruction);

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

        if ((rhs1IsConstant && rhs2IsConstant) || (!rhs1IsConstant && !rhs2IsConstant)) {
          result = null;
          return;
        } else if (rhs1IsConstant && !rhs2IsConstant) {
          constant = rhs1Constant;
        } else if (!rhs1IsConstant && rhs2IsConstant) {
          constant = rhs2Constant;
        }

        Assertions.productionAssertion(constant != null, "got null constant");

        switch ((ConditionalBranchInstruction.Operator) operator) {
        case EQ:
          if (isTruePi) {
            if (DEBUG) {
              Trace.println("EQ-PI: set value of " + piLhs + " to " + constant);
            }
            setValue(piLhs, constant);
          } else {
            setValue(piLhs, SCCPValue.TOP);
          }
          break;
        case NE:
          if (!isTruePi) {
            if (DEBUG) {
              Trace.println("NEQ-PI: set value of " + piLhs + " to " + constant);
            }
            setValue(piLhs, constant);
          } else {
            setValue(piLhs, SCCPValue.TOP);
          }
          break;
        }

        result = getValue(piLhs);

      }

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

          if (SCCPSolver.DEBUG) {
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
        if (SCCPSolver.DEBUG) {
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
