package org.pl241.ir;

public class StoreNode extends AbstractNode {
	public String memAddress;
	public String value ;
	public StoreNode( String _memAddress ){
		super("store");
		memAddress = _memAddress;
		
	}
	
	public void setValue(String value){
		this.value = value ;
	}
	
	public String toString(){
		return super.label + ": " + operator + " " + memAddress + " " + value ;
	}
	@Override
	public String getOutputOperand(){
		return null ;
	}
}


