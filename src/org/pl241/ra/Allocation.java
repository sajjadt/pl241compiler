package org.pl241.ra;

public class Allocation implements Comparable<Allocation> {
    public enum Type{
		STACK,
		GENERAL_REGISTER,
        SCRATCH_REGISTER
	}

	public Allocation(Allocation.Type type, Integer address){
		this.type = type ;
		this.address = address ;
	}

    @Override
    public String toString() {
	    if (type == Type.GENERAL_REGISTER)
	        return "R." + address;
	    else if (type == Type.SCRATCH_REGISTER)
            return "R.scratch" ;
        else if (type == Type.STACK)
            return "Stack[" + address + "]";
        else
            throw new Error("Undefined allocation type");
    }
	@Override
	public int compareTo(Allocation allocation) {
		if (this.type == allocation.type &&
                this.address == allocation.address)
            return 0;
		return -1;
	}


    @Override
    public boolean equals(Object obj) {

        return (obj instanceof Allocation &&
                this.type == ((Allocation)obj).type &&
                this.address == ((Allocation)obj).address);
    }

    public static Allocation getScratchRegister() {
	    return new Allocation(Type.SCRATCH_REGISTER, null);
    }

    public Allocation.Type type;
    public Integer address ; // Reg number or spill jumpAddress

}
