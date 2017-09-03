package org.pl241.ir;

import java.util.ArrayList;

public class CopyNode extends AbstractNode {

	public CopyNode(String _memAddress ){
		super();
		memAddress = _memAddress;
		originalMemAddress = _memAddress;
		strides = new ArrayList<Integer>();
	}

	public CopyNode(String _memAddress , ArrayList<Integer>  _strides , AbstractNode _data){
		this(_memAddress);
		strides.addAll(_strides);
		data = _data;
		operands.add(data);
	}

	@Override
	public String toString(){
		if( strides.size()> 0 ) 
			return  "lindex " + sourceIndex +  " " + super.nodeId + ": " +  memAddress + strides;
		else
			return  "lindex " + sourceIndex +  " " + super.nodeId + ": " +  memAddress  + " " + data;
	}
	
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

    public String memAddress;
    public ArrayList<Integer> strides;
    private AbstractNode data;
    public String originalMemAddress;
	
}
