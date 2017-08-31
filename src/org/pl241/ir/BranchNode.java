package org.pl241.ir;

import java.util.HashMap;
import java.util.Map;

public class BranchNode extends AbstractNode{

    public BranchNode() {
        super("bra");
        type = BranchType.BRA;
    }

    public BranchNode(BranchType _type, AbstractNode _operand) {
        super("bra");
        type = _type;
        operands.add(_operand);
        takenTarget = null;
        nonTakenTarget = null;

        takenBlock = null;
        nonTakenTarget = null;
    }

    public String takenTarget;
    public String nonTakenTarget;


    public BasicBlock takenBlock;
    public BasicBlock nonTakenBlock;


    public boolean isCall  ;
    public BranchType type;

    public enum BranchType {
        BRA,
        BNE,
        BEQ,
        BLE,
        BLT,
        BGE,
        BGT,
        END, // TODO should be here?
        PHI // TODO should be here?
    }

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

	
	@Override
	public String toString() {
        String ret = super.uniqueLabel + " " + type;
        if (operands.size()>0)
            ret += " " + getOperandAtIndex(0).uniqueLabel ;
        if (takenBlock != null) ret += (", Block " + takenBlock.id);
        if (nonTakenBlock != null) ret += (", Block " + nonTakenBlock.id);
	    return ret;
    }
	
	@Override
	public String getOutputOperand() {
		return null ;
	}

	public boolean isConditioned() {
        if (type == BranchType.BRA)
            return false;
        else
            return true;
    }

}
