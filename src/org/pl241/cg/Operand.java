package org.pl241.cg;

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

    public Operand.Type type;
    public Integer value;

    @Override
    public String toString() {
        return type + "." + value;
    }
}
