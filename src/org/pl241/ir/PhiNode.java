package org.pl241.ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PhiNode extends AbstractNode {
	public Map< Integer ,String > rightLabels ; // bbl index, instruction label
	public Map< Integer, String> rightOperands; // bbl index, operands
	
	
	public String memAddress;
	public String originalMemAddress;
	
	public PhiNode( String _memAddress ){
		super("phi");
		memAddress = _memAddress;
		originalMemAddress = _memAddress;
		rightLabels = new HashMap<Integer,String>();
		rightOperands = new HashMap<Integer,String>();
	}
	public String toString(){
		String oSet = "" ;
		for( String key: rightOperands.values() ){
			oSet += key;
		}
		return  "lindex " + lineIndex +  " " + super.label + ": " + operator + " " + memAddress +  " " + oSet;
	}
	public String inputOf(BasicBlock node){
		return rightOperands.get(node.getIndex());
	}
	@Override
	public String getOutputOperand() {
		// TODO Auto-generated method stub
		return memAddress;
	}
	@Override
	public List<String> getInputOperands() {
		return   new ArrayList<String>( rightOperands.values() );
	}
}
