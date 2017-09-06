package org.pl241.cg;

import org.pl241.ir.*;
import org.pl241.ra.Allocation;
import org.pl241.ra.RegisterAllocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.pl241.cg.DLXCodeGenerator.ZERO;
import static org.pl241.cg.DLXCodeGenerator.TEMP_REGISTER;


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
        LDW,
        LDX,
        POP,
        STW,
        STX,
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

        //@Override
        //public String toString() {
        //    switch(this) {
        //        default: throw new IllegalArgumentException();
        //    }
        //}

        public static Instruction.Type fromArithmeticType(ArithmeticNode.Type type, boolean withImmediateOperand) {
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

        public static Instruction.Type fromBranchType(BranchNode.Type type) {
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

        ret += this.type + " ";

        if (sourceOperand2 != null)
            ret += " " + sourceOperand2.toString();
        return ret;
    }

    // Lowers one IR instruction into DLX equivalent
    public static List<Instruction> lowerIRNode(AbstractNode node, RegisterAllocator allocator, Integer currentBlockIndex, Map<Integer, Integer> blockMap) {

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

        List<Instruction> instructions = new ArrayList<>();


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

            boolean hasImmediateOperand = ((node1 != null) && node1 instanceof ImmediateNode) ||
                    ((node2 != null) && node2 instanceof ImmediateNode);


            instructions.add(new Instruction(Instruction.Type.fromArithmeticType( ((ArithmeticNode) node).operator, hasImmediateOperand),
                    operand1, operand2,
                    new Operand(Operand.Type.REGISTER, dst.address)));
        } else if (node instanceof VarSetNode) {
            Allocation dst = allocator.getAllocationAt(node.getOutputOperand(), node.sourceIndex);
            assert dst.type == Allocation.Type.REGISTER;

            instructions.add(new Instruction(Type.MOV, operand1, null,
                    new Operand(Operand.Type.REGISTER, dst.address)));
        } else if (node instanceof BranchNode) {


            Integer offset1 = null;
            Integer offset2 = null;

            if (((BranchNode) node).takenBlock != null)
                offset1 = currentBlockIndex - blockMap.get(((BranchNode) node).takenBlock.getID());

            if (((BranchNode) node).nonTakenBlock != null)
                offset2 = currentBlockIndex - blockMap.get(((BranchNode) node).nonTakenBlock.getID());

            if (((BranchNode)node).isConditioned()) {
                src1 = allocator.getAllocationAt(node.getOperandAtIndex(0).getOutputOperand(), node.sourceIndex);
                operand1 = new Operand(Operand.Type.REGISTER, src1.address);
                instructions.add(new BranchInstruction(Instruction.Type.fromBranchType(((BranchNode) node).type), operand1, offset1));
            } else {
                // No jump is necessary
                if (offset1 != 0)
                    instructions.add(new BranchInstruction(Instruction.Type.fromBranchType(((BranchNode) node).type), operand1, offset1));
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
                            new Operand(Operand.Type.REGISTER, TEMP_REGISTER)));
                    instructions.add(new Instruction(Type.WRD,
                            new Operand(Operand.Type.REGISTER, TEMP_REGISTER),
                            null,
                            null));
                }
            }
            else if (((IONode) node).type == IONode.IOType.WRITELINE)
                instructions.add(new Instruction(Type.WRL, null, null, null));

        } else if (node instanceof ReturnNode) {
            if (node.hasOutputRegister())
                instructions.add(new Instruction(Type.RET, operand1, null, null));
            else
                instructions.add(new Instruction(Type.RET, null, null, null));
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

