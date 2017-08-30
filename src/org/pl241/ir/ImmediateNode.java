package org.pl241.ir;

public class ImmediateNode extends AbstractNode{
	private String value ;
	public ImmediateNode(String _value){
		super("Imm");
		value = _value ;
	}
	@Override 
	public String toString(){
		return  uniqueLabel +": IMM " + value ;
	}
}
