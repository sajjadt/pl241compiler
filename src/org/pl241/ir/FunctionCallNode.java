package org.pl241.ir;

import java.util.stream.Collectors;

public class FunctionCallNode extends AbstractNode {

    public FunctionCallNode(String callTarget) {
        super();
        this.callTarget = callTarget;
    }

    @Override
    public String toString() {
        String ret = super.toString();
        ret += (callTarget + "(");

        String joinedOperands = this.getInputOperands().stream()
                .map(AbstractNode::getOutputOperand)
                .collect(Collectors.joining(", ")); // "John, Anna, Paul"

        ret += joinedOperands;
        ret += ")";
        return ret;
    }

    @Override
    public boolean hasOutputRegister() {
        return returnsStuff;
    }
    @Override
    public boolean isExecutable() {
        return true;
    }


    @Override
    public String getOutputOperand() {
        if (returnsStuff)
            return nodeId;
        else
            return null;
    }

    // Adds operands at the beginning since they are accessed on Stack
    @Override
    public void addOperand(AbstractNode node) {
        operands.add(0, node);
    }

    public String callTarget;
    public boolean returnsStuff;
}
