package org.pl241.ir;

import java.util.HashMap;
import java.util.Map;

public class AddressCalcNode extends AbstractNode {

    public AddressCalcNode(String variableName, AbstractNode offset) {
        super();
        this.variableName = variableName;
        this.addOperand(offset);
    }

    public String toString() {
        String ret = super.toString() +  "ADDA ";
        if (this.operands.size() > 0)
            ret += variableName + "," + this.operands.get(0).getOutputOperand();
        return ret;
    }

    @Override
    public String getOutputOperand() {
        return nodeId;
    }
    @Override
    public boolean hasOutputRegister() {
        return true;
    }
    @Override
    public boolean isExecutable() {
        return true;
    }


    public String variableName;

}
