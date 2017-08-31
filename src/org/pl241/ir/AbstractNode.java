package org.pl241.ir;

import java.util.*;
public class AbstractNode {

    public AbstractNode() {
        nodeId = generateId("L");
        removed = false ;
        sourceLocation = 1 ;
        operands = new ArrayList<>();
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

    static String generateId (String str) {
        return str + counter++;
    }

    @Override
    public String toString() {
        return sourceLocation + ":  [" + nodeId + "]";
    }

    public List<AbstractNode> getInputOperands() {
        return operands;
    }
    public void addOperand(AbstractNode node) {
        operands.add(node);
    }

    protected List<AbstractNode> operands ;
    public String nodeId;
    public int sourceLocation; // In source code
	public boolean removed ;
	public String removeReason ;
	public int getSourceLocation() {
		return sourceLocation;
	}
    private static int counter = 0;
}
