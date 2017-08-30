package org.pl241.ir;

import java.util.HashMap;
import java.util.Map;

public class ArithmaticNode extends AbstractNode {
    public String operator;

    public ArithmaticNode(AbstractNode _operand1, AbstractNode _operand2 , String _operator) {
        super(_operand1, _operand2);
        operator = _operator;
    }

    public static enum ArithmaticType {
        NEG,
        ADD ,
        ADDA,
        SUB,
        MUL,
        DIV,
        CMP,
        END,
        BRA,
        BNE,
        PHI,
        BEQ,
        BLE,
        BLT,
        BGE,
        BGT
    }

    public static Map<String,String> operatorMap ;
    public static Map<ArithmaticType,String> operatorMapR ;
    static {
        operatorMap = new HashMap<String, String >();
        operatorMap.put("+", "ADD" );
        operatorMap.put("-", "SUB" );
        operatorMap.put("*", "MUL" );
        operatorMap.put("/", "DIV" );
        operatorMap.put("=", "MOVE" );
        operatorMap.put("==", "BEQ" );
        operatorMap.put("!=", "BNE" );
        operatorMap.put("<", "BLT");
        operatorMap.put("<=", "BLE" );
        operatorMap.put(">", "BGT" );
        operatorMap.put(">=", "BGE" );

        operatorMapR = new HashMap <ArithmaticType, String>();
        operatorMapR.put( ArithmaticType.ADD , "ADD" );
        operatorMapR.put( ArithmaticType.SUB ,"-" );
        operatorMapR.put( ArithmaticType.MUL , "" );
        operatorMapR.put( ArithmaticType.DIV ,"/");
        operatorMapR.put( ArithmaticType.BEQ , "==");
        operatorMapR.put( ArithmaticType.BNE , "!=");
        operatorMapR.put( ArithmaticType.BLT , "<");
        operatorMapR.put( ArithmaticType.BLE , "<=" );
        operatorMapR.put( ArithmaticType.BGT , ">");
        operatorMapR.put( ArithmaticType.BGE  , ">=");
    }

    public String toString() {
        return  "op: " + operator + super.toString();
    }

}
