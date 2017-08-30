package org.pl241.ir;

public class StoreNode extends AbstractNode {

	public StoreNode (String _memAddress) {
		super("store");
		memAddress = _memAddress;
		
	}
	
	public void setValue(String value) {
		this.value = value ;
	}
	
	public String toString() {
		return super.uniqueLabel + "  " + memAddress + " " + value ;
	}
	@Override
	public String getOutputOperand() {
		return null ;
	}

	public String memAddress;
	public String value ;
}


