package org.pl241.ir;

import java.util.Objects;

public class LoadNode extends AbstractNode {
	public String variableId;
	public LoadNode(String variableId) {
		super();
		this.variableId = variableId;
	}
	public String toString() {
		return super.nodeId + " read-var=" + variableId;
	}

	@Override
	public String getOutputOperand() {
		return variableId ;
	}

    @Override
    public boolean accessVariable(String variableId) {
        return Objects.equals(this.variableId, variableId);
    }


    @Override
    public String displayId() {
        return variableId;
    }
}

