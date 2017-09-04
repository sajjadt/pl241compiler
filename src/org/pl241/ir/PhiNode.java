package org.pl241.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhiNode extends AbstractNode {

    public Map<Integer, String> rightLabels ; // bbl index, instruction nodeId
	public Map<Integer, AbstractNode> rightOperands; // bbl index, operands


	public String variableName;
	public String originalVarName;
	
	public PhiNode (String variableName) {
		super();
		this.variableName = variableName;
		this.originalVarName = variableName;
		rightLabels = new HashMap<>();
		rightOperands = new HashMap<>();
	}

	public String toString() {
		StringBuilder oSet = new StringBuilder();
		for (AbstractNode key: rightOperands.values()) {
			oSet.append(key+", ");
		}
		return super.toString() +   ": [" + sourceIndex +"]  " + variableName +  " phi: " + oSet;
	}

	public AbstractNode inputOf(BasicBlock block) {
		return rightOperands.get(block.getIndex());
	}

	public String getOutputOperand() {
		return variableName;
	}



	@Override
	public List<AbstractNode> getInputOperands() {
	    return new ArrayList<AbstractNode> (rightOperands.values());
	}

    @Override
    public boolean isExecutable() {
        return true;
    }


}
