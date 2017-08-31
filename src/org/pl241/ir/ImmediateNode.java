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

	public int getValue() {
	    return value;
    }

	private int value;
}
