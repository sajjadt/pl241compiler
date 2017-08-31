package org.pl241.ir;

import java.util.HashMap;
import java.util.Map;

public class ArithmeticNode extends AbstractNode {

    public enum ArithmeticType {
        NEG,
        ADD,
        ADDA,
        SUB,
        MUL,
        DIV,
        CMP
    }

    public ArithmeticNode(AbstractNode _operand1, AbstractNode _operand2 , ArithmeticType _operator) {
        super(_operand1, _operand2);
        operator = _operator;
    }

    public static ArithmeticType toType(String textOperator) {
        return operatorMap.get(textOperator);
    }

    public String toString() {
        String operands = this.operands.get(0).nodeId;
        if (this.operands.size() > 1)
            operands +=  ", " + this.operands.get(1).nodeId;
        return super.toString() + " "+ operatorMapR.get(operator) + " " + operands;
    }

    public ArithmeticType operator;
    public static Map<String, ArithmeticType> operatorMap ;
    public static Map<ArithmeticType,String> operatorMapR ;
    static {
        operatorMap = new HashMap<>();

        operatorMap.put("+", ArithmeticType.ADD );
        operatorMap.put("-", ArithmeticType.SUB );
        operatorMap.put("*", ArithmeticType.MUL );
        operatorMap.put("/", ArithmeticType.DIV );
        //branchMap.put("=", ArithmaticType.MOVE );

        operatorMapR = new HashMap <ArithmeticType, String>();
        operatorMapR.put( ArithmeticType.ADD , "+" );
        operatorMapR.put( ArithmeticType.SUB ,"-" );
        operatorMapR.put( ArithmeticType.MUL , "*" );
        operatorMapR.put( ArithmeticType.DIV ,"/");
        operatorMapR.put( ArithmeticType.CMP ,"cmp");
    }
}
