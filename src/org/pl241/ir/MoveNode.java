package org.pl241.ir;

import java.util.ArrayList;
import java.util.Map;

public class MoveNode extends AbstractNode {
	public String memAddress;
	public ArrayList<Integer> strides;
	private String data;
	public String originalMemAddress;
	public MoveNode( String _memAddress ){
		super("move");
		memAddress = _memAddress;
		originalMemAddress = _memAddress;
		strides = new ArrayList<Integer>();
	}
	public MoveNode( String _memAddress , ArrayList<Integer>  _strides , String _data){
		this(_memAddress);
		strides.addAll(_strides);
		data = _data;
		operands.add(data);
	}
	public String toString(){
		if( strides.size()> 0 ) 
			return  "lindex " + lineIndex +  " " + super.label + ": " + operator + " " +  memAddress + strides;
		else
			return  "lindex " + lineIndex +  " " + super.label + ": " + operator + " " +  memAddress  + " " + data;
	}
	
	@Override
	public String getOutputOperand(){
		return memAddress;
	}
	public void setRightOperand(String label1) {
		data = label1 ;
		if( operands.size() > 0 )
			operands.set(0,data);
		else 
			operands.add(data);
		
	}
	
	
	
}
