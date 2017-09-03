package org.pl241.ra;

public class Allocation {
    public enum Type{
		STACK,
		REGISTER
	}

	public Allocation(Allocation.Type type, int address){
		this.type = type ;
		this.address = address ;
	}

    @Override
    public String toString() {
        return "[" + type + ", " + address + "]";
    }

	public Allocation.Type type;
	public int address ; // Reg number or spill offset

}
