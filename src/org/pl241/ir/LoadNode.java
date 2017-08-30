package org.pl241.ir;

public class LoadNode extends AbstractNode {
	public String memAddress;
	public LoadNode( String _memAddress ){
		super("load");
		memAddress = _memAddress;
	}
	public String toString(){
		return super.uniqueLabel + ":  LD " +  memAddress ;
	}
	@Override
	public String getOutputOperand(){
		return null ;
	}
}
