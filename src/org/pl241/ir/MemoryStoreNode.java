package org.pl241.ir;

import java.util.Objects;

public class MemoryStoreNode extends AbstractNode {
    public AbstractNode offsetCalculatioNode;
    public MemoryStoreNode(AbstractNode offsetCalculatioNode) {
        super();
        this.offsetCalculatioNode = offsetCalculatioNode;
    }
    public String toString() {
        String ret  = super.nodeId +  "Mem[" + offsetCalculatioNode.getOutputOperand() + "]";
        if (operands.size() > 0)
            ret += ( "=" + getOperandAtIndex(0).getOutputOperand());
        return ret;
    }

    void setSrcOperand(AbstractNode srcOperand) {
        super.addOperand(srcOperand);
    }


    @Override
    public String getOutputOperand() {
        return null;
    }


    @Override
    public boolean isExecutable() {
        return true;
    }
}
