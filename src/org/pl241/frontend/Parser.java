package org.pl241.frontend;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Parser {
	private LinkedList<Token> tokens;
	private Token lookahead;

	public ParseTreeNode parse(List<Token> tokens) throws Exception
	{
		this.tokens = new LinkedList<>();
		this.tokens.addAll(tokens);
		lookahead = this.tokens.getFirst();
		ParseTreeNode root = pl241();

		if (lookahead.token != Token.EPSILON){
			StringBuilder ret = new StringBuilder();
			while( this.tokens.size() > 0 )
				ret.append(" ").append(nextToken().sequence);
			throw new Exception(  "Unexpected symbol " + lookahead.sequence + " not found " + ret);
		}
		return root;
	}

	private ParseTreeNode pl241() throws Exception{
		ProgramNode node = new ProgramNode(null, "");
		// pl241 : MAIN (varDecl)* (funcDecl)* '{' statSequence '}' '.' ;
		if (lookahead.token == Token.MAIN)
		{
			node.addChild( new TerminalNode(node,  nextToken().sequence )); // Main
			
			List<ParseTreeNode> vars = varDeclStar(node) ;
			node.addChildren(  vars );
			node.addChildren( funcDeclStar(node) );
			node.addChild( new TerminalNode(node,  nextToken().sequence )); // "{"
			node.addChild( statSequence(node) );
			node.addChild( new TerminalNode(node,  nextToken().sequence )); // "}"
			node.addChild( new TerminalNode(node,  nextToken().sequence ));// "."\

		}
		return node;
	}

	private List<ParseTreeNode> varDeclStar(ParseTreeNode parent){
		// varDecl : typeDecl IDENT  ( ',' IDENT )* ';' ;
		List<ParseTreeNode> nodes = new ArrayList<>();
		if (lookahead.token == Token.VAR || lookahead.token == Token.ARRAY )
		{
			VarDeclNode node = new VarDeclNode(parent, "") ;
			node.addChild( typeDecl(node) ); 
			
			node.addChild( new TerminalNode(parent,  nextToken().sequence )); // id
			
			node.addChildren( identStar(node,"") );
			node.addChild( new TerminalNode(parent,  nextToken().sequence )); // ";"
			nodes.add(node);
			nodes.addAll( varDeclStar(parent) );
		}
		return nodes ;
	}

	private List<ParseTreeNode> identStar(ParseTreeNode parent , String type){
		List<ParseTreeNode> nodes = new ArrayList<>();
		if (lookahead.token == Token.COMMA){
			nodes.add( new TerminalNode(parent,  nextToken().sequence ));
			nodes.add( new TerminalNode(parent,  nextToken().sequence ));
			nodes.addAll(identStar(parent,type));
		} 
		return nodes ;
	}

	private ParseTreeNode typeDecl(ParseTreeNode parent){
		// typeDecl : VAR | ( 'array' '[' NUMBER ']' ( '[' NUMBER ']' )*  );
		TypeDeclNode node = new TypeDeclNode(parent,"");
		if (lookahead.token == Token.VAR )
		{
			node.addChild( new TerminalNode(node,  nextToken().sequence ));
		} else if (lookahead.token == Token.ARRAY ){
			node.addChild( new TerminalNode(node,  nextToken().sequence )); // array
			node.addChild( new TerminalNode(node,  nextToken().sequence )); //[
			node.addChild( new NumberNode(node,  nextToken().sequence )); // Number
			node.addChild( new TerminalNode(node,  nextToken().sequence )); //]
			node.addChildren( arrayDimStar(node) );
			
		}
		return node ;
	}
	
	private List<ParseTreeNode> arrayDimStar( ParseTreeNode parent){
		
		List<ParseTreeNode> nodes = new ArrayList<>();
		if( lookahead.token == Token.OPEN_BRACKET ) {
			nodes.add( new TerminalNode(parent,  nextToken().sequence )); //[
			nodes.add( new NumberNode(parent,  nextToken().sequence )); // Number
			nodes.add( new TerminalNode(parent,  nextToken().sequence )); //]
			nodes.addAll( arrayDimStar(parent) );
		}
		return nodes;
	}

	private List<ParseTreeNode> funcDeclStar(ParseTreeNode parent) throws Exception{
		List<ParseTreeNode> nodes = new ArrayList<>();
		
		if (lookahead.token == Token.FUNCTION || lookahead.token == Token.PROCEDURE ){
			//FuncDeclNode node = new FuncDeclNode(parent, "Func Decl"));
			nodes.add( new TerminalNode(parent,  nextToken().sequence )); // func/procedure
			
			FuncDeclNode node = new FuncDeclNode(parent,"");
			node.addChild( new TerminalNode(node,  nextToken().sequence )); //func name			
			node.addChild(formalParam(node));
			node.addChild( new TerminalNode(node,  nextToken().sequence )); //semicolon
			node.addChild( funcBody(node) );
			node.addChild( new TerminalNode(node,  nextToken().sequence ));// semicolon
			nodes.add( node ) ;
			nodes.addAll( funcDeclStar(parent) );
		}
		return nodes ;
	}

	private ParseTreeNode formalParam(ParseTreeNode parent){
		FormalParamNode node = new FormalParamNode(parent,"");
		if ( lookahead.token == Token.OPEN_P ){
			
			node.addChild( new TerminalNode(node,  nextToken().sequence ));// "("
			if (lookahead.token != Token.CLOSE_P && lookahead.token != Token.COMMA)
			{
				node.addChild( new TerminalNode(node,  nextToken().sequence ));// Arg0
				
			}
			node.addChildren( identStar(node, "Arg") );
			node.addChild( new TerminalNode(node,  nextToken().sequence )); // ")"
		}
		return node ;
	}

	private ParseTreeNode funcBody(ParseTreeNode parent) throws Exception{
		FuncBodyNode node = new FuncBodyNode(parent,"");
		node.addChildren( varDeclStar(node) );
		node.addChild( new TerminalNode(node,  nextToken().sequence )); // "{" 
		node.addChild( statSequence(node) );
		node.addChild( new TerminalNode(node,  nextToken().sequence )); // "}"
		return node;
	}

	private ParseTreeNode statSequence(ParseTreeNode parent) throws Exception{
		StatSeqNode node = new StatSeqNode(parent,"");
		node.addChild( statement(node) );
		node.addChildren( statementStar(node) );
		return node;
	}

	private List<ParseTreeNode> statementStar(ParseTreeNode parent) throws Exception{
		List<ParseTreeNode> nodes = new ArrayList<>();
		if( lookahead.token == Token.SEMICOLON ){
			nodes.add( new TerminalNode(parent,  nextToken().sequence ));//";"
			nodes.add( statement(parent) );
			nodes.addAll( statementStar(parent) );
			
		}
		return nodes;
	}
	
	private ParseTreeNode statement(ParseTreeNode parent) throws Exception{
		StatementNode node = new StatementNode(parent, "");
		if( lookahead.token == Token.LET ){
			AssignmentNode anode = new AssignmentNode(node, "");
			anode.addChild( new TerminalNode(anode,  nextToken().sequence )); // Let
			anode.addChild( designator(anode) );
			anode.addChild( new TerminalNode(anode,  nextToken().sequence )); // Gets
			anode.addChild( expression(anode) );
			node.addChild(anode);
			return node ;
			
		} else if( lookahead.token == Token.CALL ){
			node.addChild(funcCall(node));
			return node;
			
		} else if( lookahead.token == Token.IF ){
			IfStmtNode inode = new IfStmtNode(node,"");
			inode.addChild( new TerminalNode(inode,  nextToken().sequence )); // "if"
			inode.addChild(  relation(inode) );
			inode.addChild( new TerminalNode(inode,  nextToken().sequence )); // "then"
			inode.addChild( statSequence(inode) );
			if( lookahead.token == Token.ELSE ){
				inode.addChild( new TerminalNode(inode,  nextToken().sequence )); // "else"
				inode.addChild( statSequence(inode) );
			}
			inode.addChild( new TerminalNode(inode,  nextToken().sequence )); // "fi"
			node.addChild(inode);
			return node;
			
		} else if (lookahead.token == Token.WHILE ){
			WhileStmtNode wnode = new WhileStmtNode(node , "");
			wnode.addChild( new TerminalNode(wnode,  nextToken().sequence ));// while
			wnode.addChild( relation(wnode) );
			wnode.addChild( new TerminalNode(wnode,  nextToken().sequence )); // do
			wnode.addChild( statSequence(wnode) );
			wnode.addChild( new TerminalNode(wnode,  nextToken().sequence ));// od
			node.addChild(wnode);
			return wnode;
		} else if ( lookahead.token == Token.RETURN ){
			ReturnStmtNode rnode = new ReturnStmtNode(node, "") ;
			rnode.addChild( new TerminalNode(rnode,  nextToken().sequence ));// Return
			
			if( lookahead.token == Token.VARIABLE ||
				lookahead.token == Token.NUMBER ||
				lookahead.token == Token.OPEN_P ||
				lookahead.token == Token.CALL ){
				rnode.addChild( expression(rnode) );
			}
			node.addChild(rnode);
			return node;
		}
		else {
			throw new Exception("Unexpected token " + lookahead.token);
		}
	}
	
	private ParseTreeNode relation(ParseTreeNode parent){
		RelationNode node = new RelationNode(parent,"");
		node.addChild( expression(node) );
		node.addChild( new TerminalNode(node,  nextToken().sequence )); // Rel-op
		node.addChild( expression(node) );
		return node ;
	}
	
	private ParseTreeNode expression(ParseTreeNode parent){
		ExpressionNode node = new ExpressionNode(parent,"");
		node.addChild( term(node) );
		node.addChildren(termStar(node));
		return node ;
	}
	
	private ParseTreeNode term(ParseTreeNode parent){
		TermNode node = new TermNode(parent,"");
		node.addChild( factor(node) );
		node.addChildren( factorStar(node) );
		return node ;
	}

	private List<ParseTreeNode> termStar(ParseTreeNode parent){
		List<ParseTreeNode> nodes = new ArrayList<>();
		if( lookahead.token == Token.PLUS_OR_MINUS ){
			nodes.add( new TerminalNode(parent,  nextToken().sequence )); // "+" or "-"
			nodes.add( term(parent) );
			nodes.addAll( termStar(parent) );
		}
		return nodes;
	}
	
	private ParseTreeNode factor(ParseTreeNode parent){
		FactorNode node = new FactorNode(parent, "");
		if( lookahead.token == Token.VARIABLE ){
			node.addChild( designator(node) );
		} else if( lookahead.token == Token.NUMBER){
			 node.addChild( new TerminalNode(node,  nextToken().sequence )); //Number
		} else if(lookahead.token == Token.OPEN_P ){
			node.addChild( new TerminalNode(node,  nextToken().sequence )); // "("
			node.addChild( expression(node) );
			node.addChild( new TerminalNode(node,  nextToken().sequence ));// ")"
		} else if( lookahead.token == Token.CALL ){
			node.addChild( funcCall(node));
		}
		return node;
	}
	
	private List<ParseTreeNode> factorStar(ParseTreeNode parent){
		List<ParseTreeNode> nodes = new ArrayList<>();
		if( lookahead.token == Token.MULT_OR_DIV ){
			
			nodes.add( new TerminalNode(parent,  nextToken().sequence )); // "*" or "/"
			
			nodes.add( factor(parent) );
			nodes.addAll( factorStar(parent) );
			
		}
		return nodes;
	}
	
	private ParseTreeNode funcCall(ParseTreeNode parent){
		FuncCallNode node = new FuncCallNode(parent, "");
		if( lookahead.token == Token.CALL ){
			node.addChild ( new TerminalNode(node, nextToken().sequence )); // call
			node.addChild ( new TerminalNode(node, nextToken().sequence )); // name
			if( lookahead.token == Token.OPEN_P ){
				node.addChild( new TerminalNode(node,  nextToken().sequence ));// "("
				if( lookahead.token != Token.CLOSE_P )
					node.addChild(  expression(node) ); 
				node.addChildren( argExpressionStar(node) );
				node.addChild( new TerminalNode(node,  nextToken().sequence )); // ")"
			}
			
		}
		return node ;
	}
	
	private ParseTreeNode designator(ParseTreeNode parent){
		DesignatorNode node = new DesignatorNode(parent, "");
		node.addChild(new TerminalNode(node, nextToken().sequence)); // identifier
		
		node.addChildren( expressionStar(node) );
		return node;
	}
	
	private List<ParseTreeNode> expressionStar(ParseTreeNode parent){
		List<ParseTreeNode> nodes = new ArrayList<>();
		if( lookahead.token == Token.OPEN_BRACKET ){
			
			nodes.add( new TerminalNode(parent,  nextToken().sequence )); // [
			nodes.add( expression(parent) );
			nodes.add( new TerminalNode(parent,  nextToken().sequence )); // ]
			nodes.addAll( expressionStar(parent) );
			
		}
		return nodes ;
	}
	
	private List<ParseTreeNode> argExpressionStar(ParseTreeNode parent) {
		List<ParseTreeNode> nodes = new ArrayList<>();
		if( lookahead.token == Token.COMMA ){
			nodes.add(new TerminalNode(parent, nextToken().sequence)); // Comma
			nodes.add(expression(parent));
			//nodes.add( new TerminalNode(parent,  nextToken().sequence )); // ]
			nodes.addAll( argExpressionStar(parent) );
		}
		return nodes ;
	}
	
	private Token nextToken()
	{
		Token top = tokens.pop();
		// at the end of input we return an epsilon token
		if (tokens.isEmpty())
			lookahead = new Token(Token.EPSILON, "" );
		else
			lookahead = tokens.getFirst();
		return top;
	}


	public abstract class ParseTreeNode  {
		
		public ParseTreeNode parent;
		public List<ParseTreeNode> children;
		private String text ;
		@Override
		public String toString(){
			return this.getClass().getName();
		}
		
		public ParseTreeNode getChild(int index){
			return children.get(index);
		}
		
		ParseTreeNode(ParseTreeNode parent, String text) {
			children = new ArrayList<>();
			this.text = text ;
			this.parent = parent;
		}
		void addChild(ParseTreeNode node){
			children.add(node);
		}
		void addChildren(List<ParseTreeNode> nodes){
			children.addAll(nodes);
		}
		public void setParent(ParseTreeNode parent){
			this.parent = parent;
		}
		public ParseTreeNode getParent(){
			return parent;
		}
		List<ParseTreeNode> getChildren(){
			return children;
		}
		public String getText(){
			return text ;
		}
		public abstract void accept(ParseTreeNodeVisitor visitor);
		
	}

	public  class ProgramNode extends ParseTreeNode {

		ProgramNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
		
	public  class VarNode extends ParseTreeNode {

		public VarNode(ParseTreeNode parent, String text) {
			super(parent, text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class TypeDeclNode extends ParseTreeNode {

		TypeDeclNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class NumberNode extends ParseTreeNode {
		NumberNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}

	public  class VarDeclNode extends ParseTreeNode {

		VarDeclNode(ParseTreeNode parent, String text) {
			super(parent, text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class FuncDeclNode extends ParseTreeNode {

		FuncDeclNode(ParseTreeNode parent, String text) {
			super(parent, text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class StatSeqNode extends ParseTreeNode {

		StatSeqNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class DesignatorNode extends ParseTreeNode {

		DesignatorNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			try {
				visitor.exit(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public  class ExpressionNode extends ParseTreeNode {
		ExpressionNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}
		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class TermNode extends ParseTreeNode {

		TermNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class FactorNode extends ParseTreeNode {

		FactorNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class RelationNode extends ParseTreeNode {

		RelationNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class AssignmentNode extends ParseTreeNode {

		AssignmentNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class FuncCallNode extends ParseTreeNode {

		FuncCallNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class WhileStmtNode extends ParseTreeNode {

		WhileStmtNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}
	
		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class IfStmtNode extends ParseTreeNode {

		IfStmtNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public class ReturnStmtNode extends ParseTreeNode {
		ReturnStmtNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}

	public  class FuncBodyNode extends ParseTreeNode {

		FuncBodyNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class FormalParamNode extends ParseTreeNode {

		FormalParamNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}

		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class StatementNode extends ParseTreeNode {

		StatementNode(ParseTreeNode parent, String text) {
			super(parent,text);
		}
		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
			visitor.enter(this);
			for(ParseTreeNode node:this.getChildren()){
				node.accept(visitor);
			}
			visitor.exit(this);
		}
	}
	
	public  class TerminalNode extends ParseTreeNode {
		TerminalNode(ParseTreeNode parent, String text){
			super(parent, text);
		}
		
		@Override
		public void accept(ParseTreeNodeVisitor visitor) {
		}
	}
}
