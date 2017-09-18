package org.pl241.ir;


public class MemoryLoadNode extends AbstractNode {
    public MemoryLoadNode(NodeContainer addressNode) {
        super();
        this.addOperand(addressNode);
    }

    public String toString() {
        return sourceIndex + ": " + getOutputVirtualReg() +  " = Mem[" + this.getOperandAtIndex(0).getOutputVirtualReg() + "]";
    }

    public NodeContainer getAddressCalcNode() {
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

    public String printAllocation() {
        return getAllocation() +  " = [" + getAddressCalcNode().getAllocation() +"]";
    }
}
