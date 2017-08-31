package org.pl241.ir;

public class ReturnNode extends AbstractNode {

	public ReturnNode (AbstractNode _returnValue) {
		super("return");
		this.returnValue = _returnValue;
	}
	public void setReturnValue (AbstractNode _returnValue) {
		this.returnValue = _returnValue ;
	}
	public String toString() {
		return super.uniqueLabel + ": return , "  +  returnValue ;
	}
	@Override
	public String getOutputOperand() {
		return null ;
	}

	private AbstractNode returnValue;
}


