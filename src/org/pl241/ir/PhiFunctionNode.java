package org.pl241.ir;

import java.lang.reflect.Array;
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
			oSet.append(key.toString()).append(":").append(rightOperands.get(key).getOutputVirtualReg()).append(",");
		}
		return this.sourceIndex +   ": " + variableName +  " phi:(" + oSet + ")";
	}

	public AbstractNode inputOf(BasicBlock block) {
		return rightOperands.get(block.getIndex());
	}
	@Override
	public List<AbstractNode> getInputOperands() {
	    return new ArrayList<> (rightOperands.values());
	}

	@Override
    public AbstractNode getOperandAtIndex(int index) {
        ArrayList<AbstractNode> nodes = new ArrayList<> (rightOperands.values());
        if (nodes.size() > index) {
            return nodes.get(index);
        }
        return null;
    }

    @Override
    public void setOperandAtIndex(int index, AbstractNode abstractNode) {
        ArrayList<AbstractNode> nodes = new ArrayList<> (rightOperands.values());
        nodes.set(index, abstractNode);
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
