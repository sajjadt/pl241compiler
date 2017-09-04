package org.pl241.ir;

public class ReturnNode extends AbstractNode {

	public ReturnNode (AbstractNode _returnValue) {
		super();
		this.returnValue = _returnValue;
	}
	public void setReturnValue (AbstractNode _returnValue) {
		this.returnValue = _returnValue ;
	}


    @Override
    public boolean isExecutable() {
        return true;
    }

    public String toString() {
        String ret =  super.toString() + " return ";
        if (returnValue != null)
            ret += returnValue.getOutputOperand();
        return ret;
    }
    private AbstractNode returnValue;
}


