package org.pl241.ir;

public class BranchNode extends AbstractNode{
	public String jumpTarget;
	public String conditions; 
	public boolean conditioned ;
	public boolean isCall  ;
	
	
	
	public BranchNode()
	{
		super("bra");
		conditioned = false ;
	}
	
	public BranchNode(String type, AbstractNode _operand)
	{
		super(type);
		conditioned = true; // TODO
		operands.add(_operand) ;
	}
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return  "lindex " + sourceLocation +  " " + super.uniqueLabel + ":  BRA " + operands.toString() + " " +   jumpTarget ;
	}
	
	@Override
	public String getOutputOperand(){
		return null ;
	}
}
