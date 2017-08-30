package org.pl241.ir;

public class ReturnNode extends AbstractNode {

	public ReturnNode (String retVal) {
		super("return");
		this.retVal = retVal;
	}
	
	public void setReturnValue (String retVal) {
		this.retVal = retVal ;
	}
	
	public String toString() {
		return super.uniqueLabel + ": return , "  +  retVal ;
	}
	@Override
	public String getOutputOperand() {
		return null ;
	}

	private String retVal;
}


