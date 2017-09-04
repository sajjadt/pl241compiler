package org.pl241.ir;

public class ImmediateNode extends AbstractNode{

    public ImmediateNode(String _value) {
		super();
		value = Integer.parseInt(_value);
	}

	@Override 
	public String toString() {
		return super.toString() + " Imm=" + value;
	}

    @Override
    public boolean isExecutable() {
        return false;
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
    public String getOutputOperand() {
        return value.toString();
    }
}
