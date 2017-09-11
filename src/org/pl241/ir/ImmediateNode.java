package org.pl241.ir;

public class ImmediateNode extends AbstractNode implements NodeInterface {

    public ImmediateNode(String _value) {
		super();
		value = Integer.parseInt(_value);
	}

	@Override 
	public String toString() {
		return super.toString() + " Imm=" + value;
	}

	public int getValue() {
	    return value;
    }

    @Override
    public String displayId() {
        return String.valueOf(value);
    }

    private Integer value;

    @Override
    public String getOutputVirtualReg() {
        return value.toString();
    }

    // Node interface implementations
    public boolean isExecutable() {
        return false;
    }
    public boolean hasOutputVirtualRegister() {
        return false;
    }
    public boolean visualize() {
        return false;
    }
}
