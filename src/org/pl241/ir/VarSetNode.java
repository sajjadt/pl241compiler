package org.pl241.ir;

public class VarSetNode extends AbstractNode {

	VarSetNode(String _memAddress) {
		super();
		memAddress = _memAddress;
        originalMemAddress = memAddress;

    }

	void setSrcOperand(AbstractNode srcOperand) {
		super.addOperand(srcOperand);
	}
	
	public String toString() {
	    String ret = super.toString();
		if (operands.size()>0) {
            ret += memAddress + "=" + getOperandAtIndex(0).getOutputOperand();
        }
	    return ret;
	}

	public String getOutputOperand() {
		return memAddress;
	}

	public String memAddress;
    public String originalMemAddress;

    @Override
    public boolean hasOutputRegister() {
        return true;
    }
    @Override
    public boolean isExecutable() {
        return true;
    }

}


