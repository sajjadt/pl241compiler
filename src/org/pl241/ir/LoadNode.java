package org.pl241.ir;

import java.util.ArrayList;

public class LoadNode extends AbstractNode {
	public String memAddress;
	public LoadNode( String _memAddress ){
		super("load");
		memAddress = _memAddress;
	}
	public String toString(){
		return super.label + ": " + operator + " " +  memAddress ;
	}
	@Override
	public String getOutputOperand(){
		return null ;
	}
}
