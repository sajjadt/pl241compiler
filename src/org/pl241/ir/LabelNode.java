package org.pl241.ir;

// This node is used to reference to variables
public class LabelNode extends AbstractNode implements  NodeInterface{
    private String label ;
    public LabelNode(String _label){
        super();
        label = _label ;
    }
    @Override
    public String toString(){
        return label;
    }
    public String getLabel() {
        return label;
    }

    @Override
    public String getOutputVirtualReg() {
        return label;
    }


    // Node interface implementation
    public boolean isExecutable() {
        return false;
    }
    public boolean hasOutputVirtualRegister() {
        return false;
    }
    public boolean visualize() {
        return false;
    }
}