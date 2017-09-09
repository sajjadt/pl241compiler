package org.pl241.frontend;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tokenizer {
	
	private LinkedList<TokenInfo> tokenInfos;
	private LinkedList<Token> tokens;
	private void registerTokens(){
		// Keywords
		this.add("\\,", Token.COMMA); // ==
		this.add("#", Token.SHARP); // ==
		this.add("\\.", Token.DOT); // <-
		this.add("<-", Token.GETS); // <-
		this.add("main", Token.MAIN); // main
		this.add("let", Token.LET); // let
		this.add("var", Token.VAR); // var
		this.add("InputNum", Token.INPUT_NUM); // InputNum
		this.add("OutputNum", Token.OUTPUT_NUM); // OutputNum
		this.add("OutputNewLine", Token.OUTPUT_NEW_LINE); // OutputNum
		// Condition keywords
		this.add("array", Token.ARRAY); 
		this.add("call", Token.CALL); 
		this.add("if", Token.IF); 
		this.add("fi", Token.FI); 
		this.add("then", Token.THEN); 
		this.add("else", Token.ELSE);  
		this.add("do", Token.DO);
		this.add("while", Token.WHILE);
		this.add("od", Token.OD); 
		this.add("return", Token.RETURN); 
		this.add("procedure", Token.PROCEDURE);
		this.add("function" , Token.FUNCTION) ;
		// Conditional operators
		this.add("==", Token.EQ); // ==
		this.add("!=", Token.NEQ); // !=
		this.add("<=", Token.LEQ); // !=
		this.add(">=", Token.GEQ); // !=
		this.add("<", Token.LESS); // ==
		this.add(">", Token.GREATER); // ==

		this.add("\\(", Token.OPEN_P); // open 
		this.add("\\)", Token.CLOSE_P); // close
		this.add("\\[", Token.OPEN_BRACKET); // open bracket
		this.add("\\]", Token.CLOSE_BRACKET); // close bracket
		this.add("\\{", Token.OPEN_BRACE); // open brace
		this.add("\\}", Token.CLOSE_BRACE); // close brace
		
		this.add("[+-]", Token.PLUS_OR_MINUS); // plus or minus
		this.add("[*/]", Token.MULT_OR_DIV); // mult or divide
		
		this.add("[0-9]+",Token.NUMBER); // integer number
		this.add("[a-zA-Z][a-zA-Z0-9_]*", Token.VARIABLE); // variable

		this.add("\\;", Token.SEMICOLON); // ;
	}
	public Tokenizer() {
	  tokenInfos = new LinkedList<>();
	  tokens = new LinkedList<>();
	  this.registerTokens();
	}
	
	private void add(String regex, int token) {
		  tokenInfos.add(
		  new TokenInfo(
		  Pattern.compile("^("+regex+")"), token));
	}
	
	public LinkedList<Token> getTokens() {
		  return tokens;
	}
	
	public void tokenize(String str) throws Exception {
		  String s = str;
		  tokens.clear();
		  while (!s.equals("")) {
			  boolean match = false;
			  for (TokenInfo info : tokenInfos) {
			      Matcher m = info.regex.matcher(s);
			      if (m.find()) {
			        match = true;

			        String tok = m.group().trim();
			        if( tok.equals("#")) // Remove comment
			        {
			        	String newline = "\n";
			        	int newLineIndex = s.indexOf(newline);
			        	s = s.substring(newLineIndex+newline.length());
			        	s = s.trim();
			        } else {
			        	tokens.add(new Token(info.token, tok));
			        	s = m.replaceFirst("");
			        	s = s.trim();
			        }
			        break;
			      }
			  }
			  if (!match) throw new Exception(
					"Unexpected character in input: "+s);
		  }
	}
	  private class TokenInfo {
	    final Pattern regex;
	    final int token;
	
	    TokenInfo(Pattern regex, int token) {
	      super();
	      this.regex = regex;
	      this.token = token;
	    }
	  }
	  
	  
}
