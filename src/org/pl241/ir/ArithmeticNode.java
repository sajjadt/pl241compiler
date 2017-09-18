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
        CMP,
        ADDA;

        @Override
        public String toString() {
            switch(this) {
                case NEG: return "!";
                case ADD: return "+";
                case ADDA: return "+";
                case SUB: return "-";
                case MUL: return "*";
                case DIV: return "/";
                case CMP: return "cmp";
                default: throw new IllegalArgumentException();
            }
        }
        public boolean isSymmetric () {
            return this == Type.ADD || this == Type.MUL;
        }
    }

    public ArithmeticNode(NodeContainer _operand1, NodeContainer _operand2, Type _operator) {
        super(_operand1, _operand2);
        operator = _operator;
    }

    public String toString() {
        String ret = super.toString();

        if (this.operands.size() == 1)
            ret += operator.toString() + " " + this.operands.get(0).displayId();
        else
            ret += this.operands.get(0).displayId() + " " + operator.toString() + " " + this.operands.get(1).displayId();
        return ret;
    }

    @Override
    public String printAllocation() {
        String ret = this.getAllocation() + " = ";

        Allocation al = this.operands.get(0).getAllocation();
        if (al != null)
            ret = ret + al.toString();

        ret += operatorMapR.get(operator);
        if (this.operands.size() > 1) {
            al = this.operands.get(1).getAllocation();
            if (al != null)
                ret +=  this.operands.get(1).getAllocation().toString();
        }

        return ret;
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
        operatorMapR.put( Type.ADDA ,"++");
    }
}
