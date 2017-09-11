package org.pl241.cg;

import org.pl241.ra.Allocation;

import static org.pl241.cg.DLXCodeGenerator.SCRATCH_REGISTER;

public class Operand {
    enum Type {
        REGISTER,
        IMMEDIATE;

        @Override
        public String toString() {
            switch(this) {
                case REGISTER: return "R";
                case IMMEDIATE: return "I";
                default: throw new IllegalArgumentException();
            }
        }
    }

    public Operand(Operand.Type type, Integer value) {
        this.type = type;
        this.value = value;
    }

    public static Operand fromAllocation(Allocation allocation) {
        assert (allocation.type != Allocation.Type.STACK): "Invalide operand type";

        if (allocation.type == Allocation.Type.GENERAL_REGISTER)
            return new Operand(Type.REGISTER, allocation.address);
        else if (allocation.type == Allocation.Type.SCRATCH_REGISTER)
            return new Operand(Type.REGISTER, SCRATCH_REGISTER);

        return null;
    }

    public Operand.Type type;
    public Integer value;

    @Override
    public String toString() {
        return type + "." + value;
    }
}
