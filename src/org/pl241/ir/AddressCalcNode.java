package org.pl241.ir;

public class AddressCalcNode extends AbstractNode implements NodeInterface {

    public AddressCalcNode(String variableName, AbstractNode offset) {
        super();
        this.variableName = variableName;
        this.addOperand(offset);
    }

    // Override methods
    @Override
    public String toString() {
        String ret = super.toString() +  "ADDA ";
        if (this.operands.size() > 0)
            ret += variableName + "," + this.operands.get(0).getOutputVirtualReg();
        return ret;
    }
    @Override
    public String getOutputVirtualReg() {
        return nodeId;
    }

    // Node interface implementations
    public boolean hasOutputVirtualRegister() {
        return true;
    }
    public boolean isExecutable() {
        return true;
    }
    public boolean visualize() {
        return true;
    }

    public String variableName;

}
