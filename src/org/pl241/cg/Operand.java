package org.pl241.cg;

public class Operand {

    public Operand(Instruction.OperandType type, Integer value) {
        this.type = type;
        this.value = value;
    }

    public Instruction.OperandType type;
    public Integer value;

    @Override
    public String toString() {
        return type + "." + value;
    }
}
