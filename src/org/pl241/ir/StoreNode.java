package org.pl241.ir;

public class StoreNode extends AbstractNode {

	StoreNode(String _memAddress) {
		super("store");
		memAddress = _memAddress;
        originalMemAddress = memAddress;

    }
	
	void setSrcOperand(AbstractNode srcOperand) {
		super.addOperand(srcOperand);
	}
	
	public String toString() {
	    String ret = super.toString();
		if (operands.size()>0)
		    ret += memAddress + "=" + getOperandAtIndex(0).uniqueLabel;
	    return ret;
	}
	@Override
	public String getOutputOperand() {
		return null ;
	}

	public String memAddress;
    public String originalMemAddress;

}


