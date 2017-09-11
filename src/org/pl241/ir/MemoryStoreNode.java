package org.pl241.ir;

public class MemoryStoreNode extends AbstractNode {
    public MemoryStoreNode(AbstractNode addressNOde, AbstractNode valueNode) {
        super(addressNOde, valueNode);
    }
    public String toString() {
        String ret  = this.getOperandAtIndex(1).toString() +  "to Mem[" + this.getOperandAtIndex(0).toString() + "]";
        return ret;
    }

    //void setSrcOperand(AbstractNode srcOperand) {
    //    super.addOperand(srcOperand);
    //}

    public AbstractNode getAddressCalcNode() {
        return super.getOperandAtIndex(0);
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

}
