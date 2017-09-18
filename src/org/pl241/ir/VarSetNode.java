package org.pl241.ir;

public class VarSetNode extends AbstractNode implements NodeInterface {

    public boolean accessGlobals;

	VarSetNode(String _memAddress) {
		super();
		memAddress = _memAddress;
        originalMemAddress = memAddress;
        this.accessGlobals = false;
    }

	void setSrcOperand(NodeContainer srcOperand) {
        if (numInputOperands() > 0)
            super.operands.set(0, srcOperand);
        else
	        super.addOperand(srcOperand);
	}
	
	public String toString() {
	    String ret = this.sourceIndex + ": ";
		if (operands.size()>0) {
            ret += memAddress + "=" + getOperandAtIndex(0).getOutputVirtualReg();
        }
        if (accessGlobals)
            ret += "*G*";
	    return ret;
	}

    @Override
    public String printAllocation() {
        String ret = getAllocation() + "=";
        if (operands.size()>0) {
            if (operands.get(0).getAllocation() != null)
                 ret += getOperandAtIndex(0).getAllocation();
            else
                ret += getOperandAtIndex(0).getOutputVirtualReg();
        }
        return ret;
    }


	public String getOutputVirtualReg() {
		return memAddress;
	}

	public String memAddress;
    public String originalMemAddress;

    @Override
    public boolean hasOutputVirtualRegister() {
        return true;
    }
    @Override
    public boolean isExecutable() {
        return true;
    }
    public boolean visualize() {
        return true;
    }

}


