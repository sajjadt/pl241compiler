package org.pl241.cg;

import org.pl241.ir.BasicBlock;

public class BranchInstruction extends Instruction {

    public BranchInstruction(Type type, Operand op1, Integer offset) {
        super(type, op1, null, null);
        this.offset = offset;
        this.resolved = true;
    }

    public BranchInstruction(Type type, Operand op1, BasicBlock destBlock) {
        super(type, op1, null, null);
        this.offset = null;
        this.resolved = false;
        this.destBlockID = destBlock.getID();
    }

    public Integer offset;
    public boolean resolved;
    public Integer destBlockID;

    @Override
    public String toString() {
        String ret = "";
        ret += this.type + " ";

        if (sourceOperand1 != null)
            ret += " " + sourceOperand1.toString();
        if (offset != null)
            ret += ("," + offset.toString());

        return ret;
    }

}
