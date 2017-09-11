package org.pl241.ir;

import org.pl241.ra.Allocation;

import java.util.HashMap;
import java.util.Map;

public class ArithmeticNode extends AbstractNode implements NodeInterface {

    public enum Type {
        NEG,
        ADD,
        SUB,
        MUL,
        DIV,
        CMP;

        @Override
        public String toString() {
            switch(this) {
                case NEG: return "!";
                case ADD: return "+";
                case SUB: return "-";
                case MUL: return "*";
                case DIV: return "/";
                case CMP: return "check";
                default: throw new IllegalArgumentException();
            }
        }
        public boolean isSymmetric () {
            return this == Type.ADD || this == Type.MUL;
        }
    }

    public ArithmeticNode(AbstractNode _operand1, AbstractNode _operand2, Type _operator) {
        super(_operand1, _operand2);
        operator = _operator;
    }


    public String toString() {
        String operands = this.operands.get(0).displayId();
        if (this.operands.size() > 1)
            operands +=  ", " + this.operands.get(1).displayId();
        return super.toString() + " "+ operatorMapR.get(operator) + " " + operands;
    }

    @Override
    public String printAllocation() {
        String ret = "";
        Allocation al = this.operands.get(0).allocation;
        if (al != null)
            ret = ret + al.toString();

        if (this.operands.size() > 1) {
            al = this.operands.get(1).allocation;
            if (al != null)
                ret +=  ", " + this.operands.get(1).allocation.toString();
        }
        return this.allocation + " "+ operatorMapR.get(operator) + " " + ret;
    }

    @Override
    public String getOutputVirtualReg() {
        return nodeId;
    }

    public boolean hasOutputVirtualRegister() {
        return true;
    }
    public boolean isExecutable() {
        return true;
    }
    public boolean visualize() {
        return true;
    }

    public Type operator;
    public static Map<String, Type> operatorMap;
    private static Map<Type,String> operatorMapR;
    static {
        operatorMap = new HashMap<>();

        operatorMap.put("+", Type.ADD );
        operatorMap.put("-", Type.SUB );
        operatorMap.put("*", Type.MUL );
        operatorMap.put("/", Type.DIV );

        operatorMapR = new HashMap <>();
        operatorMapR.put( Type.ADD , "+" );
        operatorMapR.put( Type.SUB ,"-" );
        operatorMapR.put( Type.MUL , "*" );
        operatorMapR.put( Type.DIV ,"/");
        operatorMapR.put( Type.CMP ,"cmp");
    }
}
