/*******************************************************************************
 * Copyright (c) 2002, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.safe.intraproc.sccp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.ibm.safe.utils.Trace;
import com.ibm.wala.cfg.ControlFlowGraph;
import com.ibm.wala.cfg.IBasicBlock;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.ISSABasicBlock;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAPiInstruction;
import com.ibm.wala.ssa.SSACFG.BasicBlock;
import com.ibm.wala.ssa.SSACFG.ExceptionHandlerBasicBlock;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.EmptyIterator;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.BitVector;
import com.ibm.wala.util.intset.IntSet;

/**
 * The ExpandedControlFlowGraph provides a convenient way of performing dataflow
 * analyses over the SSA IR. The SSA IR is efficient, but complicates analyses
 * by exposing null instructions, and by working at the (more efficient)
 * basic-blocks level.
 * 
 * The ExpandedControlFlowGraph has the following features: 1. it has a single
 * instruction per basic block (acutally, it uses SingleInstructionBasicBlocks)
 * 2. all instructions, including phi instructions, are present in the graph 3.
 * there are no null instructions in the graph, these are skipped over 4. there
 * are designated single entry and single exit nodes that do not contain any
 * instructions
 * 
 * At least at this stage, I prefer the less efficient, but more clear
 * ExpandedControlFlowGraph representation.
 * 
 * TODO: this needs MAJOR refactoring !!! [EY]
 * 
 * SJF: the treatment of phis here is extremely problematic .. this class breaks
 * IR invariants about whichPred() and phi blocks. I'm going to replace this
 * class with a new kind of ExplodedControlFlowGraph.
 * 
 * @author Eran Yahav (yahave)
 */
@Deprecated
public class ExpandedControlFlowGraph implements ControlFlowGraph<SSAInstruction,ISSABasicBlock> {
  private static final boolean DEBUG = false;

  /**
   * underlying array of basic blocks
   */
  private SingleInstructionBasicBlock[] basicBlocks;

  /**
   * map SSAInstructions to their corresponding SingleInstructionBlocks
   */
  final Map<SSAInstruction, SingleInstructionBasicBlock> instructionToBlock = HashMapFactory.make();

  /**
   * underlying IR
   */
  final private IR ir;

  /**
   * successor relation
   */
  final private Map<ISSABasicBlock, List<ISSABasicBlock>> successors;

  /**
   * predecessor relation
   */
  final private Map<ISSABasicBlock, List<ISSABasicBlock>> predecessors;

  /**
   * number of entry instruction
   */
  private int entry;

  /**
   * number of exit instruction
   */
  private int exit;

  /**
   * A set of blocks that are true-pi instructions
   */
  final private Set<SSAPiInstruction> trueCasePiInstructions = HashSetFactory.make();

  /**
   * entry block
   */
  SingleInstructionBasicBlock entryBlock;

  /**
   * exit block
   */
  SingleInstructionBasicBlock exitBlock;

  /**
   * map of fall-through targets
   */
  final private Map<IBasicBlock, IBasicBlock> fallThroughTargets = HashMapFactory.make();

  /**
   * create an ExpandedControlFlowGraph
   * 
   * @param ir -
   *            method's IR (just to avoid re-getting it)
   * @throws IllegalArgumentException
   *             if ir is null
   */
  public ExpandedControlFlowGraph(IR ir) {
    if (ir == null) {
      throw new IllegalArgumentException("ir is null");
    }
    this.ir = ir;
    if (DEBUG) {
      debugDump();
    }

    successors = HashMapFactory.make();
    predecessors = HashMapFactory.make();
    createBasicBlocks();
    createGraphEdges();
    if (DEBUG) {
      dumpBasicBlocks();
      dumpGraphEdges();
    }
  }

  /**
   * dump basic blocks into the trace file. debugging only
   */
  private void dumpBasicBlocks() {
    for (int i = 0; i < basicBlocks.length; i++) {
      Trace.println("BB[" + i + "] = " + basicBlocks[i]);
    }
  }

  /**
   * dump graph edges into the trace file debugging only
   */
  private void dumpGraphEdges() {
    Trace.println("Succ: " + successors.toString());
    Trace.println("Pred: " + predecessors.toString());
  }

  /**
   * return the Instruction-Block for the given instruction.
   * 
   * DEPRECATED: use getBlockForInstruction instead.
   * 
   * @param inst -
   *            instruction
   * @return SingleInstructionBasicBlock containing the given instruction
   */
  @Deprecated
  public SingleInstructionBasicBlock getInstructionBlock(SSAInstruction inst) {
    return instructionToBlock.get(inst);
  }

  /**
   * get all instructions in the method
   * 
   * @return a collection of SSAInstructions
   */
  public Collection<SSAInstruction> getAllInstructions() {
    return instructionToBlock.keySet();
  }

  /**
   * create single-instruction basic-blocks distinguish blocks inside exception
   * handler from other blocks.
   * 
   * This is pretty horrible.
   * 
   */
  private void createBasicBlocks() {

    List<IBasicBlock> basicBlockList = new ArrayList<IBasicBlock>();

    entry = 0; // -1;
    entryBlock = new SingleInstructionBasicBlock(entry, null, -1);
    entryBlock.setIsEntryBlock(true);
    basicBlockList.add(entryBlock);

    for (Iterator<ISSABasicBlock> it = ir.getControlFlowGraph().iterator(); it.hasNext();) {
      BasicBlock bb = (BasicBlock) it.next();
      // add phis first
      for (Iterator<SSAPhiInstruction> phiIt = bb.iteratePhis(); phiIt.hasNext();) {
        SSAPhiInstruction phi = phiIt.next();
        if (phi != null) {
          int blockNum = basicBlockList.size();
          basicBlockList.add(new SingleInstructionBasicBlock(blockNum, phi, -1));
        }
      }

      // add pis
      for (Iterator<SSAPiInstruction> piIt = bb.iteratePis(); piIt.hasNext();) {
        SSAPiInstruction pi = piIt.next();
        if (pi != null) {
          int blockNum = basicBlockList.size();
          basicBlockList.add(new SingleInstructionBasicBlock(blockNum, pi, -1));
        }
      }

      // then other instructions
      for (int i = bb.getFirstInstructionIndex(); i <= bb.getLastInstructionIndex(); i++) {
        SSAInstruction s = ir.getInstructions()[i];
        if (s != null) {
          int blockNum = basicBlockList.size();
          basicBlockList.add(new SingleInstructionBasicBlock(blockNum, s, i));
        }
      }

      // if this is an ExceptionHandlerBasicBlock, add the catch instruction
      // too
      if (bb instanceof ExceptionHandlerBasicBlock) {
        ExceptionHandlerBasicBlock ebb = (ExceptionHandlerBasicBlock) bb;
        SSAInstruction catchInst = ebb.getCatchInstruction();
        if (catchInst != null) {
          int blockNum = basicBlockList.size();
          basicBlockList.add(new SingleInstructionExceptionHandlerBlock(blockNum, catchInst));
        }
      }
    }
    exit = basicBlockList.size();
    exitBlock = new SingleInstructionBasicBlock(exit, null, -1);
    exitBlock.setIsExitBlock(true);
    basicBlockList.add(exitBlock);

    basicBlocks = new SingleInstructionBasicBlock[basicBlockList.size()];
    for (int i = 0; i < basicBlockList.size(); i++) {
      SingleInstructionBasicBlock sibb = (SingleInstructionBasicBlock) basicBlockList.get(i);
      basicBlocks[i] = sibb;
      SSAInstruction blockInstruction = sibb.getInstruction();
      if (blockInstruction != null) {
        instructionToBlock.put(blockInstruction, sibb);
      }

    }
  }

  /**
   * create graph edges
   */
  private void createGraphEdges() {

    for (Iterator<ISSABasicBlock> it = ir.getControlFlowGraph().iterator(); it.hasNext();) {
      BasicBlock bb = (BasicBlock) it.next();

      if (!basicBlockHasNonNullInstruction(bb) && bb.isEntryBlock()) {
        handleLastInstruction(bb, entryBlock);
        continue;
      }

      List<SSAInstruction> blockInstrctions = getBlockInstructions(bb);
      Object[] blockInstructionArray = blockInstrctions.toArray();

      if (bb.isEntryBlock()) {
        SSAInstruction inst = (SSAInstruction) blockInstructionArray[0];
        Assertions.productionAssertion(inst != null);
        SingleInstructionBasicBlock currBlock = getInstructionBlock(inst);
        addEdge(entryBlock, currBlock);
      }

      if (bb.isExitBlock()) {
        // TODO: treatment of instructions in the exit block seems to be wrong
        if (DEBUG) {
          Trace.println(bb + " is the exit block with " + blockInstructionArray.length + " instructions");
        }
      }

      if (!basicBlockHasPi(bb)) {
        processNoPi(bb, blockInstructionArray);
      } else {
        processWithPi(bb, blockInstructionArray);
      }
    }
  }

  /**
   * process a basic block that does not have pi nodes
   * 
   * @param bb -
   *            basic block
   * @param blockInstructionArray -
   *            instructions of the basic-block
   */
  private void processNoPi(BasicBlock bb, Object[] blockInstructionArray) {
    int size = blockInstructionArray.length;
    for (int i = 0; i < size; i++) {
      SSAInstruction inst = (SSAInstruction) blockInstructionArray[i];
      Assertions.productionAssertion(inst != null);
      SingleInstructionBasicBlock currBlock = getInstructionBlock(inst);
      if (currBlock == null) {
        Assertions.UNREACHABLE("bang " + inst);
      }
      if (i < size - 1) {
        // System.out.println("ADDED EDGE TO SUCCESSOR " + instNum);
        SSAInstruction nextInst = (SSAInstruction) blockInstructionArray[i + 1];
        Assertions.productionAssertion(nextInst != null);
        SingleInstructionBasicBlock nextBlock = getInstructionBlock(nextInst);
        addEdge(currBlock, nextBlock);
        fallThroughTargets.put(currBlock, nextBlock);
      } else {
        handleLastInstruction(bb, currBlock);
      }
    }
  }

  /**
   * process a basic block that contain pi nodes
   * 
   * @param bb -
   *            basic block
   * @param blockInstructionArray -
   *            instructions of the basic-block
   */
  private void processWithPi(BasicBlock bb, Object[] blockInstructionArray) {
    int size = blockInstructionArray.length;
    for (int i = 0; i < size; i++) {
      SSAInstruction inst = (SSAInstruction) blockInstructionArray[i];
      Assertions.productionAssertion(inst != null);
      SingleInstructionBasicBlock currBlock = getInstructionBlock(inst);
      if (i < size - 1 && !(inst instanceof SSAPiInstruction)) {
        int j = i + 1;
        SSAInstruction nextInst = (SSAInstruction) blockInstructionArray[j];
        while (j < size - 1 && nextInst instanceof SSAPiInstruction) {
          j = j + 1;
          nextInst = (SSAInstruction) blockInstructionArray[j];
        }
        Assertions.productionAssertion(nextInst != null);
        SingleInstructionBasicBlock nextBlock = getInstructionBlock(nextInst);
        if (!nextBlock.isPiBlock()) {
          addEdge(currBlock, nextBlock);
          fallThroughTargets.put(currBlock, nextBlock);
        }
      } else if (i == size - 1) {
        for (Iterator<SSAPiInstruction> it = bb.iteratePis(); it.hasNext();) {
          SSAPiInstruction pi = it.next();
          SingleInstructionBasicBlock piBlock = getInstructionBlock(pi);

          int piSuccNumber = pi.getSuccessor();
          int currBlockNum = bb.getNumber();
          if (currBlockNum + 1 != piSuccNumber) {
            trueCasePiInstructions.add(pi);
          } else {
            fallThroughTargets.put(currBlock, piBlock);
          }

          addEdge(currBlock, piBlock);
        }
      } else {
        handleLastInstruction(bb, currBlock);
      }
    }
  }

  /**
   * some code is required here in order to "hop-over" dead blocks that only
   * contain null-instructions.
   * 
   * @param bb -
   *            basic block
   * @param instbb -
   *            an instruction basic-block (SingleInstructionBasicBlock)
   */
  private void handleLastInstruction(BasicBlock bb, SingleInstructionBasicBlock instbb) {
    assert instbb != null;
    // Set successorInstructions = HashSetFactory.make();
    Set<BBEdge> edgeWorkSet = HashSetFactory.make();

    // create initial set of edges
    if (!instbb.isPiBlock()) {
      for (Iterator<ISSABasicBlock> sit = ir.getControlFlowGraph().getSuccNodes(bb); sit.hasNext();) {
        BasicBlock succNode = (BasicBlock) sit.next();
        boolean fallThrough = isFallThroughEdge(bb, succNode);
        edgeWorkSet.add(new BBEdge(bb, succNode, fallThrough));
      }
    } else {
      SSAPiInstruction pi = (SSAPiInstruction) instbb.getInstruction();
      int succNum = pi.getSuccessor();
      BasicBlock succNode = (BasicBlock) ir.getControlFlowGraph().getNode(succNum);
      boolean fallThrough = isFallThroughEdge(bb, succNode);
      edgeWorkSet.add(new BBEdge(bb, succNode, fallThrough));
    }

    if (bb.isExitBlock()) {
      addEdge(instbb, exitBlock);
    }

    while (!edgeWorkSet.isEmpty()) {
      // select and remove edge
      Iterator<BBEdge> workIt = edgeWorkSet.iterator();
      BBEdge edge = workIt.next();
      boolean fallThru = false;
      if (isFallThroughEdge(edge.src, edge.dest)) {
        fallThru = true;
      }
      workIt.remove();

      if (edge.dest.isEntryBlock()) {
        addEdge(instbb, entryBlock);
        if (fallThru) {
          fallThroughTargets.put(instbb, entryBlock);
        }
      } else if (edge.dest.isExitBlock()) {
        // should go to the entry of the exit block
        if (basicBlockHasNonNullInstruction(edge.dest)) {
          SSAInstruction succInst = getBasicBlockEntry(edge.dest);
          SingleInstructionBasicBlock succBlock = getInstructionBlock(succInst);
          addEdge(instbb, succBlock);
          if (fallThru) {
            fallThroughTargets.put(instbb, succBlock);
          }
        } else {
          // DONE: this is still broken when exit block has no instructions ??
          // FIXED BY ADDING THIS ELSE BRANCH [EY]
          addEdge(instbb, exitBlock);
          if (fallThru) {
            fallThroughTargets.put(instbb, exitBlock);
          }
        }
      } else if (basicBlockHasNonNullInstruction(edge.dest)) {
        SSAInstruction succInst = getBasicBlockEntry(edge.dest);
        SingleInstructionBasicBlock succBlock = getInstructionBlock(succInst);
        addEdge(instbb, succBlock);
        if (fallThru) {
          fallThroughTargets.put(instbb, succBlock);
        }
      } else {
        // we should skip the block, and add its successor to the
        // workset
        for (Iterator<ISSABasicBlock> sit = ir.getControlFlowGraph().getSuccNodes(edge.dest); sit.hasNext();) {
          BasicBlock succNode = (BasicBlock) sit.next();
          // preserve the "fallthrough" label
          edgeWorkSet.add(new BBEdge(bb, succNode, edge.isFallThrough));
        }
      }

    }
  }

  /**
   * get a set of true-case pi instructions
   * 
   * @return a set of SSAPiInstructions
   */
  public Set<SSAPiInstruction> getTrueCasePiInstructions() {
    return trueCasePiInstructions;
  }

  /**
   * Edge between basic-blocks
   * 
   * @author Eran Yahav (yahave)
   */
  private class BBEdge {
    /**
     * is this a fallthrough edge?
     */
    final public boolean isFallThrough;

    /**
     * source basic-block
     */
    final public BasicBlock src;

    /**
     * destination basic-block
     */
    final public BasicBlock dest;

    /**
     * create a new Edge
     * 
     * @param src -
     *            source BB
     * @param dest -
     *            destination BB
     * @param isFallThrough -
     *            is the edge a fallthrough edge?
     */
    public BBEdge(BasicBlock src, BasicBlock dest, boolean isFallThrough) {
      this.src = src;
      this.dest = dest;
      this.isFallThrough = isFallThrough;
    }

    /**
     * is this BBEdge equal to another object?
     * 
     * @param other -
     *            another object to be compared with
     * @return true if BBEdge equals to another given BBEdge, false otherwise
     */
    @Override
    public boolean equals(Object other) {
      if (!(other instanceof BBEdge)) {
        return false;
      }
      BBEdge otherEdge = (BBEdge) other;
      return src.equals(otherEdge.src) && dest.equals(otherEdge.dest) && isFallThrough == otherEdge.isFallThrough;
    }

    /**
     * @return BBEdge hashcode
     */
    @Override
    public int hashCode() {
      return src.hashCode() + dest.hashCode();
    }

    /**
     * @return string representation of the BBEdge
     */
    @Override
    public String toString() {
      return src + " -> " + dest;
    }

  }

  /**
   * return all the instructions in a given basic-block
   * 
   * @param bb -
   *            basic-block
   * @return a List of SSAInstructions
   */
  private List<SSAInstruction> getBlockInstructions(BasicBlock bb) {
    List<SSAInstruction> result = new ArrayList<SSAInstruction>();
    // add phis first
    for (Iterator<SSAPhiInstruction> phiIt = bb.iteratePhis(); phiIt.hasNext();) {
      SSAPhiInstruction phi = (SSAPhiInstruction) phiIt.next();
      if (phi != null) {
        result.add(phi);
      }
    }

    // add pis
    for (Iterator<SSAPiInstruction> piIt = bb.iteratePis(); piIt.hasNext();) {
      SSAPiInstruction pi = (SSAPiInstruction) piIt.next();
      if (pi != null) {
        result.add(pi);
        if (DEBUG) {
          Trace.println("PINODE added");
        }
      }
    }

    // then other instructions
    for (int i = bb.getFirstInstructionIndex(); i <= bb.getLastInstructionIndex(); i++) {
      SSAInstruction s = ir.getInstructions()[i];
      if (s != null) {
        result.add(s);
      }
    }

    // if this is an ExceptionHandlerBasicBlock, add the catch instruction
    // too
    if (bb instanceof ExceptionHandlerBasicBlock) {
      ExceptionHandlerBasicBlock ebb = (ExceptionHandlerBasicBlock) bb;
      SSAInstruction catchInst = ebb.getCatchInstruction();
      if (catchInst != null) {
        result.add(catchInst);
      }
    }

    return result;
  }

  /**
   * until DOMO is fixed TODO: once hasPhi is fixed, migrate to use it
   * 
   * @param bb -
   *            basic-block
   * @return true when basic-block has phi nodes, false otherwise
   */
  private boolean basicBlockHasPhi(BasicBlock bb) {
    List<SSAInstruction> result = new ArrayList<SSAInstruction>();
    for (Iterator<SSAPhiInstruction> phiIt = bb.iteratePhis(); phiIt.hasNext();) {
      SSAPhiInstruction phi = phiIt.next();
      if (phi != null) {
        result.add(phi);
      }
    }
    return !result.isEmpty();
  }

  /**
   * check whether a basic block has non-null instructions
   * 
   * @param bb -
   *            basic block
   * @return true when basic block has non-null instruction, false otherwise.
   */
  private boolean basicBlockHasNonNullInstruction(BasicBlock bb) {
    for (Iterator<SSAInstruction> it = bb.iterator(); it.hasNext();) {
      SSAInstruction inst = (SSAInstruction) it.next();
      if (inst != null) {
        return true;
      }
    }
    return false;
  }

  /**
   * check whether basic-block contains a pi node
   * 
   * @param bb -
   *            basic block
   * @return true when basic-block contains a pi node, false otherwise.
   */
  private boolean basicBlockHasPi(BasicBlock bb) {
    List<SSAInstruction> result = new ArrayList<SSAInstruction>();
    for (Iterator<SSAPiInstruction> piIt = bb.iteratePis(); piIt.hasNext();) {
      SSAPiInstruction pi = piIt.next();
      if (pi != null) {
        result.add(pi);
      }
    }
    return !result.isEmpty();
  }

  /**
   * returns a basic-block entry the BB entry is either: 1. if block has no
   * phis, this is the number of the instructiona at firstInstructionIndex 2. if
   * block has phi, it is the number of the first phi instruction
   * 
   * TODO: once hasPhi is fixed, migrate to use it
   * 
   * @param bb -
   *            basic-block
   * @return SSAInstruction which is the basic-block's entry
   */
  private SSAInstruction getBasicBlockEntry(BasicBlock bb) {
    if (!basicBlockHasPhi(bb)) {

      int origIndex = bb.getFirstInstructionIndex();
      SSAInstruction origInst = ir.getInstructions()[origIndex];
      if (origInst == null) {
        int i = origIndex;
        while (origInst == null && i < bb.getLastInstructionIndex()) {
          i++;
          origInst = ir.getInstructions()[i];
        }
      }
      if (bb.isCatchBlock() && origInst == null) {
        origInst = ((ExceptionHandlerBasicBlock) bb).getCatchInstruction();
      }

      Assertions.productionAssertion(origInst != null);

      return origInst;
    } else {
      SSAInstruction origInst = null;
      for (Iterator<SSAPhiInstruction> it = bb.iteratePhis(); it.hasNext() && origInst == null;) {
        origInst = it.next();
      }
      Assertions.productionAssertion(origInst != null);
      return origInst;
    }
  }

  /**
   * add an edge to the expanded graph
   * 
   * @param src
   * @param dest
   */
  private void addEdge(SingleInstructionBasicBlock src, SingleInstructionBasicBlock dest) {
    if (successors.get(src) == null) {
      successors.put(src, new ArrayList<ISSABasicBlock>());
    }
    if (predecessors.get(dest) == null) {
      predecessors.put(dest, new ArrayList<ISSABasicBlock>());
    }

    List<ISSABasicBlock> succList = successors.get(src);
    succList.add(dest);
    List<ISSABasicBlock> predList = predecessors.get(dest);
    predList.add(src);
  }

  /**
   * get successors of a basic block
   * 
   * @param src -
   *            source basic block
   * @return a List of successor basic-blocks
   */
  public List<ISSABasicBlock> getSuccessors(BasicBlock src) {
    return successors.get(src);
  }

  /**
   * get predecessors of a basic block
   * 
   * @param dest -
   *            destination basic block (starting point)
   * @return a List of predecessor basic-blocks
   */
  public List<ISSABasicBlock> getPredecessors(BasicBlock dest) {
    return predecessors.get(dest);
  }

  /**
   * Return the entry basic block in the CFG
   */
  public ISSABasicBlock entry() {
    return basicBlocks[entry];
  }

  /**
   * @return the synthetic exit block for the cfg
   */
  public ISSABasicBlock exit() {
    return basicBlocks[exit];
  }

  /**
   * @return the indices of the catch blocks, as a bit vector
   */
  public BitVector getCatchBlocks() {
    // todo ... this can't be correct, can it?
    return ir.getControlFlowGraph().getCatchBlocks();
  }

  /**
   * @param index
   *            an instruction index from the original SSA IR
   * @return the basic block which contains this instruction.
   */
  public ISSABasicBlock getBlockForInstruction(int index) {
    // this is horrible and ugly.
    SSAInstruction s = null;
    // find the first non-null instruction following the index, since this class
    // elides null instructions.
    do {
      s = ir.getInstructions()[index++];
    } while (s == null);
    return instructionToBlock.get(s);
  }

  /**
   * @return the instructions of this CFG, as an array.
   */
  public SSAInstruction[] getInstructions() {
    return ir.getControlFlowGraph().getInstructions();
  }

  /**
   * @param index
   *            an instruction index
   * @return the program counter (bytecode index) corresponding to that
   *         instruction
   */
  public int getProgramCounter(int index) {
    return ir.getControlFlowGraph().getProgramCounter(index);
  }

  /**
   * @return the Method this CFG represents
   */
  public IMethod getMethod() {
    return ir.getMethod();
  }

  /**
   * @param b
   * @return the basic blocks which may be reached from b via exceptional
   *         control flow
   */
  public List<ISSABasicBlock> getExceptionalSuccessors(ISSABasicBlock b) {
    return ir.getControlFlowGraph().getExceptionalSuccessors(b);
  }

  /**
   * @param b
   * @return the basic blocks which may be reached from b via normal control
   *         flow
   */
  public Collection<ISSABasicBlock> getNormalSuccessors(ISSABasicBlock b) {
    return Collections.unmodifiableCollection(successors.get(b));
  }

  /**
   * @param node -
   *            source node
   */
  public int getPredNodeCount(ISSABasicBlock node) {
    List<ISSABasicBlock> l = predecessors.get(node);
    return l == null ? 0 : l.size();
  }

  /**
   * @param node -
   *            source node
   * @return iterator over successor nodes
   */
  public Iterator<ISSABasicBlock> getSuccNodes(ISSABasicBlock node) {
    List<ISSABasicBlock> succNodes = successors.get(node);
    if (succNodes != null) {
      return succNodes.iterator();
    } else {
      List<ISSABasicBlock> l = Collections.emptyList();
      return l.iterator();
    }
  }

  /**
   * @param node -
   *            source node
   * @return number of successor nodes
   */
  public int getSuccNodeCount(ISSABasicBlock node) {
    return successors.get(node).size();
  }

  /**
   * unsupported operation
   * 
   * @throws UnsupportedOperationException
   *             unconditionally
   */
  public void addEdge(ISSABasicBlock src, ISSABasicBlock dst) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * unsupported operation
   */
  public void removeEdge(ISSABasicBlock src, ISSABasicBlock dst) {
    throw new UnsupportedOperationException();
  }

  /**
   * unsupported operation
   */
  public void removeAllIncidentEdges(ISSABasicBlock node) {
    throw new UnsupportedOperationException();
  }

  /**
   * unsupported operation
   */
  public void removeIncomingEdges(ISSABasicBlock node) {
    throw new UnsupportedOperationException();
  }

  /**
   * unsupported operation
   */
  public void removeOutgoingEdges(ISSABasicBlock node) {
    throw new UnsupportedOperationException();
  }

  /**
   * unsupported operation
   */
  public Iterator<ISSABasicBlock> iterateNodes(IntSet set) {
    throw new UnsupportedOperationException();
  }

  /**
   * unsupported operation
   */
  public boolean containsNode(ISSABasicBlock n) {
    throw new UnsupportedOperationException();
  }

  /**
   * unsupported operation
   */
  public int getMaxNumber() {
    throw new UnsupportedOperationException();
  }

  public Iterator<ISSABasicBlock> iterator() {
    List<ISSABasicBlock> a = new ArrayList<ISSABasicBlock>();
    for (SingleInstructionBasicBlock b : basicBlocks) {
      a.add(b);
    }
    return a.iterator();
  }

  /**
   * unsupported operation
   * 
   * @param obj -
   *            object to be removed
   */
  public void removeNode(ISSABasicBlock obj) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param node -
   *            source node
   * @return iterator over predecssors of given node
   */
  public Iterator<ISSABasicBlock> getPredNodes(ISSABasicBlock node) {
    List<ISSABasicBlock> predNodes = predecessors.get(node);
    if (predNodes != null) {
      return predNodes.iterator();
    } else {
      return EmptyIterator.instance();
    }
  }

  /**
   * unsupported operation
   * 
   * @param n
   * @throws UnsupportedOperationException
   *             unconditionally
   */
  public void addNode(ISSABasicBlock n) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /**
   * unsupported operation
   * 
   * @param n
   */
  public void removeNodeAndEdges(ISSABasicBlock n) {
    throw new UnsupportedOperationException();
  }

  /**
   * unsupported operation
   * 
   * @param n
   */
  public int getNumber(ISSABasicBlock n) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param i -
   *            number of node to be returned
   * @return the i-th node
   */
  public ISSABasicBlock getNode(int i) {
    return basicBlocks[i];
  }

  /**
   * @return number of nodes in the graph
   */
  public int getNumberOfNodes() {
    return basicBlocks.length;
  }

  /**
   * is the given edge a fall-through edge?
   * 
   * @return note: this is based on a WALA-invariant that guarantees the blocks
   *         are numbered in a certain way. This is the "WALA-way" of
   *         identifying fall-through edges, and is mainly done due for
   *         efficiency reasons (as many other software crimes).
   */
  private boolean isFallThroughEdge(ISSABasicBlock src, ISSABasicBlock dest) {
    if (ir.getControlFlowGraph().getSuccNodeCount(src) == 2) {
      return ((src.getNumber() + 1) == dest.getNumber());
    } else {
      return false;
    }
  }

  public boolean isFallThroughTarget(IBasicBlock src, IBasicBlock dest) {
    IBasicBlock ftt = getFallThroughTarget(src);
    return (ftt != null) && ftt.equals(dest);
  }

  /**
   * get the fall-through target of a given source block
   * 
   * @param src -
   *            source block
   * @return the fall-through target of the given src block
   */
  public IBasicBlock getFallThroughTarget(IBasicBlock src) {
    return fallThroughTargets.get(src);
  }

  /**
   * SingleInstructionBasicBlock A basic-block containing a single instructions.
   * These are used as building-blocks of the ExpandedControlFlowGraph.
   * 
   * @author Eran Yahav (yahave)
   */
  public class SingleInstructionBasicBlock implements ISSABasicBlock {
    /**
     * block number
     */
    private int number;

    /**
     * instruction contained in the block (a single instruction!)
     */
    final private SSAInstruction instruction;

    /**
     * If the instruction is a normal instruction, then the index of said
     * instruction in the SSA instruction array. If the instruction is a phi or
     * pi, then this is -1.
     */
    private final int normalInstructionIndex;

    /**
     * is this an exit block?
     */
    private boolean isExit = false;

    /**
     * is this an entry block?
     */
    private boolean isEntry = false;

    /**
     * create a new SingleInstructionBasicBlock
     * 
     * @param number -
     *            block number
     * @param instruction -
     *            contained instruction
     */
    public SingleInstructionBasicBlock(int number, SSAInstruction instruction, int normalInstructionIndex) {
      this.number = number;
      this.instruction = instruction;
      this.normalInstructionIndex = normalInstructionIndex;
    }

    /**
     * get block instruction
     * 
     * @return instruction in the block
     */
    public SSAInstruction getInstruction() {
      return instruction;
    }

    /**
     * get the index of the first (and only) normal instruction in the block
     */
    public int getFirstInstructionIndex() {
      return normalInstructionIndex;
    }

    /**
     * get the index of the last (and only) normal instruction in the block
     */
    public int getLastInstructionIndex() {
      return normalInstructionIndex;
    }

    /**
     * is this a catch block?
     * 
     * @return false, this class does not represent catch blocks
     */
    public boolean isCatchBlock() {
      return false;
    }

    /**
     * does this single-instruction block contains a pi-instruction?
     * 
     * @return true if it is a Pi instruction
     */
    public boolean isPiBlock() {
      return (instruction instanceof SSAPiInstruction);
    }

    /**
     * set this block as an entry block
     * 
     * @param val -
     *            new value
     */
    public void setIsEntryBlock(boolean val) {
      isEntry = val;
    }

    /**
     * set this block as exit block
     * 
     * @param val -
     *            new value
     */
    public void setIsExitBlock(boolean val) {
      isExit = val;
    }

    /**
     * is this block an exit block?
     * 
     * @return true if this is an exit block, false otherwise
     */
    public boolean isExitBlock() {
      return isExit;
    }

    /**
     * is this block an entry block?
     * 
     * @return true if this is an entry block, false otherwise
     */
    public boolean isEntryBlock() {
      return isEntry;
    }

    /**
     * get the method containing this block
     * 
     * @return IMethod of the method containing this block
     */
    public IMethod getMethod() {
      return ir.getMethod();
    }

    /**
     * @return the {@link ExpandedControlFlowGraph} to which this block belongs
     */
    public ExpandedControlFlowGraph getCFG() {
      return ExpandedControlFlowGraph.this;
    }

    /**
     * get number of the block
     * 
     * @return block number
     */
    public int getNumber() {
      return number;
    }

    /**
     * get graph-node ID
     * 
     * @return graph-node ID (same as block number)
     */
    public int getGraphNodeId() {
      return number;
    }

    /**
     * set the graph-node ID
     * 
     * @param id -
     *            node ID to be set.
     */
    public void setGraphNodeId(int id) {
      number = id;
    }

    public Iterator<SSAPhiInstruction> iteratePhis() {
      throw new UnsupportedOperationException("NYI");
    }

    public Iterator<SSAPiInstruction> iteratePis() {
      throw new UnsupportedOperationException("NYI");
    }

    public Iterator<SSAInstruction> iterator() {
      Collection<SSAInstruction> s = Collections.singleton(instruction);
      return s.iterator();
    }

    @Override
    public boolean equals(Object other) {
      if (!(other instanceof SingleInstructionBasicBlock)) {
        return false;
      }
      SingleInstructionBasicBlock otherBlock = (SingleInstructionBasicBlock) other;
      return (otherBlock.number == number);
    }

    @Override
    public int hashCode() {
      return number;
    }

    @Override
    public String toString() {
      if (isEntry) {
        return "entry";
      } else if (isExit) {
        return "exit";
      } else {
        return number + " : " + instruction.toString();
      }
    }

    public SSAInstruction getLastInstruction() {
      return instruction;
    }

    public Iterator<TypeReference> getCaughtExceptionTypes() {
      // TODO Auto-generated method stub
      return null;
    }

  }

  /**
   * SingleInstructionExceptionHandlerBlock BB for exception handler.
   * 
   * @author Eran Yahav (yahave)
   */
  public class SingleInstructionExceptionHandlerBlock extends SingleInstructionBasicBlock {
    /**
     * create a new exception handler block.
     * 
     * @param number -
     *            number of block
     * @param instruction -
     *            instruction contained in the block
     */
    public SingleInstructionExceptionHandlerBlock(int number, SSAInstruction instruction) {
      // note that the single instruction, GetCaughtException, is not in the
      // normal
      // ssa instruction array, so we pass -1 as the normalInstructionIndex
      super(number, instruction, -1);
    }

    /**
     * is this block a catch block?
     * 
     * @return true, since SingleInstructionExceptionHandlerBlock is always a
     *         catch block.
     */
    @Override
    public boolean isCatchBlock() {
      return true;
    }
  }

  /**
   * dump dot files of the current CFG and the underlying original CFG. note:
   * only used for debugging.
   */
  public void dumpDotFile() {
    ExpandedCFGDotWriter.write("c:/temp/original.dt", ir.getControlFlowGraph());
    ExpandedCFGDotWriter.write("c:/temp/expanded.dt", this);
  }

  /**
   * dump debug information. note: only used for debugging.
   */
  private void debugDump() {
    Trace.println("IR INST: " + ir.getInstructions().length);
    Trace.println("CFG INST: " + ir.getControlFlowGraph().getInstructions().length);

    SSAInstruction[] debugInstIR = ir.getInstructions();

    int countDebugIR = 0;
    for (int i = 0; i < debugInstIR.length; i++) {
      if (debugInstIR[i] != null) {
        countDebugIR++;
      }
    }

    Trace.println("The countDebugIR is:" + countDebugIR);

    int countInst = 0;
    for (Iterator<SSAInstruction> it = ir.iterateAllInstructions(); it.hasNext();) {
      SSAInstruction inst = (SSAInstruction) it.next();
      if (inst == null) {
        Trace.println("*KABOOM*");
      }
      countInst++;
    }

    Trace.println("The countInst is:" + countInst);

    for (Iterator<? extends SSAInstruction> debugIt = ir.iteratePhis(); debugIt.hasNext();) {
      SSAPhiInstruction phi = (SSAPhiInstruction) debugIt.next();
      Trace.println(phi);
    }
  }

  public Collection<ISSABasicBlock> getExceptionalPredecessors(ISSABasicBlock b) {
    Assertions.UNREACHABLE();
    return null;
  }

  public Collection<ISSABasicBlock> getNormalPredecessors(ISSABasicBlock b) {
    Assertions.UNREACHABLE();
    return null;
  }

  public boolean hasEdge(ISSABasicBlock src, ISSABasicBlock dst) {
    // TODO Auto-generated method stub
    Assertions.UNREACHABLE();
    return false;
  }

  public IntSet getSuccNodeNumbers(ISSABasicBlock node) {
    // TODO Auto-generated method stub
    Assertions.UNREACHABLE();
    return null;
  }

  public IntSet getPredNodeNumbers(ISSABasicBlock node) {
    // TODO Auto-generated method stub
    Assertions.UNREACHABLE();
    return null;
  }

  public Stream<ISSABasicBlock> stream() {
    // TODO Auto-generated method stub
    Assertions.UNREACHABLE();
    return null;
  }

}
