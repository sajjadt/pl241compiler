package org.pl241.ir;

public class ReturnNode extends AbstractNode {
	private String retVal;
	public ReturnNode( String retVal ){
		super("return");
		this.retVal = retVal;
	}
	
	public void setReturnValue(String retVal){
		this.retVal = retVal ;
	}
	
	public String toString(){
		return super.label + ": " + operator + " " +  retVal ;
	}
	@Override
	public String getOutputOperand(){
		return null ;
	}
}


