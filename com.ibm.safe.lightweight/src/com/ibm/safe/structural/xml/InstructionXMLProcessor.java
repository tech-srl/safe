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
 * Created on Jan 4, 2005
 */
package com.ibm.safe.structural.xml;

import java.util.Collection;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ibm.safe.intraproc.sccp.SCCPValue;
import com.ibm.safe.processors.InstructionProcessor;
import com.ibm.safe.reporting.message.Message;
import com.ibm.wala.analysis.typeInference.TypeAbstraction;
import com.ibm.wala.analysis.typeInference.TypeInference;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
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
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.ssa.Value;
import com.ibm.wala.ssa.SSAInstruction.Visitor;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;

/**
 * @author Eran Yahav (yahave)
 * 
 */
public class InstructionXMLProcessor implements InstructionProcessor {

  public static final String ID_TAG = "id";

  public static final String TYPE_TAG = "type";

  public static final String VALUE_TAG = "value";

  public static final String PARAM_TAG = "param";

  public static final String RHS1_TAG = "rhs1";

  public static final String RHS2_TAG = "rhs2";

  public static final String OPCODE_TAG = "opcode";

  public static final String LHS_TAG = "lhs";

  public static final String TARGET_TAG = "target";

  public static final String OPERAND_TAG = "op";

  public static final String GOTO_INST = "goto";

  public static final String ARRAY_LOAD_INST = "arrayload";

  public static final String ARRAY_STORE_INST = "arraystore";

  public static final String BINARY_OP_INST = "binaryop";

  public static final String UNARY_OP_INST = "unaryop";

  public static final String CONVERSION_INST = "conversion";

  public static final String COMPARISON_INST = "comparison";

  public static final String CONDITIONAL_BRANCH_INST = "condbranch";

  public static final String SWITCH_INST = "switch";

  public static final String RETURN_INST = "return";

  public static final String GET_INST = "get";

  public static final String PUT_INST = "put";

  public static final String INVOKE_INST = "invoke";

  public static final String NEW_INST = "new";

  public static final String ARRAY_LEN_INST = "arraylen";

  public static final String THROW_INST = "throw";

  public static final String MONITOR_ENTER_INST = "monitorenter";

  public static final String MONITOR_EXIT_INST = "monitorexit";

  public static final String CHECK_CAST_INST = "checkcast";

  public static final String INSTANCEOF_INST = "instanceof";

  public static final String PHI_INST = "phi";

  public static final String PI_INST = "pi";

  public static final String GET_CAUGHT_EXCEPTION_INST = "getcaught";

  public static final String RETURN_TYPE = "rettype";

  public static final String USED_ATTR = "used";

  public static final String LINE_ATTR = "line";

  public static final String LOAD_CLASS_INST = "load_class";

  private XMLInstructionVisitor visitor = new XMLInstructionVisitor();

  public void setDocument(Document doc) {
    visitor.setDocument(doc);
  }

  public void setEnvironment(IR ir) {
    visitor.setEnvironment(ir);
  }

  public void setup(IMethod method, Map<Integer, SCCPValue> context, IR ir) {

  }

  public Element getResultElement() {
    return visitor.getResultElement();
  }

  public void processProlog(SSAInstruction instruction) {

  }

  public void processEpilog(SSAInstruction instruction) {

  }

  public void process(SSAInstruction instruction, int bcIndex) {
    this.visitor.setBcIndex(bcIndex);
    instruction.visit(visitor);
  }

  public static class XMLInstructionVisitor extends Visitor {

    protected Element result;

    protected Document doc;

    private SymbolTable st;

    private TypeInference typeInference;

    private int bcIndex;

    public void setEnvironment(IR ir) {
      st = ir.getSymbolTable();
      typeInference = TypeInference.make(ir, true);
    }

    public void setDocument(Document doc) {
      this.doc = doc;
    }

    public Element getResultElement() {
      return result;
    }

    protected Element createOperandElement(Document doc, String tag, int valNumber) {
      Element resultElement = XMLDOMUtils.createTaggedElement(doc, tag, valNumber);
      if (valNumber == -1) {
        return resultElement;
      }

      TypeAbstraction operandType = typeInference.getType(valNumber);

      // do we have information in the symbol table?
      if (st != null && st.isConstant(valNumber)) {
        if (st.isStringConstant(valNumber)) {
          resultElement.appendChild(XMLDOMUtils.createTaggedElement(doc, TYPE_TAG, "Ljava/lang/String"));
        } else if (st.isFloatConstant(valNumber)) {
          resultElement.appendChild(XMLDOMUtils.createTaggedElement(doc, TYPE_TAG, "float"));
        } else if (st.isDoubleConstant(valNumber)) {
          resultElement.appendChild(XMLDOMUtils.createTaggedElement(doc, TYPE_TAG, "double"));
        } else if (st.isIntegerConstant(valNumber)) {
          resultElement.appendChild(XMLDOMUtils.createTaggedElement(doc, TYPE_TAG, "int"));
        }
      } else if (operandType != null) {
        if (operandType.getType() != null) {
          resultElement.appendChild(XMLDOMUtils.createTaggedElement(doc, TYPE_TAG, operandType.getType().getName().toString()));
        } else {
          resultElement.appendChild(XMLDOMUtils.createTaggedElement(doc, TYPE_TAG, operandType.toString()));
        }
      }

      if (st != null) {
        Value operandValue = st.getValue(valNumber);
        if (operandValue != null) {
          resultElement.appendChild(XMLDOMUtils.createTaggedElement(doc, VALUE_TAG, operandValue.toString()));
        }
      }
      return resultElement;
    }

    public void visitGoto(SSAGotoInstruction instruction) {
      result = doc.createElement(GOTO_INST);

      // BasicBlock instBB = null;
      // for(Iterator it=cfg.getSuccNodes(instBB);it.hasNext();) {
      // BasicBlock bb = (BasicBlock)it.next();
      // String value = String.valueOf(bb.getNumber());
      // result.appendChild(XMLDOMUtils.createTaggedElement(doc,TARGET_TAG,value));
      // }
    }

    public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
      result = doc.createElement(ARRAY_LOAD_INST);
      String value = String.valueOf(instruction.getArrayRef());
      result.appendChild(XMLDOMUtils.createTaggedElement(doc, OPERAND_TAG, value));
    }

    public void visitArrayStore(SSAArrayStoreInstruction instruction) {
      result = doc.createElement(ARRAY_STORE_INST);
      String value = String.valueOf(instruction.getArrayRef());
      result.appendChild(XMLDOMUtils.createTaggedElement(doc, OPERAND_TAG, value));
    }

    public void visitBinaryOp(SSABinaryOpInstruction instruction) {
      result = doc.createElement(BINARY_OP_INST);
      Element rhs1Element = createOperandElement(doc, RHS1_TAG, instruction.getUse(0));
      Element rhs2Element = createOperandElement(doc, RHS2_TAG, instruction.getUse(1));
      Element lhsElement = createOperandElement(doc, LHS_TAG, instruction.getDef());
      result.appendChild(lhsElement);
      result.appendChild(rhs1Element);
      result.appendChild(XMLDOMUtils.createTaggedElement(doc, OPCODE_TAG, instruction.getOperator().toString()));
      result.appendChild(rhs2Element);
    }

    public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
      result = doc.createElement(UNARY_OP_INST);
      Element rhs1Element = createOperandElement(doc, RHS1_TAG, instruction.getUse(0));
      Element lhsElement = createOperandElement(doc, LHS_TAG, instruction.getDef());
      String opcode = String.valueOf(instruction.getOpcode());
      result.appendChild(lhsElement);
      result.appendChild(rhs1Element);
      result.appendChild(XMLDOMUtils.createTaggedElement(doc, OPCODE_TAG, opcode));
    }

    public void visitConversion(SSAConversionInstruction instruction) {
      result = doc.createElement(CONVERSION_INST);
      String rhs1 = String.valueOf(instruction.getUse(0));
      String lhs = String.valueOf(instruction.getDef());
      result.appendChild(XMLDOMUtils.createTaggedElement(doc, LHS_TAG, lhs));
      result.appendChild(XMLDOMUtils.createTaggedElement(doc, RHS1_TAG, rhs1));
    }

    public void visitComparison(SSAComparisonInstruction instruction) {
      result = doc.createElement(COMPARISON_INST);
      Element rhs1Element = createOperandElement(doc, RHS1_TAG, instruction.getUse(0));
      Element rhs2Element = createOperandElement(doc, RHS2_TAG, instruction.getUse(1));
      Element lhsElement = createOperandElement(doc, LHS_TAG, instruction.getDef());
      result.appendChild(lhsElement);
      result.appendChild(rhs1Element);
      result.appendChild(rhs2Element);
    }

    public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
      result = doc.createElement(CONDITIONAL_BRANCH_INST);
      Element rhs1Element = createOperandElement(doc, RHS1_TAG, instruction.getUse(0));
      Element rhs2Element = createOperandElement(doc, RHS2_TAG, instruction.getUse(1));
      result.appendChild(rhs1Element);
      result.appendChild(rhs2Element);

      try {
        result.setAttribute(LINE_ATTR, String.valueOf(this.typeInference.getIR().getMethod().getLineNumber(this.bcIndex)));
      } catch (Exception e) {
        e.printStackTrace();
        Assertions.UNREACHABLE();
      }
    }

    public void visitSwitch(SSASwitchInstruction instruction) {
      result = doc.createElement(SWITCH_INST);
      Element rhs1Element = createOperandElement(doc, RHS1_TAG, instruction.getUse(0));
      result.appendChild(rhs1Element);
    }

    public void visitReturn(SSAReturnInstruction instruction) {
      result = doc.createElement(RETURN_INST);
      Element rhs1Element = createOperandElement(doc, RHS1_TAG, instruction.getUse(0));
      result.appendChild(rhs1Element);
    }

    public void visitGet(SSAGetInstruction instruction) {
      result = doc.createElement(GET_INST);
      Element lhsElement = createOperandElement(doc, LHS_TAG, instruction.getDef());
      result.appendChild(lhsElement);
      if (instruction.getNumberOfUses() > 0) {
        Element rhs1Element = createOperandElement(doc, RHS1_TAG, instruction.getUse(0));
        result.appendChild(rhs1Element);
      }
    }

    public void visitPut(SSAPutInstruction instruction) {
      result = doc.createElement(PUT_INST);
      Element rhs1Element = createOperandElement(doc, RHS1_TAG, instruction.getUse(0));
      result.appendChild(rhs1Element);
      if (!instruction.isStatic()) {
        Element rhs2Element = createOperandElement(doc, RHS2_TAG, instruction.getUse(1));
        result.appendChild(rhs2Element);
      }
    }

    public void visitInvoke(SSAInvokeInstruction instruction) {
      result = doc.createElement(INVOKE_INST);
      CallSiteReference site = instruction.getCallSite();
      String opcode = "";
      switch ((IInvokeInstruction.Dispatch) site.getInvocationCode()) {
      case STATIC:
        opcode = "static";
        break;
      case INTERFACE:
        opcode = "interface";
        break;
      case SPECIAL:
        opcode = "special";
        break;
      case VIRTUAL:
        opcode = "virtual";
        break;
      default:
        Assertions.UNREACHABLE();
      }
      result.appendChild(XMLDOMUtils.createTaggedElement(doc, OPCODE_TAG, opcode));
      result.setAttribute(LINE_ATTR, String.valueOf(this.typeInference.getIR().getMethod()
          .getLineNumber(site.getProgramCounter())));

      if (instruction.getNumberOfReturnValues() > 0) {
        int lhs = instruction.getDef();
        if (lhs != -1) {
          Element lhsElement = createOperandElement(doc, LHS_TAG, lhs);
          final DefUse du = new DefUse(this.typeInference.getIR());
          lhsElement.setAttribute(USED_ATTR, String.valueOf(du.getUses(instruction.getDef()).hasNext()));
          result.appendChild(lhsElement);
        }
      }

      int nParam = instruction.getNumberOfUses();
      for (int i = 0; i < nParam; i++) {
        int param = instruction.getUse(i);
        // Element paramElement = XMLDOMUtils.createTaggedElement(doc,
        // PARAM_TAG, String.valueOf(param));
        Element paramElement = createOperandElement(doc, PARAM_TAG, param);
        paramElement.setAttribute(ID_TAG, String.valueOf(i));
        result.appendChild(paramElement);
      }

      MethodReference declaredTarget = instruction.getDeclaredTarget();
      if (declaredTarget != null) {
        TypeReference retType = declaredTarget.getReturnType();
        if (retType != null) {
          Element returnTypeElement = XMLDOMUtils.createTaggedElement(doc, RETURN_TYPE, retType.getName().toString());
          result.appendChild(returnTypeElement);
        }
        result.appendChild(XMLDOMUtils.createTaggedElement(doc, TARGET_TAG, declaredTarget.getSignature()));
      }
    }

    public void visitNew(SSANewInstruction instruction) {
      result = doc.createElement(NEW_INST);
      Element lhsElement = createOperandElement(doc, LHS_TAG, instruction.getDef());
      result.appendChild(lhsElement);
      TypeReference declaredType = instruction.getNewSite().getDeclaredType();
      result.appendChild(XMLDOMUtils.createTaggedElement(doc, TYPE_TAG, declaredType.toString()));
    }

    public void visitArrayLength(SSAArrayLengthInstruction instruction) {
      result = doc.createElement(ARRAY_LEN_INST);
      String lhs = String.valueOf(instruction.getDef());
      result.appendChild(XMLDOMUtils.createTaggedElement(doc, LHS_TAG, lhs));
    }

    public void visitThrow(SSAThrowInstruction instruction) {
      result = doc.createElement(THROW_INST);
      Element rhs1Element = createOperandElement(doc, RHS1_TAG, instruction.getException());
      result.appendChild(rhs1Element);
    }

    public void visitMonitor(SSAMonitorInstruction instruction) {
      if (instruction.isMonitorEnter()) {
        result = doc.createElement(MONITOR_ENTER_INST);
      } else {
        result = doc.createElement(MONITOR_EXIT_INST);
      }
      Element rhs1Element = createOperandElement(doc, RHS1_TAG, instruction.getRef());
      result.appendChild(rhs1Element);
    }

    public void visitCheckCast(SSACheckCastInstruction instruction) {
      result = doc.createElement(CHECK_CAST_INST);
      Element lhsElement = createOperandElement(doc, LHS_TAG, instruction.getDef());
      Element rhs1Element = createOperandElement(doc, RHS1_TAG, instruction.getUse(0));
      result.appendChild(lhsElement);
      result.appendChild(rhs1Element);
      TypeReference resultType = instruction.getDeclaredResultType();
      result.appendChild(XMLDOMUtils.createTaggedElement(doc, TYPE_TAG, resultType.toString()));
    }

    public void visitInstanceof(SSAInstanceofInstruction instruction) {
      result = doc.createElement(INSTANCEOF_INST);
      Element lhsElement = createOperandElement(doc, LHS_TAG, instruction.getDef());
      Element rhs1Element = createOperandElement(doc, RHS1_TAG, instruction.getUse(0));
      result.appendChild(lhsElement);
      result.appendChild(rhs1Element);
      TypeReference checkedType = instruction.getCheckedType();
      result.appendChild(XMLDOMUtils.createTaggedElement(doc, TYPE_TAG, checkedType.toString()));
    }

    public void visitPi(SSAPhiInstruction instruction) {
      result = doc.createElement(PI_INST);
    }

    public void visitPhi(SSAPhiInstruction instruction) {
      result = doc.createElement(PHI_INST);
    }

    public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
      result = doc.createElement(GET_CAUGHT_EXCEPTION_INST);
    }

//    public void visitLoadClass(SSALoadClassInstruction instruction) {
//      result = doc.createElement(LOAD_CLASS_INST);
//      result.appendChild(XMLDOMUtils.createTaggedElement(doc, TYPE_TAG, instruction.getLoadedClass().getName().toString()));
//    }

    public void setBcIndex(final int index) {
      this.bcIndex = index;
    }

  }

  public Collection<? extends Message> getResult() {
    Assertions.UNREACHABLE();
    return null;
  }

}