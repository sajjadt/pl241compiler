package org.pl241.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhiNode extends AbstractNode {
	public Map< Integer ,String > rightLabels ; // bbl index, instruction nodeId
	public Map< Integer, AbstractNode> rightOperands; // bbl index, operands

	public String memAddress;
	public String originalMemAddress;
	
	public PhiNode (String _memAddress) {
		super();
		memAddress = _memAddress;
		originalMemAddress = _memAddress;
		rightLabels = new HashMap<>();
		rightOperands = new HashMap<>();
	}

	public String toString() {
		StringBuilder oSet = new StringBuilder();
		for (AbstractNode key: rightOperands.values()) {
			oSet.append(key+", ");
		}
		return super.toString() +   ": [" + sourceLocation  +"]  " + memAddress +  " phi: " + oSet;
	}

	public AbstractNode inputOf(BasicBlock block) {
		return rightOperands.get(block.getIndex());
	}

	public String getOutputOperand() {
		// TODO Auto-generated method stub
		return memAddress;
	}

	@Override
	public List<AbstractNode> getInputOperands() {
	    return new ArrayList<AbstractNode> (rightOperands.values());
	}
}
