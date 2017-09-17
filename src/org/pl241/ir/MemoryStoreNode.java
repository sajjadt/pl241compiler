package org.pl241.ir;

public class MemoryStoreNode extends AbstractNode {
    public MemoryStoreNode(AbstractNode addressNOde, AbstractNode valueNode) {
        super(addressNOde, valueNode);
    }
    public String toString() {
        return sourceIndex + ": " +  "Mem[" + this.getOperandAtIndex(0).getOutputVirtualReg() + "] = " + this.getOperandAtIndex(1).getOutputVirtualReg();
    }
    public AbstractNode getAddressCalcNode() {
        return super.getOperandAtIndex(0);
    }
    public AbstractNode getValueNode() {
        return super.getOperandAtIndex(1);
    }
    public void setValueNode(AbstractNode node) {
        this.getInputOperands().set(1, node);
    }

    @Override
    public String getOutputVirtualReg() {
        return null;
    }
    // Node interface implementation
    public boolean isExecutable() {
        return true;
    }
    public boolean hasOutputVirtualRegister() {
        return false;
    }
    public boolean visualize() {
        return true;
    }

    public String printAllocation() {
        return "[" + getAddressCalcNode().allocation +"]" + " = " + getValueNode().allocation;
    }


}
