package org.pl241.ra;

public class Allocation implements Comparable<Allocation> {
    public enum Type{
		STACK,
		REGISTER,
        IMMEDIATE
	}

	public Allocation(Allocation.Type type, int address){
		this.type = type ;
		this.address = address ;
	}

    @Override
    public String toString() {
	    if (type == Type.REGISTER)
	        return "R." + address;
	    else
            return "Stack[" + address + "]";
    }

	public Allocation.Type type;
	public int address ; // Reg number or spill offset

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
}
