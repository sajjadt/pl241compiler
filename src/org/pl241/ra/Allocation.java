package org.pl241.ra;

public class Allocation {
	public static enum Type{
		STACK,
		REGISTER
	};
	
	public Allocation.Type type;
	public int address ; // Reg number or spill offset
	public Allocation(Allocation.Type type, int address){
		this.type = type ;
		this.address = address ;
	}

	@Override
	public String toString() {
		return "Allocation:{" + type + ", " + address + "}";
	}
}
