package org.pl241.ir;

import java.util.HashMap;
import java.util.Map;

public class BranchNode extends AbstractNode{

    public enum BranchType {
        BRA, // Used for returns as well
        BNE,
        BEQ,
        BLE,
        BLT,
        BGE,
        BGT
    }

    public BranchNode() {
        type = BranchType.BRA;
    }

    public BranchNode(BranchType _type, AbstractNode _operand) {
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
            if (takenBlock != null) ret += (", Block " + takenBlock.id);
            if (nonTakenBlock != null) ret += (", Block " + nonTakenBlock.id);
        }
        return ret;
    }

    public boolean isConditioned() {
        if (type == BranchType.BRA)
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
    public BranchType type;

    public static Map<String, BranchType> branchMap;
    public static Map<BranchType, String> branchMapR;
    static {
        branchMap = new HashMap<>();

        branchMap.put("==", BranchType.BEQ );
        branchMap.put("!=", BranchType.BNE );
        branchMap.put("<", BranchType.BLT);
        branchMap.put("<=", BranchType.BLE );
        branchMap.put(">", BranchType.BGT );
        branchMap.put(">=", BranchType.BGE );

        branchMapR = new HashMap <>();
        branchMapR.put( BranchType.BEQ , "==");
        branchMapR.put( BranchType.BNE , "!=");
        branchMapR.put( BranchType.BLT , "<");
        branchMapR.put( BranchType.BLE , "<=" );
        branchMapR.put( BranchType.BGT , ">");
        branchMapR.put( BranchType.BGE  , ">=");
    }

}
