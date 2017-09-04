package org.pl241.ir;

import org.pl241.ra.Allocation;

import java.util.*;
public class AbstractNode {

    public AbstractNode() {
        nodeId = generateId("L");
        removed = false;
        sourceIndex = -1;
        operands = new ArrayList<>();
        allocation = null;
    }

    public AbstractNode(AbstractNode operand1, AbstractNode operand2) {
        this();
        operands.add(operand1);
        operands.add(operand2);
    }

    public AbstractNode getOperandAtIndex(int index) {
		if (operands.size() > index) {
			return operands.get(index);
		}
		return null;
	}

    public void setOperandAtIndex(int index, AbstractNode node) {
        if (operands.size() > index)
            operands.set(index, node);
        else
            throw new Error("no operands at index" + index);
    }

    public static void reset() {
        counter = 0;
    }

    public String getOutputOperand() {
        return null;
    }
    public void setAllocation(Allocation allocation) {
        this.allocation = allocation;
    }

    static String generateId (String str) {
        return str + counter++;
    }

    @Override
    public String toString() {
        String ret = "";

        if (sourceIndex != -1)
            ret = sourceIndex + ":";

        ret += "[" + nodeId + "]";

        if (allocation != null)
                ret += allocation.toString();
        return ret;
    }

    public boolean accessVariable(String variableId) {
        return false;
    }

    // Show compact representation of node in CFGs
    public String displayId() {
        return nodeId;
    }

    public List<AbstractNode> getInputOperands() {
        return operands;
    }
    public void addOperand(AbstractNode node) {
        operands.add(node);
    }

    protected List<AbstractNode> operands ;
    public String nodeId;

    public int sourceIndex; // In source code

    public boolean removed;
	public String removeReason;


	public int getSourceIndex() {
		return sourceIndex;
	}
    private static int counter = 0;
	public Allocation allocation;

    // These function should be overriden by executable nodes
    public boolean isExecutable() {
        return false;
    }
    public boolean hasOutputRegister() {
        return  false;
    }
}
