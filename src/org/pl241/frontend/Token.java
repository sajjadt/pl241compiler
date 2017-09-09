package org.pl241.frontend;

class Token {
	public static final int EPSILON = 0;

	public static final int MAIN = 1;
	public static final int LET = 2;
	public static final int VAR = 3;
	public static final int INPUT_NUM = 4;
	public static final int OUTPUT_NUM = 5;
	public static final int CALL = 6;
	public static final int IF = 7;
	public static final int THEN = 9;
	public static final int ELSE = 10;
	public static final int DO = 11;
	public static final int WHILE = 12;
	public static final int OD = 13;
	public static final int RETURN = 14;

	public static final int EQ = 15;
	public static final int NEQ = 16;
	public static final int LESS = 17;
	public static final int LEQ = 18;
	public static final int GREATER = 19;
	public static final int GEQ = 20;

	public static final int OPEN_P = 21;
	public static final int CLOSE_P = 22;

	public static final int OPEN_BRACE = 23;
	public static final int CLOSE_BRACE = 24;

	public static final int OPEN_BRACKET = 25;
	public static final int CLOSE_BRACKET = 26;

	public static final int NUMBER = 28;
	public static final int VARIABLE = 29;

	public static final int ARRAY = 30;
	public static final int SEMICOLON = 31;
	public static final int GETS = 32;

	public static final int COMMA = 33;

	public static final int PLUS_OR_MINUS = 35;
	public static final int MULT_OR_DIV = 36;
	
	public static final int FUNCTION = 37 ;
	public static final int PROCEDURE = 38 ;
	
	public static final int OUTPUT_NEW_LINE = 39;
	public static final int DOT = 40;
	
	public static final int SHARP = 41;
	public static final int FI = 42;

	public final int token;
	public final String sequence;

	public Token(int token, String sequence) {
		super();
		this.token = token;
		this.sequence = sequence;
	}
}