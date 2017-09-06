package org.pl241.cg;

public class BranchInstruction extends Instruction {

    public BranchInstruction(Type type, Operand op1, Integer offset) {
        super(type, op1, null, null);
        this.offset = offset;
    }

    public Integer offset;

    @Override
    public String toString() {
        String ret = "";


        ret += this.type + " ";

        if (sourceOperand1 != null)
            ret += " " + sourceOperand1.toString();

        ret += ("," + offset.toString());

        return ret;
    }

}
