package org.pl241.ir;

import org.pl241.ra.Allocation;

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
                .map(NodeContainer::getOutputVirtualReg)
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
    public void addOperand(NodeContainer node) {
        operands.add(0, node);
    }


    @Override
    public String printAllocation() {
        String ret = "";

        if (this.getAllocation() != null)
            ret += this.getAllocation().toString();

        ret += this.callTarget + "(";

        if (this.operands.size() > 0) {
            Allocation al = this.operands.get(0).getAllocation();
            if (al != null)
                ret +=  ", " + al.toString();
        }
        ret += ")";
        return ret;
    }

    @Override
    public boolean isControlFlow() {
        return true;
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
}
