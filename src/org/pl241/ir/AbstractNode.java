package org.pl241.ir;

import org.pl241.ra.Allocation;
import java.util.*;

public class AbstractNode implements NodeInterface {

    AbstractNode() {
        nodeId = generateId("L");
        removed = false;
        sourceIndex = -1;
        operands = new ArrayList<>();
        allocation = null;
    }

    AbstractNode(NodeContainer operand1, NodeContainer operand2) {
        this();
        operands.add(operand1);
        operands.add(operand2);
    }

    public NodeContainer getOperandAtIndex(int index) {
		if (operands.size() > index) {
			return operands.get(index);
		}
		return null;
	}

    public static void resetNodeCounter() {
        counter = 0;
    }

    public void setAllocation(Allocation allocation) {
        this.allocation = allocation;
    }

    private static String generateId(String str) {
        return str + counter++;
    }

    @Override
    public String toString() {
        String ret = "";

        if (sourceIndex != -1)
            ret = sourceIndex + ": ";

        if (hasOutputVirtualRegister())
            ret += nodeId + " = ";

        if (allocation != null)
            ret += allocation.toString();
        return ret;
    }


    // Show compact representation of node in CFGs
    String displayId() {
        return nodeId;
    }

    public List<NodeContainer> getInputOperands() {
        return operands;
    }
    public void addOperand(NodeContainer node) {
        operands.add(node);
    }


    public int numInputOperands() {
        return getInputOperands().size();
    }

	public int getSourceIndex() {
		return sourceIndex;
	}


    public String printAllocation() {
        return null;
    }

    // Node interface implementation
    public boolean isExecutable() {
        return false;
    }
    public boolean hasOutputVirtualRegister() {
        return false;
    }
    public String getOutputVirtualReg() {
        return null;
    }

    @Override
    public boolean visualize() {
        return false;
    }

    public void setOperandAtIndex(int i, NodeContainer node) {
        assert operands.size() > i;
        operands.set(i, node);
    }

    public boolean isControlFlow() {
        return false;
    }

    public Allocation getAllocation() {
        return allocation;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    private static int counter = 0;
    private Allocation allocation;
    List<NodeContainer> operands ;
    public String nodeId;
    public int sourceIndex; // In source code
    private boolean removed;
    public String removeReason;
}
