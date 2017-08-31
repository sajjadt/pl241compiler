package org.pl241.ir;

public class ImmediateNode extends AbstractNode{
	private int value ;
	public ImmediateNode(String _value){
		super("Imm");
		value = Integer.parseInt(_value) ;
	}
	@Override 
	public String toString(){
		return  uniqueLabel +": IMM " + value ;
	}
	public int getValue() {
	    return value;
    }
}
