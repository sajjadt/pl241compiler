package org.pl241.optimization;

import org.pl241.ir.ArithmeticNode;
import org.pl241.ir.PhiFunctionNode;

public class Expression {

    public static Expression fromPhi(PhiFunctionNode node) {
        Object[] operands = node.rightOperands.values().toArray();
        return new Expression(operands[0], operands[1], ExpressionType.PHI);
    }

    public static ExpressionType fromArithExpressions(ArithmeticNode.Type type) {
        switch (type) {
            case ADD:
                return ExpressionType.ADD;
            case SUB:
                return ExpressionType.SUB;
            case MUL:
                return ExpressionType.MUL;
            case DIV:
                return ExpressionType.DIV;
            case CMP:
                return ExpressionType.CMP;
            default:
                return ExpressionType.NONE;
        }
    }

    public enum ExpressionType {
        PHI,
        ADD,
        SUB,
        MUL,
        DIV,
        CMP,
        NONE;


        public boolean isSymmetric() {
            return this.equals(ADD) || this.equals(MUL);
        }
    }


	public Expression(Object operand1, Object operand2, ExpressionType operator){
		this.operand1 = operand1;
		this.operand2 = operand2;
		this.operator = operator;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Expression other = (Expression)obj;
		if (other.operator.equals(operator)) {

		    if (operator.isSymmetric()) {
                return (other.operand1.equals(operand1) && other.operand2.equals(operand2)) ||
                        (other.operand1.equals(operand2) && other.operand2.equals(operand1));

            } else {
		        return other.operand1.equals(operand1) &&
                        other.operand2.equals(operand2);
            }

		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 7 * hash + this.operator.hashCode();
		return hash;
	}

    private Object operand1;
    private Object operand2;
    private ExpressionType operator;
}
