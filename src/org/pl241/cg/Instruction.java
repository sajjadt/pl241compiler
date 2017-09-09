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
        // Arithmetic with immediate values
        ADDI,
        SUBI,
        MULI,
        DIVI,
        CMPI,
        // Load/Store
        LOAD,
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
        this.sourceOperand2 = op2;
        this.destinationOperand = dst;
    }

    public String toString() {
        String ret = "";

        if (destinationOperand != null)
            ret += destinationOperand.toString();

        ret += "=";

        if (sourceOperand1 != null)
            ret += " " + sourceOperand1.toString();

        ret += this.type.toString() + " ";

        if (sourceOperand2 != null)
            ret += " " + sourceOperand2.toString();
        return ret;
    }

    // Lowers one IR instruction into DLX equivalent
    public static List<Instruction> lowerIRNode(AbstractNode node, RegisterAllocator allocator, Integer currentBlockIndex, Map<Integer, Integer> blockMap, HashMap<String, Integer> localVarMap, boolean isMain) {

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
                src1 = allocator.getAllocationAt(node.getOperandAtIndex(0).getOutputOperand(), node.sourceIndex);
                operand1 = new Operand(Operand.Type.REGISTER, src1.address);
            } else
                operand1 = new Operand(Operand.Type.IMMEDIATE, ((ImmediateNode) node.getOperandAtIndex(0)).getValue());
        }
        if (node2 != null) {
            if (!(node2 instanceof ImmediateNode)) {
                src2 = allocator.getAllocationAt(node.getOperandAtIndex(1).getOutputOperand(), node.sourceIndex);
                operand2 = new Operand(Operand.Type.REGISTER, src2.address);
            } else
                operand2 = new Operand(Operand.Type.IMMEDIATE, ((ImmediateNode) node.getOperandAtIndex(1)).getValue());
        }

        if (node instanceof ArithmeticNode) {
            Allocation dst = allocator.getAllocationAt(node.getOutputOperand(), node.sourceIndex);
            assert dst.type == Allocation.Type.REGISTER;

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
            Allocation dst = allocator.getAllocationAt(node.getOutputOperand(), node.sourceIndex);
            assert dst.type == Allocation.Type.REGISTER;

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
                src1 = allocator.getAllocationAt(node.getOperandAtIndex(0).getOutputOperand(), node.sourceIndex);
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
        } else if (node instanceof IONode) {
            if (((IONode) node).type == IONode.IOType.READ) {
                Allocation dst = allocator.getAllocationAt(node.getOutputOperand(), node.sourceIndex);
                assert dst.type == Allocation.Type.REGISTER;
                instructions.add(new Instruction(Type.RDD, null, null, new Operand(Operand.Type.REGISTER, dst.address)));
            }
            else if (((IONode) node).type == IONode.IOType.WRITE) {
                if (operand1.type == Operand.Type.REGISTER)
                    instructions.add(new Instruction(Type.WRD, operand1, null, null));
                else {
                    instructions.add(new Instruction(Type.ADD, operand1,
                            new Operand(Operand.Type.REGISTER, ZERO),
                            new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER)));
                    instructions.add(new Instruction(Type.WRD,
                            new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER),
                            null,
                            null));
                }
            }
            else if (((IONode) node).type == IONode.IOType.WRITELINE)
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
                    Allocation retVal = allocator.getAllocationAt(node.getOperandAtIndex(0).getOutputOperand(), node.sourceIndex);
                    assert retVal.type == Allocation.Type.REGISTER;
                    instructions.add(new Instruction(Type.MOV,
                            new Operand(Operand.Type.REGISTER, retVal.address),
                            null,
                            new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER)));
                }
                instructions.add(new BranchInstruction(Type.RET, new Operand(Operand.Type.REGISTER, RA), (Integer) null));
            }
        }
        else if (node instanceof MoveNode) {
            instructions.add(new Instruction(Type.MOV,
                    new Operand(Operand.Type.REGISTER, ((MoveNode) node).from.address),
                    null,
                    new Operand(Operand.Type.REGISTER, ((MoveNode) node).to.address)));
        } else if (node instanceof FunctionCallNode) {
            instructions.addAll(generateCall((FunctionCallNode)node, allocator));
        } else if (node instanceof MemoryLoadNode) {
            Allocation base = allocator.getAllocationAt(((MemoryLoadNode) node).offsetCalculatioNode.getOutputOperand(), ((MemoryLoadNode) node).offsetCalculatioNode.sourceIndex);
            Allocation dst = allocator.getAllocationAt(node.getOutputOperand(), node.sourceIndex);
            instructions.add(new Instruction(Type.LOAD,
                    new Operand(Operand.Type.REGISTER, base.address),
                    new Operand(Operand.Type.REGISTER, 0),
                    new Operand(Operand.Type.REGISTER, dst.address)));
        } else if (node instanceof MemoryStoreNode) {
            Allocation base = allocator.getAllocationAt(((MemoryStoreNode) node).offsetCalculatioNode.getOutputOperand(), ((MemoryStoreNode) node).offsetCalculatioNode.sourceIndex);

            Allocation src = null;
            if (node.getOperandAtIndex(0) instanceof ImmediateNode) {
                // Move it to temp register
                instructions.add(new Instruction(Type.ADDI,
                        new Operand(Operand.Type.REGISTER, (Integer)0),
                        new Operand(Operand.Type.IMMEDIATE, ((ImmediateNode)node.getOperandAtIndex(0)).getValue()),
                        new Operand(Operand.Type.REGISTER, SCRATCH_REGISTER)));
                src = new Allocation(Allocation.Type.REGISTER, SCRATCH_REGISTER);
            } else {
                src = allocator.getAllocationAt(node.getOperandAtIndex(0).getOutputOperand(), node.sourceIndex);
            }
            assert src.type == Allocation.Type.REGISTER;

            instructions.add(new Instruction(Type.STORE,
                    new Operand(Operand.Type.REGISTER, src.address),
                    new Operand(Operand.Type.REGISTER, 0),
                    new Operand(Operand.Type.REGISTER, base.address)));
        }
        else if (node instanceof AddressCalcNode) {
            // Add variable address on stack to operand
            assert localVarMap.containsKey(((AddressCalcNode) node).variableName);

            int displacement = localVarMap.get(((AddressCalcNode) node).variableName);
            Allocation dst = allocator.getAllocationAt(node.getOutputOperand(), node.sourceIndex);
            assert dst.type == Allocation.Type.REGISTER;

            if (node.getOperandAtIndex(0) instanceof ImmediateNode) {
                instructions.add(new Instruction(Type.ADDI,
                        new Operand(Operand.Type.REGISTER, FRAMEP),
                        new Operand(Operand.Type.IMMEDIATE, displacement - 4* ((ImmediateNode)node.getOperandAtIndex(0)).getValue()),
                        new Operand(Operand.Type.REGISTER, dst.address)));
            } else {
                throw new Error("ADDA with reg operand is not implemented yet");
            }


        }
        else {
            throw new Error("Unsupported instruction type " + node);
        }

        return instructions;
    }

    private static ArrayList<Instruction> generateCall(FunctionCallNode callNode, RegisterAllocator allocator) {
        ArrayList<Instruction> instructions = new ArrayList<>();

        // Push Regs
        for (int i = 1; i < DLXCodeGenerator.NUM_REGISTERS; i++) {
            if (i == SCRATCH_REGISTER)
                continue;
            instructions.add(new Instruction(Type.PSH, new Operand(Operand.Type.REGISTER, i), null, null));
        }
        //Pass parameters
        for (AbstractNode node: callNode.getInputOperands()) {
            assert node.hasOutputRegister();
            Allocation srcAllocation = allocator.getAllocationAt(node.getOutputOperand(), callNode.sourceIndex);
            assert srcAllocation.type == Allocation.Type.REGISTER;
            instructions.add(new Instruction(Type.PSH, new Operand(Operand.Type.REGISTER, srcAllocation.address), null, null));
        }

        // Call
        instructions.add(new CallInstruction(callNode.callTarget));

        //Discard parameters
        instructions.add(new Instruction(Type.ADDI, new Operand(Operand.Type.REGISTER, RA), new Operand(Operand.Type.IMMEDIATE, 4*callNode.getInputOperands().size()), new Operand(Operand.Type.REGISTER, RA)));

        //Restore Registers
        for (int i = DLXCodeGenerator.NUM_REGISTERS -1; i > 0; i--) {
            if (i == SCRATCH_REGISTER)
                continue;
            instructions.add(new Instruction(Type.POP, new Operand(Operand.Type.REGISTER, i), null, null));
        }

        // Move return value
        if (callNode.hasOutputRegister()){
            Allocation retVal = allocator.getAllocationAt(callNode.getOutputOperand(), callNode.sourceIndex);
            assert retVal.type == Allocation.Type.REGISTER;

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

    public Integer assemble () {
        Integer ins = 0;
        return ins;
    }



}

