package org.pl241.optimization;

public class Expression {

	public Expression(Object _op1, Object _op2, String _operator){
		op1 = _op1;
		op2 = _op2;
		operator = _operator;
	}
	
	@Override
	public boolean equals(Object obj) {
		if ( this == obj )
			return true;
		if ( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		Expression other = (Expression)obj;
		if (other.operator.equals(operator) &&
                ((other.op1.equals(op1) && other.op2.equals(op2)) ||
                        (other.op2.equals(op1) && other.op1.equals(op2)))) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 3;
		hash = 7 * hash + this.operator.hashCode();
		return hash;
	}

    private Object op1;
    private Object op2;
    private String operator;
}
