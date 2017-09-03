package org.pl241.ir;

public class ReturnNode extends AbstractNode {

	public ReturnNode (AbstractNode _returnValue) {
		super();
		this.returnValue = _returnValue;
	}
	public void setReturnValue (AbstractNode _returnValue) {
		this.returnValue = _returnValue ;
	}
	public String toString() {
		return super.toString() + " return " + returnValue ;
	}

	public String getOutputOperand() {
	        return null;
	}

	private AbstractNode returnValue;


    @Override
    public boolean isExecutable() {
        return true;
    }
}


