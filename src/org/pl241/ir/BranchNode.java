package org.pl241.ir;

import java.util.HashMap;
import java.util.Map;

public class BranchNode extends AbstractNode{

    public enum Type {
        BRA, // Used for returns as well
        BNE,
        BEQ,
        BLE,
        BLT,
        BGE,
        BGT;

        @Override
        public String toString() {
            switch(this) {
                case BRA: return "jmp";
                case BNE: return "bne";
                case BEQ: return "beq";
                case BLE: return "ble";
                case BLT: return "blt";
                case BGE: return "bge";
                case BGT: return "bgt";
                default: throw new IllegalArgumentException();
            }
        }
    }

    public BranchNode() {
        type = Type.BRA;
    }

    public BranchNode(Type _type, AbstractNode _operand) {
        super();
        type = _type;
        operands.add(_operand);
        takenBlock = null;
        nonTakenBlock = null;
    }

    @Override
    public String toString() {
        String ret = super.toString() + " " + type;

        if (isCall) {
            ret += ("(" + callTarget + ")");
        } else {
            if (operands.size() > 0)
                ret += " " + getOperandAtIndex(0).nodeId;
            if (takenBlock != null) ret += (", Block " + takenBlock.getID());
            if (nonTakenBlock != null) ret += (", Block " + nonTakenBlock.getID());
        }
        return ret;
    }

    public boolean isConditioned() {
        if (type == Type.BRA)
            return false;
        else
            return true;
    }


    @Override
    public String getOutputOperand() {
        return null;
    }

    @Override
    public boolean hasOutputRegister() {
        return false;
    }
    @Override
    public boolean isExecutable() {
        return true;
    }

    public BasicBlock takenBlock;
    public BasicBlock nonTakenBlock;

    boolean isCall;
    public String callTarget;
    public Type type;

    public static Map<String, Type> branchMap;
    public static Map<Type, String> branchMapR;
    static {
        branchMap = new HashMap<>();

        branchMap.put("==", Type.BEQ );
        branchMap.put("!=", Type.BNE );
        branchMap.put("<", Type.BLT);
        branchMap.put("<=", Type.BLE );
        branchMap.put(">", Type.BGT );
        branchMap.put(">=", Type.BGE );

        branchMapR = new HashMap <>();
        branchMapR.put( Type.BEQ , "==");
        branchMapR.put( Type.BNE , "!=");
        branchMapR.put( Type.BLT , "<");
        branchMapR.put( Type.BLE , "<=" );
        branchMapR.put( Type.BGT , ">");
        branchMapR.put( Type.BGE  , ">=");
    }

}
