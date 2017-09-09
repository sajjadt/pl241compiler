package org.pl241.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhiNode extends AbstractNode {

    // Mapping from block index to instruction node
	public Map<Integer, AbstractNode> rightOperands;

	public String variableName;
	public String originalVarName;
	
	public PhiNode (String variableName) {
        super();
        this.variableName = variableName;
        this.originalVarName = variableName;
        rightOperands = new HashMap<>();
    }

	public String toString() {
		StringBuilder oSet = new StringBuilder();
		for (Integer key: rightOperands.keySet()) {
			oSet.append(key.toString()).append(":").append(rightOperands.get(key).getOutputOperand()).append(", ");
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
	    return new ArrayList<> (rightOperands.values());
	}

    @Override
    public boolean isExecutable() {
        return true;
    }

	public boolean hasOutputRegister() {
		return true;
	}

}
