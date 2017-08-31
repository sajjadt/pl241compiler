package org.pl241.ir;

public class LoadNode extends AbstractNode {
	public String memAddress;
	public LoadNode(String _memAddress) {
		super();
		memAddress = _memAddress;
	}
	public String toString() {
		return super.nodeId + " read-var=" + memAddress ;
	}

	public String getOutputOperand() {
		return null ;
	}
}
