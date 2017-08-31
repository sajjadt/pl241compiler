package org.pl241.ir;

import java.util.*;
public class AbstractNode {

	// Unique Label used to counter and identify this node
	public String uniqueLabel;
	private static int counter = 0 ;
    public static String generateLabel (String str) {
        return str + counter++;
    }

    public AbstractNode() {
        uniqueLabel = "l" + counter++;
        removed = false ;
        sourceLocation = 1 ;
        operands = new ArrayList<>();
    }

    public AbstractNode(AbstractNode operand1, AbstractNode operand2) {
        this();
        operands.add(operand1);
        operands.add(operand2);
    }

    public AbstractNode(String _label) {
        this();
        uniqueLabel = generateLabel("L");
    }

    protected List<AbstractNode> operands ;

    // Starts from 0
    public AbstractNode getOperandAtIndex(int index) {
		if (operands.size() > index) {
			return operands.get(index);
		}

		return null;
	}

	public int sourceLocation; // In source code
	public boolean removed ;
	public String removeReason ;
	public int getSourceLocation() {
		return sourceLocation;
	}


	@Override
	public String toString() {
		return sourceLocation + ":  [" + uniqueLabel + "]";
	}

	public String getOutputOperand() {
	    /*
		if(Objects.equals(operator, "end") || Objects.equals(operator, "imm"))
			return null ;
		else
			return uniqueLabel;
			*/
	    //TODO new
        return null;
	}

	public List<AbstractNode> getInputOperands() {
	    return operands;
	}

	public void addOperand(AbstractNode node) {
		operands.add(node);
	}

}
