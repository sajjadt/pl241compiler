package org.pl241.cg;

import org.pl241.ir.*;
import org.pl241.ra.Allocation;
import org.pl241.ra.RegisterAllocator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.pl241.cg.DLXCodeGenerator.RA;
import static org.pl241.cg.DLXCodeGenerator.ZERO;
import static org.pl241.cg.DLXCodeGenerator.SCRATCH_REGISTER;
import static org.pl241.cg.DLXCodeGenerator.FRAMEP;
import static org.pl241.cg.DLXCodeGenerator.SP;

public class Instruction {

    enum Type {
        // Arithmetic on registers
        ADD,
        SUB,
        MUL,
        DIV,
        CMP,
        NEG,
        XOR,
        // Arithmetic with immediate values
        ADDI,
        SUBI,
        MULI,
        DIVI,
        CMPI,
        // Load/Store
        LOAD,
        LOADI,
        POP,
        STORE,
        PSH,
        // Branch
        BEQ,
        BNE,
        BLT,
        BGE,
        BLE,
        BGT,
        BRA,
        //
        JSR,
        RET,
        // I/O
        RDD,
        WRD,
        WRL,
        // Extra
        MOV;

        @Override
        public String toString() {
            return name();
        }

        static Instruction.Type fromArithmeticType(ArithmeticNode.Type type, boolean withImmediateOperand) {
            switch (type) {
                case NEG:
                    if (withImmediateOperand)
                        throw new IllegalArgumentException();
                    else
                        return Type.NEG;
                case ADD:
                case ADDA:
                    if (withImmediateOperand)
                        return Type.ADDI;
                    else
                        return Type.ADD;
                case SUB:
                    if (withImmediateOperand)
                        return Type.SUBI;
                    else
                        return Type.SUB;
                case MUL:
                    if (withImmediateOperand)
                        return Type.MULI;
                    else
                        return Type.MUL;
                case DIV:
                    if (withImmediateOperand)
                        return Type.DIVI;
                    else
                        return Type.DIV;
                case CMP:
                    if (withImmediateOperand)
                        return Type.CMPI;
                    else
                        return Type.CMP;
                default:
                    throw new IllegalArgumentException();
            }
        }

        static Instruction.Type fromBranchType(BranchNode.Type type) {
            switch (type) {
                case BRA:
                    return Type.BRA;
                case BNE:
                    return Type.BNE;
                case BEQ:
                    return Type.BEQ;
                case BLE:
                    return Type.BLE;
                case BLT:
                    return Type.BLT;
                case BGE:
                    return Type.BGE;
                case BGT:
                    return Type.BGT;
                default:
                    throw new IllegalArgumentException();
            }
        }


        static  boolean isSymmetric(Instruction.Type type) {
            switch (type) {
                case NEG:
                case SUB:
                case SUBI:
                case DIV:
                case DIVI:
                case CMP:
                case CMPI:
                    return false;
                case MUL:
                case MULI:
                case ADD:
                case ADDI:
                    return true;
                default:
                    throw new IllegalArgumentException();
            }
        }

    }

    public Instruction (Instruction.Type type, Operand op1, Operand op2, Operand dst) {
        this.type = type;
        this.sourceOperand1 = op1;

        if (sourceOperand1 != null && sourceOperand1.value == null) {
            System.out.println("wooo");
        }

        this.sourceOperand2 = op2;
        this.destinationOperand = dst;
    }

    public String toString() {
        String ret = "";

        if (destinationOperand != null)
            ret += destinationOperand.toString() + "=";


        switch (this.type) {
            case LOAD:
            case LOADI:
                return destinationOperand.toString() + " = Mem[" + sourceOperand1.toString() + "+" + sourceOperand2.toString() + "]" ;
            case STORE:
                return "Mem[" + destinationOperand.toString() + " + " + sourceOperand2.toString() + "] = " + sourceOperand1.toString();
            case MOV:
                return destinationOperand.toString() + " = " + sourceOperand1.toString();
            case WRD:
                return "WRD " + sourceOperand1.toString();
            case WRL:
                return "WRL";
            case RET:
                return "RET";
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case CMP:
            case XOR:
            case ADDI:
            case SUBI:
            case MULI:
            case DIVI:
            case CMPI:
                return destinationOperand.toString() + " = " + sourceOperand1.toString() + " " + type.toString() + " " + sourceOperand2.toString() ;
            default :
                return "x"
;        }
    }

    // Lowers one IR instruction into DLX equivalent
    public static List<Instruction> lowerIRNode(AbstractNode node, Integer currentBlockIndex, Map<Integer, Integer> blockMap, HashMap<String, Integer> localVarMap, boolean isMain) {

        List<Instruction> instructions = new ArrayList<>();
        if (!node.isExecutable())
            return instructions;

        AbstractNode node1 = null;
        AbstractNode node2 = null;
        if (node.getInputOperands().size() > 0)
            node1 = node.getOperandAtIndex(0);
        if (node.getInputOperands().size() > 1)
            node2 = node.getOperandAtIndex(1);

        Allocation src1 = null;
        Allocation src2 = null;
        Operand operand1 = null;
        Operand operand2 = null;


        if (node1 != null) {
            if (!(node1 instanceof ImmediateNode)) {
                src1 = node.getOperandAtIndex(0).allocation;
                operand1 = new Operand(Operand.Type.REGISTER, src1.address);
            } else
                operand1 = new Operand(Operand.Type.IMMEDIATE, ((ImmediateNode) node.getOperandAtIndex(0)).getValue());
        }
        if (node2 != null) {
            if (!(node2 instanceof ImmediateNode)) {
                src2 = node.getOperandAtIndex(1).allocation;
                operand2 = new Operand(Operand.Type.REGISTER, src2.address);
            } else
                operand2 = new Operand(Operand.Type.IMMEDIATE, ((ImmediateNode) node.getOperandAtIndex(1)).getValue());
        }

        if (node instanceof ArithmeticNode) {
            Allocation dst =  node.allocation;
            assert dst.type == Allocation.Type.GENERAL_REGISTER;

            boolean firstOperandIsImmediate = ((node1 != null) && node1 instanceof ImmediateNode);
            boolean secondOperandIsImmediate = ((node2 != null) && node2 instanceof ImmediateNode);
            boolean hasImmediateOperand = firstOperandIsImmediate | secondOperandIsImmediate;

            // TODO: remove this by simple constant propagation
            if (firstOperandIsImmediate && secondOperandIsImmediate) { // Both operands are immediate values
                instructions.add(new Instruction(Type.ADDI,
                        new Operand(Operand.Type.REGISTER, ZERO),
                        new Operand(Operand.Type.IMMEDIATE, ((ImmediateNode) node1).getValue()),
                        new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER)));
                instructions.add(
                        new Instruction(
                                Instruction.Type.fromArithmeticType(((ArithmeticNode)node).operator, hasImmediateOperand),
                                new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER),
                                operand2,
                                new Operand(Operand.Type.REGISTER, dst.address)));
            }
            else if (firstOperandIsImmediate ) { // Only first operand is immediate

                Instruction.Type type = Instruction.Type.fromArithmeticType(((ArithmeticNode) node).operator, true);

                if (Instruction.Type.isSymmetric(type)) {
                    // Swap first and second param
                    instructions.add(new Instruction(type,
                            operand2, operand1,
                            new Operand(Operand.Type.REGISTER, dst.address)));
                } else {
                    instructions.add(new Instruction(Type.ADDI,
                            new Operand(Operand.Type.REGISTER, ZERO),
                            new Operand(Operand.Type.IMMEDIATE, ((ImmediateNode) node1).getValue()),
                            new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER)));
                    instructions.add(new Instruction(Instruction.Type.fromArithmeticType(((ArithmeticNode) node).operator, false),
                            new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER),
                            operand2,
                            new Operand(Operand.Type.REGISTER, dst.address)));
                }
            }
            else {
                instructions.add(new Instruction(Instruction.Type.fromArithmeticType( ((ArithmeticNode) node).operator, hasImmediateOperand),
                        operand1, operand2,
                        new Operand(Operand.Type.REGISTER, dst.address)));
            }
        } else if (node instanceof VarSetNode) {
            Allocation dst = node.allocation;
            assert dst.type == Allocation.Type.GENERAL_REGISTER;

            instructions.add(new Instruction(Type.MOV, operand1, null,
                    new Operand(Operand.Type.REGISTER, dst.address)));
        } else if (node instanceof BranchNode) {
            Integer offset1 = null;
            //Integer offset2 = null;

            if (((BranchNode) node).takenBlock != null) {
                if (!blockMap.containsKey(((BranchNode) node).takenBlock.getID())) {
                    offset1 = null;
                } else {
                    offset1 = currentBlockIndex - blockMap.get(((BranchNode) node).takenBlock.getID());
                    offset1 += 1;
                }
            }

            if (((BranchNode)node).isConditioned()) {
                src1 = node.getOperandAtIndex(0).allocation;
                operand1 = new Operand(Operand.Type.REGISTER, src1.address);
                instructions.add(new BranchInstruction(Instruction.Type.fromBranchType(((BranchNode) node).type), operand1, offset1));
            } else {
                if (offset1 == null) {
                    instructions.add(new BranchInstruction(Instruction.Type.fromBranchType(((BranchNode) node).type),
                            operand1,
                            ((BranchNode) node).fallThroughBlock));
                } else {
                    // No jump is necessary
                    if (offset1 != 1)
                        instructions.add(new BranchInstruction(Instruction.Type.fromBranchType(((BranchNode) node).type), operand1, offset1));
                }
            }
        } else if (node instanceof AtomicFunctionNode) {
            if (((AtomicFunctionNode) node).type == AtomicFunctionNode.IOType.READ) {
                Allocation dst = node.allocation;
                assert dst.type == Allocation.Type.GENERAL_REGISTER;
                instructions.add(new Instruction(Type.RDD, null, null, new Operand(Operand.Type.REGISTER, dst.address)));
            }
            else if (((AtomicFunctionNode) node).type == AtomicFunctionNode.IOType.WRITE) {
                if (operand1.type == Operand.Type.REGISTER)
                    instructions.add(new Instruction(Type.WRD, operand1, null, null));
                else {
                    instructions.add(new Instruction(Type.ADDI,
                            new Operand(Operand.Type.REGISTER, ZERO),
                            operand1,
                            new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER)));
                    instructions.add(new Instruction(Type.WRD,
                            new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER),
                            null,
                            null));
                }
            }
            else if (((AtomicFunctionNode) node).type == AtomicFunctionNode.IOType.WRITELINE)
                instructions.add(new Instruction(Type.WRL, null, null, null));

        } else if (node instanceof ReturnNode) {
            if (isMain)
                instructions.add(new BranchInstruction(Type.RET, new Operand(Operand.Type.REGISTER, 0), (Integer) null));
            else {
                // Restore SP
                instructions.add(new Instruction(Type.MOV,
                        new Operand(Operand.Type.REGISTER, FRAMEP),
                        null,
                        new Operand(Operand.Type.REGISTER, SP)));

                if (node.getInputOperands().size()>0) {
                    // Prepare return value
                    AbstractNode retNode = node.getOperandAtIndex(0);
                    if (retNode instanceof ImmediateNode) {
                        instructions.add(new Instruction(Type.ADDI,
                                new Operand(Operand.Type.REGISTER, ZERO),
                                new Operand(Operand.Type.IMMEDIATE, ((ImmediateNode) retNode).getValue()),
                                new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER)));

                    } else {
                        Allocation retVal = retNode.allocation;
                        assert retVal.type == Allocation.Type.GENERAL_REGISTER;

                        instructions.add(new Instruction(Type.MOV,
                                new Operand(Operand.Type.REGISTER, retVal.address),
                                null,
                                new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER)));

                    }

                }
                instructions.add(new BranchInstruction(Type.RET, new Operand(Operand.Type.REGISTER, RA), (Integer) null));
            }
        } else if (node instanceof MoveNode) {

            Operand src = null;
            Operand destination = null;

            if (((MoveNode) node).from.type == Allocation.Type.SCRATCH_REGISTER)
                src = new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER);
            else
                src = new Operand(Operand.Type.REGISTER, ((MoveNode) node).from.address);
            assert ((MoveNode) node).from.type != Allocation.Type.STACK: "Move from/to stack not implemented";

            if (((MoveNode) node).to.type == Allocation.Type.SCRATCH_REGISTER)
                destination = new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER);
            else
                destination = new Operand(Operand.Type.REGISTER, ((MoveNode) node).to.address);
            assert ((MoveNode) node).to.type != Allocation.Type.STACK: "Move from/to stack not implemented";

            instructions.add(new Instruction(Type.MOV, src, null, destination));

        } else if (node instanceof FunctionCallNode) {
            instructions.addAll(generateCall((FunctionCallNode)node));
        } else if (node instanceof MemoryLoadNode) {
            Allocation base = ((MemoryLoadNode) node).getAddressCalcNode().allocation;// allocator.getAllocationAt(((MemoryLoadNode) node).getAddressCalcNode().getOutputVirtualReg(), ((MemoryLoadNode) node).getAddressCalcNode().sourceIndex);
            Allocation dst =  node.allocation; // allocator.getAllocationAt(node.getOutputVirtualReg(), node.sourceIndex);
            instructions.add(new Instruction(Type.LOAD,
                    new Operand(Operand.Type.REGISTER, base.address),
                    new Operand(Operand.Type.REGISTER, 0),
                    new Operand(Operand.Type.REGISTER, dst.address)));
        } else if (node instanceof MemoryStoreNode) {
            Allocation base = ((MemoryStoreNode) node).getAddressCalcNode().allocation; //allocator.getAllocationAt(((MemoryStoreNode) node).getAddressCalcNode().getOutputVirtualReg(), ((MemoryStoreNode) node).getAddressCalcNode().sourceIndex);

            Allocation src = null;
            if (((MemoryStoreNode) node).getValueNode() instanceof ImmediateNode) {
                // Move it to temp register
                instructions.add(new Instruction(Type.ADDI,
                        new Operand(Operand.Type.REGISTER, (Integer)0),
                        new Operand(Operand.Type.IMMEDIATE, ((ImmediateNode)((MemoryStoreNode) node).getValueNode()).getValue()),
                        new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER)));
                src = new Allocation(Allocation.Type.GENERAL_REGISTER, SCRATCH_REGISTER);
            } else {
                src = ((MemoryStoreNode) node).getValueNode().allocation; //allocator.getAllocationAt(node.getOperandAtIndex(0).getOutputVirtualReg(), node.sourceIndex);
            }
            assert src.type == Allocation.Type.GENERAL_REGISTER;

            instructions.add(new Instruction(Type.STORE,
                    new Operand(Operand.Type.REGISTER, src.address),
                    new Operand(Operand.Type.REGISTER, 0),
                    new Operand(Operand.Type.REGISTER, base.address)));
        } else if (node instanceof ExchangeNode) {
            assert ((ExchangeNode) node).from.type != Allocation.Type.STACK &&
                    ((ExchangeNode) node).to.type != Allocation.Type.STACK : "Exchange of non registers not implemented";

            Operand from = Operand.fromAllocation(((ExchangeNode) node).from);
            Operand to = Operand.fromAllocation(((ExchangeNode) node).to);

            instructions.add(new Instruction(Type.XOR, from, to, from));
            instructions.add(new Instruction(Type.XOR, to, from, to));
            instructions.add(new Instruction(Type.XOR, from, to, from));
        }
        else {
            throw new Error("Unsupported instruction type " + node);
        }
        return instructions;
    }

    private static ArrayList<Instruction> generateCall(FunctionCallNode callNode) {
        ArrayList<Instruction> instructions = new ArrayList<>();

        // Push Regs
        for (int i = 1; i < DLXCodeGenerator.NUM_REGISTERS; i++) {
            if (i == SCRATCH_REGISTER)
                continue;
            instructions.add(new Instruction(Type.PSH, new Operand(Operand.Type.REGISTER, i), null, null));
        }
        //Pass parameters
        for (AbstractNode node: callNode.getInputOperands()) {
            if (node.hasOutputVirtualRegister()) {
                Allocation srcAllocation = node.allocation; // allocator.getAllocationAt(node.getOutputVirtualReg(), callNode.sourceIndex);
                assert srcAllocation.type == Allocation.Type.GENERAL_REGISTER;
                instructions.add(new Instruction(Type.PSH, new Operand(Operand.Type.REGISTER, srcAllocation.address), null, null));
            } else if (node instanceof ImmediateNode) {
                instructions.add(new Instruction(Type.ADDI, new Operand(Operand.Type.REGISTER, ZERO), new Operand(Operand.Type.IMMEDIATE, ((ImmediateNode) node).getValue()), new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER)));
                instructions.add(new Instruction(Type.PSH, new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER), null, null));
            }
            assert true : "Unexpected parameter for function call";
        }

        // Call
        instructions.add(new CallInstruction(callNode.callTarget));

        //Discard parameters
        for (AbstractNode node: callNode.getInputOperands()) {
            instructions.add(new Instruction(Type.POP, null, null, new Operand(Operand.Type.REGISTER, ZERO)));
        }
        //instructions.add(new Instruction(Type.ADDI, new Operand(Operand.Type.REGISTER, SP), new Operand(Operand.Type.IMMEDIATE, 4*callNode.getInputOperands().size()), new Operand(Operand.Type.REGISTER, SP)));

        //Restore Registers
        for (int i = DLXCodeGenerator.NUM_REGISTERS -1; i > 0; i--) {
            if (i == SCRATCH_REGISTER)
                continue;
            instructions.add(new Instruction(Type.POP, null, null, new Operand(Operand.Type.REGISTER, i)));
        }

        // Move return value
        if (callNode.hasOutputVirtualRegister()){
            Allocation retVal = callNode.allocation; //allocator.getAllocationAt(callNode.getOutputVirtualReg(), callNode.sourceIndex);
            assert retVal.type == Allocation.Type.GENERAL_REGISTER;

            instructions.add(new Instruction(Type.MOV, new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER),
                    null,
                    new Operand(Operand.Type.REGISTER, retVal.address)));
        }
        return instructions;
    }

    public Operand destinationOperand;
    public Operand sourceOperand1;
    public Operand sourceOperand2;
    public Instruction.Type type;
}

