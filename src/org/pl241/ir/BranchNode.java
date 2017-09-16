package org.pl241.ir;

import java.util.HashMap;
import java.util.Map;

public class BranchNode extends AbstractNode implements NodeInterface{

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
        public boolean isConditioned() {
            return this != Type.BRA;
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
        fallThroughBlock = null;
    }

    @Override
    public String toString() {
        String ret = super.toString() + " " + type;

        if (isCall) {
            ret += ("(" + callTarget + ")");
        } else {
            if (operands.size() > 0)
                ret += " " + getOperandAtIndex(0).nodeId;
            if (takenBlock != null) ret += (" TakenBl:" + takenBlock.getID());
            if (fallThroughBlock != null) ret += (" FallThBl:" + fallThroughBlock.getID());
        }
        return ret;
    }


    @Override
    public String printAllocation() {
        String ret = type.toString();

        if (isCall) {
            ret += ("(" + callTarget + ")");
        } else {
            if (operands.size() > 0)
                ret += " " + getOperandAtIndex(0).allocation;
            if (takenBlock != null) ret += (", TakenBl " + takenBlock.getID());
            if (fallThroughBlock != null) ret += (", FallThBl " + fallThroughBlock.getID());
        }
        return ret;
    }

    public boolean isConditioned() {
        return type.isConditioned();
    }

    @Override
    public String getOutputVirtualReg() {
        return null;
    }

    public boolean hasOutputVirtualRegister() {
        return false;
    }
    public boolean isExecutable() {
        return true;
    }
    public boolean visualize() {
        return true;
    }

    public BasicBlock takenBlock;
    public BasicBlock fallThroughBlock;

    boolean isCall;
    private String callTarget;
    public Type type;

    private static Map<String, Type> branchMap;
    public static Map<String, Type> branchMapReversed;
    private static Map<Type, String> branchMapR;
    static {
        branchMap = new HashMap<>();

        branchMap.put("==", Type.BEQ );
        branchMap.put("!=", Type.BNE );
        branchMap.put("<", Type.BLT);
        branchMap.put("<=", Type.BLE );
        branchMap.put(">", Type.BGT );
        branchMap.put(">=", Type.BGE );

        branchMapReversed = new HashMap<>();
        branchMapReversed.put("==", Type.BNE);
        branchMapReversed.put("!=", Type.BEQ);
        branchMapReversed.put("<", Type.BGE);
        branchMapReversed.put("<=", Type.BGT);
        branchMapReversed.put(">", Type.BLE);
        branchMapReversed.put(">=", Type.BLT);

        branchMapR = new HashMap <>();
        branchMapR.put( Type.BEQ , "==");
        branchMapR.put( Type.BNE , "!=");
        branchMapR.put( Type.BLT , "<");
        branchMapR.put( Type.BLE , "<=" );
        branchMapR.put( Type.BGT , ">");
        branchMapR.put( Type.BGE  , ">=");
    }

    @Override
    public boolean isControlFlow() {
        return true;
    }
}
