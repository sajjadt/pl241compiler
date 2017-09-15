package org.pl241.ir;

public class VarSetNode extends AbstractNode implements NodeInterface {

	VarSetNode(String _memAddress) {
		super();
		memAddress = _memAddress;
        originalMemAddress = memAddress;
    }

    VarSetNode(String _memAddress, AbstractNode srcOperand) {
        super();
        memAddress = _memAddress;
        originalMemAddress = memAddress;
        super.addOperand(srcOperand);
    }

	void setSrcOperand(AbstractNode srcOperand) {
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
	    return ret;
	}

    @Override
    public String printAllocation() {
        String ret = allocation + "=";
        if (operands.size()>0) {
            if (operands.get(0).allocation != null)
                 ret += getOperandAtIndex(0).allocation;
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


