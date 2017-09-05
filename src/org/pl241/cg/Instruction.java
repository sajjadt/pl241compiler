package org.pl241.cg;

import org.pl241.ir.*;
import org.pl241.ra.Allocation;
import org.pl241.ra.RegisterAllocator;

import java.util.ArrayList;
import java.util.List;


public class Instruction {

    enum OperandType {
        REGISTER,
        IMMEDIATE;

        @Override
        public String toString() {
            switch(this) {
                case REGISTER: return "R";
                case IMMEDIATE: return "I";
                default: throw new IllegalArgumentException();
            }
        }
    }

    public Instruction (String instruction, Operand op1, Operand op2, Operand dst) {
        this.instruction = instruction;
        this.sourceOperand1 = op1;
        this.sourceOperand2 = op2;
        this.destinationOperand = dst;
    }

    @Override
    public String toString() {
        String ret = "";

        if (destinationOperand != null)
            ret += destinationOperand.toString();

        ret += " " + this.instruction;

        if (sourceOperand1 != null)
            ret += " " + sourceOperand1.toString();
        if (sourceOperand2 != null)
            ret += " " + sourceOperand2.toString();
        return ret;
    }

    // Converts one IR instruction into DLX equivalent
    public static List<Instruction> fromIRNode(AbstractNode node, RegisterAllocator allocator) {

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
                operand1 = new Operand(OperandType.REGISTER, src1.address);
            } else
                operand1 = new Operand(OperandType.IMMEDIATE, ((ImmediateNode) node.getOperandAtIndex(0)).getValue());
        }
        if (node2 != null) {
            if (!(node2 instanceof ImmediateNode)) {
                src2 = allocator.getAllocationAt(node.getOperandAtIndex(1).getOutputOperand(), node.sourceIndex);
                operand2 = new Operand(OperandType.REGISTER, src2.address);
            } else
                operand2 = new Operand(OperandType.IMMEDIATE, ((ImmediateNode) node.getOperandAtIndex(1)).getValue());
        }

        if (node instanceof ArithmeticNode) {
            Allocation dst = allocator.getAllocationAt(node.getOutputOperand(), node.sourceIndex);
            assert dst.type == Allocation.Type.REGISTER;

            instructions.add(new Instruction(((ArithmeticNode) node).operator.toString(),
                    operand1, operand2,
                    new Operand(OperandType.REGISTER, dst.address)));
        } else if (node instanceof VarSetNode) {
            Allocation dst = allocator.getAllocationAt(node.getOutputOperand(), node.sourceIndex);
            assert dst.type == Allocation.Type.REGISTER;

            instructions.add(new Instruction("mov", operand1, null,
                    new Operand(OperandType.REGISTER, dst.address)));
        } else if (node instanceof BranchNode) {
            if (((BranchNode)node).isConditioned()) {
                src1 = allocator.getAllocationAt(node.getOperandAtIndex(0).getOutputOperand(), node.sourceIndex);
                operand1 = new Operand(OperandType.REGISTER, src1.address);
            }
            instructions.add(new Instruction(((BranchNode)node).type.toString(), operand1, null, null));
        } else if (node instanceof IONode) {
            if (((IONode) node).type == IONode.IOType.READ) {
                Allocation dst = allocator.getAllocationAt(node.getOutputOperand(), node.sourceIndex);
                assert dst.type == Allocation.Type.REGISTER;
                instructions.add(new Instruction("IOREAD", null, null, new Operand(OperandType.REGISTER, dst.address)));
            }
            else if (((IONode) node).type == IONode.IOType.WRITE)
                instructions.add(new Instruction("IOWRITE", operand1, null, null));
            else if (((IONode) node).type == IONode.IOType.WRITELINE)
                instructions.add(new Instruction("IOWRITELINE", null, null, null));

        } else if (node instanceof ReturnNode) {
            if (node.hasOutputRegister())
                instructions.add(new Instruction("Return", operand1, null, null));
            else
                instructions.add(new Instruction("Return", null, null, null));
        }

        return instructions;
    }

    private Integer generateRegisterArithmetic() {
        Integer ins = 0;
        /*
        assert destinationOperand.type == OperandType.REGISTER
                && sourceOperand1.type == OperandType.REGISTER
                && sourceOperand2.type == OperandType.REGISTER;

        ArrayList<Integer> instructions = new ArrayList<>();
        switch (instruction) {
            case ADD:
                instructions.add(DLX.assemble(DLX.ADD, destAllocation.address, src1Allocation.address, src2Allocation.address));
                break;
            case SUB:
                instructions.add(DLX.assemble(DLX.SUB, destAllocation.address, src1Allocation.address, src2Allocation.address));
                break;
            case MUL:
                instructions.add(DLX.assemble(DLX.MUL, destAllocation.address, src1Allocation.address, src2Allocation.address));
                break;
            case CMP:
                instructions.add(DLX.assemble(DLX.ADD, destAllocation.address, src1Allocation.address, src2Allocation.address));
                break;
            case DIV:
                instructions.add(DLX.assemble(DLX.DIV, destAllocation.address, src1Allocation.address, src2Allocation.address));
                break;
            case NEG:
                instructions.add(DLX.assemble(DLX.MULI, destAllocation.address, src1Allocation.address,-1));
                break;
            default:
                throw new Error("Unsupported op " + operation);
        }*/
        return ins;
    }

    public Operand destinationOperand;
    public Operand sourceOperand1;
    public Operand sourceOperand2;
    public String instruction;

    public Integer assemble () {
        Integer ins = 0;
        return ins;
    }



}

