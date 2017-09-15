package org.pl241.ir;

import org.pl241.ra.Allocation;

public class AtomicFunctionNode extends AbstractNode implements NodeInterface {

    public enum IOType {
        READ,
        WRITE,
        WRITELINE;

        @Override
        public String toString() {
            switch(this) {
                case READ: return "read";
                case WRITE: return "write";
                case WRITELINE: return "writeln";
                default: throw new IllegalArgumentException();
            }
        }
    }

    public AtomicFunctionNode() {
        super();
    }

    public AtomicFunctionNode(IOType type , AbstractNode operand) {
        super();
        setParams(type, operand);
    }

    public void setParams(IOType type , AbstractNode operand) {
        this.type = type;
        isAMemoryLoad = isAMemoryStore = false;
        if (type.equals(IOType.WRITE)){
            if (operands.isEmpty())
                operands.add(operand);
            else
                operands.set(0, operand);
        }

        if (type.equals(IOType.READ))
            isAMemoryLoad = true;
        else if( type.equals(IOType.WRITE))
            isAMemoryStore = true;
    }

    @Override
    public String toString() {
        String ret = super.toString() + " ";
        if (type == IOType.WRITE)
            ret  += (type + "(" + getOperandAtIndex(0).getOutputVirtualReg() + ")");
        else
            ret  += (type + "()");
        return ret;
    }

    public String getOutputVirtualReg() {
        if( this.isAMemoryLoad){
            return super.nodeId;
        } else {
            return null;
        }
    }

	public boolean isAMemoryLoad;
    public boolean isAMemoryStore;
	public IOType type;


    @Override
    public String printAllocation() {
        String ret = "";

        if (this.allocation != null)
                ret += this.allocation.toString();

        ret += this.type.toString();

        if (this.operands.size() > 0) {
            Allocation al = this.operands.get(0).allocation;
            if (al != null)
                ret +=  ", " + al.toString();
        }
        return ret;
    }

    @Override
    public boolean hasOutputVirtualRegister() {
        return isAMemoryLoad;
    }
    public boolean isExecutable() {
        return true;
    }
    public boolean visualize() {
        return true;
    }
}
