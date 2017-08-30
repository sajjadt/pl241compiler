package org.pl241.ir;

public class LabelNode extends AbstractNode{
    private String label ;
    public LabelNode(String _label){
        super("uniqueLabel");
        label = _label ;
    }
    @Override
    public String toString(){
        return label;
    }
}