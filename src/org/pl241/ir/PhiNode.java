package org.pl241.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhiNode extends AbstractNode {
	public Map< Integer ,String > rightLabels ; // bbl index, instruction uniqueLabel
	public Map< Integer, AbstractNode> rightOperands; // bbl index, operands

	public String memAddress;
	public String originalMemAddress;
	
	public PhiNode (String _memAddress) {
		super("phi");
		memAddress = _memAddress;
		originalMemAddress = _memAddress;
		rightLabels = new HashMap<Integer,String>();
		rightOperands = new HashMap<Integer,AbstractNode>();
	}

	public String toString() {
		StringBuilder oSet = new StringBuilder();
		for (AbstractNode key: rightOperands.values()) {
			oSet.append(key);
		}
		return  "lindex " + sourceLocation +  " " + super.uniqueLabel + ":  " + memAddress +  " " + oSet;
	}
	public AbstractNode inputOf(BasicBlock block) {
		return rightOperands.get(block.getIndex());
	}

	@Override
	public String getOutputOperand() {
		// TODO Auto-generated method stub
		return memAddress;
	}
	@Override
	public List<AbstractNode> getInputOperands() {
	    return new ArrayList<AbstractNode> (rightOperands.values());
	}
}
