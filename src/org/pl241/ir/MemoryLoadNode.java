package org.pl241.ir;


public class MemoryLoadNode extends AbstractNode {
    public MemoryLoadNode(AbstractNode addressNode) {
        super();
        this.addOperand(addressNode);
    }

    public String toString() {
        String ret  = super.nodeId +  "Mem[" + this.getOperandAtIndex(0).getOutputVirtualReg() + "]";
        return ret;
    }

    public AbstractNode getAddressCalcNode() {
        return this.getOperandAtIndex(0);
    }

    @Override
    public String getOutputVirtualReg() {
        return nodeId;
    }

    // Node interface implementation
    public boolean isExecutable() {
        return true;
    }
    public boolean hasOutputVirtualRegister() {
        return true;
    }
    public boolean visualize() {
        return true;
    }
}
