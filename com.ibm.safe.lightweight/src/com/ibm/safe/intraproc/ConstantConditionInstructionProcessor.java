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
import java.util.Map;

import com.ibm.safe.intraproc.sccp.SCCPValue;
import com.ibm.safe.processors.BaseInstructionProcessor;
import com.ibm.safe.reporting.message.Location;
import com.ibm.safe.reporting.message.Message;
import com.ibm.safe.reporting.message.MethodLocation;
import com.ibm.safe.rules.StructuralRule;
import com.ibm.safe.structural.impl.StructuralMessage;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAInstruction.Visitor;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.util.CancelException;

/**
 * Processor identifying conditional instructions that have a constant
 * condition.
 * 
 * @author Eran Yahav (yahave)
 */
public class ConstantConditionInstructionProcessor extends BaseInstructionProcessor {

  /**
   * visitor for identifying instructions with constant conditions
   */
  private ConstantConditionVisitor ccv;

  /**
   * a map Integer->SCCPValue mapping constant value numbers to their constant
   * value.
   */
  private Map<Integer, SCCPValue> methodConstants;

  /**
   * a collection of structural messages resulting from this processor
   */
  private Collection<StructuralMessage> result;

  /**
   * The rule related to this particular processor.
   */
  private final StructuralRule rule;

  /**
   * Initializes rule field and let the other fields to their default value.
   */
  public ConstantConditionInstructionProcessor(final StructuralRule structuralRule) {
    super();
    this.rule = structuralRule;
  }

  /**
   * setup the instruction processor for a specific method and context
   * @throws CancelException 
   */
  public void setup(IMethod method, Map<Integer, SCCPValue> context, IR ir) throws CancelException {
    super.setup(method, context, ir);
    this.methodConstants = (Map<Integer, SCCPValue>) context;
    ccv = new ConstantConditionVisitor(methodConstants);
  }

  /**
   * prepare for processing, initialize the result
   * 
   * @param inst -
   *            instruction for prolog processing
   */
  public void processProlog(SSAInstruction inst) {
    result = Collections.emptySet();
  }

  /**
   * Process instruction
   * 
   * @param inst -
   *            instruction to be processed
   *            ------------------------------------------------------------------------------
   *            DONE: get real variable names when this is eventually
   *            implemented. [EY] //Value val1 =
   *            ir.getSymbolTable().getValue(valNum1); //Value val2 =
   *            ir.getSymbolTable().getValue(valNum2);
   */
  public void process(SSAInstruction inst, int bcIndex) {
    assert inst != null : " cannot process null instruction";

    ccv.init();
    inst.visit(ccv);

    if (ccv.hasViolation()) {
      int valNum1 = ccv.getViolatingNum1();
      int valNum2 = ccv.getViolatingNum2();
      int lineNum = -1;
      lineNum = method.getLineNumber(bcIndex);
      String valueString = " with value(s): ";
      String valueNamesString = "names: ";

      if (valNum1 >= 0) {
        String[] val1Names = ir.getLocalNames(getInstructionIndex(inst), valNum1);
        SCCPValue rhs1val = methodConstants.get(valNum1);
        valueString = valueString + rhs1val;
        valueNamesString = valueNamesString + val1Names;
      }
      if (valNum2 >= 0) {
        String[] val2Names = ir.getLocalNames(getInstructionIndex(inst), valNum2);
        SCCPValue rhs2val = methodConstants.get(valNum2);
        valueString = valueString + "," + rhs2val;
        valueNamesString = valueNamesString + val2Names;
      }

      MethodLocation currLocation = Location.createMethodLocation(method.getDeclaringClass().getName(), method.getSelector(),
          lineNum, bcIndex);
      StructuralMessage msg = new StructuralMessage(rule, currLocation);
      result = Collections.singleton(msg);
    }
  }

  /**
   * no epilog processing
   * 
   * @param inst -
   *            instruction just processed
   */
  public void processEpilog(SSAInstruction inst) {

  }

  /**
   * get result of instruction processing
   * 
   * @return a Collection of error messages created by this processor
   */
  public Collection<? extends Message> getResult() {
    return result;
  }

  /**
   * ConstantConditionVisitor identifies conditionals that compare constant
   * values, and are therefore meaningless.
   * 
   * @author Eran Yahav (yahave)
   * 
   */
  public class ConstantConditionVisitor extends Visitor {

    /**
     * Constant values for the method. A map Integer->SCCPValue from value
     * number to its constant value as identified by previous pass of constant
     * propagation.
     */
    private Map<Integer, SCCPValue> constantValues;

    /**
     * was a violation identified for the instruction being processed?
     */
    private boolean hasViolation;

    /**
     * the value number of the first constant participating in the violation
     */
    private int violatingNum1;

    /**
     * the value number of the second constant participating in the violation
     * (optional)
     */
    private int violatingNum2;

    /**
     * create a new visitor with a Map of method constants
     * 
     * @param constantValues -
     *            map of constant values identified for the method.
     */
    public ConstantConditionVisitor(Map<Integer, SCCPValue> constantValues) {
      this.constantValues = constantValues;
    }

    /**
     * did this visitor identify a violation?
     * 
     * @return true when a violation was identified
     */
    public boolean hasViolation() {
      return hasViolation;
    }

    /**
     * @return value number of first constant
     */
    public int getViolatingNum1() {
      return violatingNum1;
    }

    /**
     * @return value number of second constant (if such constant exists)
     */
    public int getViolatingNum2() {
      return violatingNum2;
    }

    /**
     * initialize visitor.
     */
    public void init() {
      hasViolation = false;
      violatingNum1 = -1;
      violatingNum2 = -1;
    }

    /**
     * Validate a given value number: is this value number a real constant
     * (other than top). If this is a real constant, record it as a violation.
     * 
     * @param valNum -
     *            value number to be checked.
     */
    private void validate(int valNum) {
      SCCPValue val = constantValues.get(valNum);
      if (val != null && val != SCCPValue.TOP && val != SCCPValue.BOTTOM) {
        hasViolation = true;
        violatingNum1 = valNum;
      }
    }

    /**
     * Validate given value numbers: are these value numbers real constants
     * (other than top). If both are real constants, record it as a violation.
     * 
     * @param rhs1Num -
     *            value number to be checked.
     * @param rhs2Num -
     *            value number to be checked.
     * @param operator -
     *            operator
     */
    private void validate(int rhs1Num, int rhs2Num) {
      SCCPValue rhs1val = constantValues.get(rhs1Num);
      SCCPValue rhs2val = constantValues.get(rhs2Num);

      if (rhs1val != null && rhs2val != null && rhs1val != SCCPValue.TOP && rhs2val != SCCPValue.TOP && rhs1val != SCCPValue.BOTTOM
          && rhs2val != SCCPValue.BOTTOM) {
        hasViolation = true;
        violatingNum1 = rhs1Num;
        violatingNum2 = rhs2Num;
      }

      // switch(operator) {
      // case Constants.OPR_eq:
      // break;
      // case Constants.OPR_ne:
      // break;
      // case Constants.OPR_lt:
      // break;
      // case Constants.OPR_ge:
      // break;
      // case Constants.OPR_gt:
      // break;
      // case Constants.OPR_le:
      // break;
      // }

    }

    public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
      int numUses = instruction.getNumberOfUses();
      switch (numUses) {
      case 2:
        validate(instruction.getUse(0), instruction.getUse(1));
        break;
      case 1:
        validate(instruction.getUse(0));
        break;
      }
    }

    public void visitSwitch(SSASwitchInstruction instruction) {
      validate(instruction.getUse(0));
    }

  }

}