package org.pl241.ir;

import java.util.Objects;

public class VarGetNode extends AbstractNode implements NodeInterface{
	public String variableId;
	public boolean accessGlobals;

	public VarGetNode(String variableId) {
		super();
		this.variableId = variableId;
		this.accessGlobals = false;
	}
	public String toString() {
		String ret = super.nodeId + "=" + variableId ;
        if (accessGlobals)
            ret += "*G*";
        return ret;
	}

	@Override
	public String getOutputVirtualReg() {
		return variableId ;
	}

    //@Override
    //public boolean accessVariable(String variableId) {
     //   return Objects.equals(this.variableId, variableId);
   // }

    @Override
    public String displayId() {
        return variableId;
    }


    // Node interface implementation
    public boolean isExecutable() {
        return false;
    }
    public boolean hasOutputVirtualRegister() {
        return true;
    }
    public boolean visualize() {
        return false;
    }

}

