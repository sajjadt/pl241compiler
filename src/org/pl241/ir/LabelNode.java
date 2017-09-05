package org.pl241.ir;

public class LabelNode extends AbstractNode{
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
    public String getOutputOperand() {
        return label;
    }
}