package org.pl241.ir;

import java.util.stream.Collectors;

public class FunctionCallNode extends AbstractNode implements NodeInterface{

    public FunctionCallNode(String callTarget) {
        super();
        this.callTarget = callTarget;
        hasReturnValue = true;
    }

    @Override
    public String toString() {
        String ret = super.toString();
        ret += (callTarget + "(");

        String joinedOperands = this.getInputOperands().stream()
                .map(AbstractNode::getOutputVirtualReg)
                .collect(Collectors.joining(", ")); // "John, Anna, Paul"

        ret += joinedOperands;
        ret += ")";
        return ret;
    }

    @Override
    public String getOutputVirtualReg() {
        if (hasReturnValue)
            return nodeId;
        else
            return null;
    }

    // Adds operands at the beginning since they are accessed on Stack
    @Override
    public void addOperand(AbstractNode node) {
        operands.add(0, node);
    }


    public boolean hasOutputVirtualRegister() {
        return hasReturnValue;
    }
    public boolean isExecutable() {
        return true;
    }
    public boolean visualize() {
        return true;
    }

    public String callTarget;
    public boolean hasReturnValue;

    @Override
    public boolean isControlFlow() {
        return true;
    }
}
