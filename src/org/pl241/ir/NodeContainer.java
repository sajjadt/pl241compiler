package org.pl241.ir;

import org.pl241.ra.Allocation;

import java.util.List;

public class NodeContainer {
    public AbstractNode node;
    public NodeContainer(AbstractNode node) {
        this.node = node;
    }

    // Node functions
    public boolean isExecutable() {
        return node.isExecutable();
    }
    public boolean hasOutputVirtualRegister() {
        return node.hasOutputVirtualRegister();
    }
    public String getOutputVirtualReg() {
        return node.getOutputVirtualReg();
    }

    public boolean visualize() {
        return node.visualize();
    }

    public boolean isControlFlow() {
        return node.isControlFlow();
    }

    public List<NodeContainer> getInputOperands() {
        return node.getInputOperands();
    }

    public void addOperand(NodeContainer node) {
        node.addOperand(node);
    }

    public int numInputOperands() {
        return node.numInputOperands();
    }

    public int getSourceIndex() {
        return node.getSourceIndex();
    }

    public void setOperandAtIndex(int i, NodeContainer operand) {
        node.setOperandAtIndex(i, operand);
    }

    public NodeContainer getOperandAtIndex(int index) {
        return node.getOperandAtIndex(index);
    }

    public Allocation getAllocation() {
        return node.getAllocation();
    }

    String displayId() {
        return node.displayId();
    }

    public void setAllocation(Allocation allocation) {
        node.setAllocation(allocation);
    }

    public boolean isRemoved() {
        return node.isRemoved();
    }

    public void setRemoved(boolean removed) {
        node.setRemoved(removed);
    }

    public String getId() {
        return node.nodeId;
    }

    @Override
    public String toString() {
        return node.toString();
    }
}
