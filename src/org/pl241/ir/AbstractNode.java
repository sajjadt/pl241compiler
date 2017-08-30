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
        uniqueLabel = generateLabel(_label);
    }

    protected List<AbstractNode> operands ;

    // Starts from 0
    public AbstractNode getOperandAtIndex(int index) {
		if( operands.size() > 0 ){
			return operands.get(0) ;	
		}
		else {
			return null;
		}
	}

	public int sourceLocation; // In source code
	public boolean removed ;
	public String removeReason ;
	public int getSourceLocation() {
		return sourceLocation;
	}


	@Override
	public String toString() {
		return  "lindex " + sourceLocation +  " " + uniqueLabel +": "  + operands.toString() ;
	}
	public List<AbstractNode> toList() {
		ArrayList<AbstractNode> list = new ArrayList<AbstractNode>() ;	
		return list;
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

}
