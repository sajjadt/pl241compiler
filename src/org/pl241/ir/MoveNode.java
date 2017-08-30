package org.pl241.ir;

import java.util.ArrayList;

public class MoveNode extends AbstractNode {
	public String memAddress;
	public ArrayList<Integer> strides;
	private AbstractNode data;
	public String originalMemAddress;
	public MoveNode( String _memAddress ){
		super("move");
		memAddress = _memAddress;
		originalMemAddress = _memAddress;
		strides = new ArrayList<Integer>();
	}
	public MoveNode( String _memAddress , ArrayList<Integer>  _strides , AbstractNode _data){
		this(_memAddress);
		strides.addAll(_strides);
		data = _data;
		operands.add(data);
	}
	public String toString(){
		if( strides.size()> 0 ) 
			return  "lindex " + sourceLocation +  " " + super.uniqueLabel + ": " +  memAddress + strides;
		else
			return  "lindex " + sourceLocation +  " " + super.uniqueLabel + ": " +  memAddress  + " " + data;
	}
	
	@Override
	public String getOutputOperand(){
		return memAddress;
	}
	public void setRightOperand(AbstractNode label1) {
		data = label1 ;
		if( operands.size() > 0 )
			operands.set(0,data);
		else 
			operands.add(data);
		
	}
	
	
	
}
