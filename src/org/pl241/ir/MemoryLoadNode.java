package org.pl241.ir;


public class MemoryLoadNode extends AbstractNode {
    public AbstractNode offsetCalculatioNode;
    public MemoryLoadNode(AbstractNode offsetCalculatioNode) {
        super();
        this.offsetCalculatioNode = offsetCalculatioNode;
    }

    public String toString() {
        String ret  = super.nodeId +  "Mem[" + offsetCalculatioNode.getOutputOperand() + "]";
        if (operands.size() > 0)
            ret += ( "=" + getOperandAtIndex(0).getOutputOperand());
        return ret;
    }

    @Override
    public String getOutputOperand() {
        return nodeId;
    }

    @Override
    public boolean isExecutable() {
        return true;
    }
}
