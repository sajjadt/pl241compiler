package org.pl241.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhiFunctionNode extends AbstractNode {

    // Mapping from block index to instruction node
	public Map<Integer, AbstractNode> rightOperands;

	public String variableName;
	public String originalVariableName;
	
	public PhiFunctionNode(String variableName) {
        super();
        this.variableName = variableName;
        this.originalVariableName = variableName;
        rightOperands = new HashMap<>();
    }

	public String toString() {
		StringBuilder oSet = new StringBuilder();
		for (Integer key: rightOperands.keySet()) {
			oSet.append(key.toString()).append(":").append(rightOperands.get(key).getOutputVirtualReg()).append(", ");
		}
		return super.toString() +   ": [" + sourceIndex +"]  " + variableName +  " phi: " + oSet;
	}

	public AbstractNode inputOf(BasicBlock block) {
		return rightOperands.get(block.getIndex());
	}
	@Override
	public List<AbstractNode> getInputOperands() {
	    return new ArrayList<> (rightOperands.values());
	}


    // Node interface implementation
    public boolean isExecutable() {
        return true;
    }
    public boolean hasOutputVirtualRegister() {
        return true;
    }
    public String getOutputVirtualReg() {
        return variableName;
    }
    public boolean visualize() {
        return true;
    }

}
