package org.pl241.ir;

import java.util.HashMap;
import java.util.Map;

public class ArithmeticNode extends AbstractNode {

    public enum ArithmeticType {
        NEG,
        ADD ,
        ADDA,
        SUB,
        MUL,
        DIV,
        CMP
    }

    public ArithmeticType operator;

    public ArithmeticNode(AbstractNode _operand1, AbstractNode _operand2 , ArithmeticType _operator) {
        super(_operand1, _operand2);
        operator = _operator;
    }



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
        operatorMapR.put( ArithmeticType.ADD , "ADD" );
        operatorMapR.put( ArithmeticType.SUB ,"-" );
        operatorMapR.put( ArithmeticType.MUL , "" );
        operatorMapR.put( ArithmeticType.DIV ,"/");
    }

    public static ArithmeticType toType(String textOperator) {
        return operatorMap.get(textOperator);
    }

    public String toString() {
        return  "op: " + operatorMapR.get(operator); //+ super.toString();
    }

}
