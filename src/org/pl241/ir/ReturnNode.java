package org.pl241.ir;

public class ReturnNode extends AbstractNode {

	public ReturnNode (NodeContainer _returnValue) {
		super();
		this.returnValue = _returnValue;
		if (_returnValue != null)
		    this.addOperand(_returnValue);
	}


    public String toString() {
        String ret =  super.toString() + " return ";
        if (returnValue != null)
            ret += returnValue.getOutputVirtualReg();
        return ret;
    }

    @Override
    public String printAllocation() {
        String ret =  super.toString() + " return ";
        if (returnValue != null)
            ret += returnValue.getAllocation();
        return ret;
    }

    private NodeContainer returnValue;

    // Node interface implementation
    public boolean isExecutable() {
        return true;
    }
    public boolean hasOutputVirtualRegister() {
        return false;
    }
    public boolean visualize() {
        return true;
    }
}


