package org.pl241.cg;

public class CallInstruction extends Instruction {

    public CallInstruction(String destFunc) {
        super(Type.JSR, null, null, null);
        this.jumpAddress = null;
        this.resolved = false;
        this.destFunc = destFunc;
    }

    public Integer jumpAddress;
    public boolean resolved;
    public String destFunc;

    @Override
    public String toString() {
        String ret = "";
        ret += this.type + " ";

        if (sourceOperand1 != null)
            ret += " " + sourceOperand1.toString();
        if (jumpAddress != null)
            ret += ("," + jumpAddress.toString());

        return ret;
    }

}
