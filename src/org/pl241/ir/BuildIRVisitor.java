package org.pl241.ir;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;









import org.pl241.frontend.ParseTreeNodeVisitor;
import org.pl241.frontend.Parser.AssignmentNode;
import org.pl241.frontend.Parser.DesignatorNode;
import org.pl241.frontend.Parser.ExpressionNode;
import org.pl241.frontend.Parser.FactorNode;
import org.pl241.frontend.Parser.FormalParamNode;
import org.pl241.frontend.Parser.FuncBodyNode;
import org.pl241.frontend.Parser.FuncCallNode;
import org.pl241.frontend.Parser.FuncDeclNode;
import org.pl241.frontend.Parser.IfStmtNode;
import org.pl241.frontend.Parser.NumberNode;
import org.pl241.frontend.Parser.ParseTreeNode;
import org.pl241.frontend.Parser.ProgramNode;
import org.pl241.frontend.Parser.RelationNode;
import org.pl241.frontend.Parser.ReturnStmtNode;
import org.pl241.frontend.Parser.StatSeqNode;
import org.pl241.frontend.Parser.StatementNode;
import org.pl241.frontend.Parser.TermNode;
import org.pl241.frontend.Parser.TerminalNode;
import org.pl241.frontend.Parser.TypeDeclNode;
import org.pl241.frontend.Parser.VarDeclNode;
import org.pl241.frontend.Parser.VarNode;
import org.pl241.frontend.Parser.WhileStmtNode;
import org.pl241.ir.IONode.IOType;
import org.pl241.ir.Variable.VariableType;

import sun.awt.image.OffScreenImage;

public class BuildIRVisitor implements ParseTreeNodeVisitor {


	private Stack<BasicBlock> bblStack; 
	private Stack<AbstractNode> expressionStack; 
	private Function currentFunction; 
	private Program program;
	private Function mainFunction;

	public BuildIRVisitor() {
		bblStack = new Stack<BasicBlock>();
		expressionStack = new Stack<AbstractNode>();
		program = new Program() ;
		mainFunction = new Function();
		mainFunction.name = "main";
	}

	public Program getProgram(){
		return program;
	}

	@Override
	public void enter(ProgramNode node) {
		// TODO Auto-generated method stub
		System.out.println("Entering program node");
		
			
		
	}

	@Override
	public void enter(VarNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enter(TypeDeclNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enter(NumberNode node) {
		// TODO Auto-generated method stub
		//System.out.printlnng number node");
	}

	@Override
	public void enter(VarDeclNode node) {
		// TODO Auto-generated method stub
		System.out.println("Entering vardecl node");
		if ( node.children.get(0).children.get(0).getText().equals("var") ){ //Variable
			System.out.println( node.children.get(0).getText());
			for( int i = 1 ;i < node.children.size() ; i+=2 ){
				System.out.println("Adding "+ node.children.get(i).getText());
				if( node.parent instanceof FuncBodyNode ){
					currentFunction.symbolTable.add( new Variable(node.children.get(i).getText(),VariableType.Integer) );	
				}
				else {
					mainFunction.symbolTable.add( new Variable(node.children.get(i).getText(),VariableType.Integer) );	
				}
			}
		} else { // Array

			ArrayList<Integer> dimensions = new ArrayList<Integer>();
			TypeDeclNode tctx = (TypeDeclNode) node.getChild(0) ;
			for( int i = 1 ;i < tctx.children.size() ; ++i ){
				if( tctx.getChild(i).getText().equals("[") ){
					dimensions.add( Integer.parseInt(tctx.getChild(i+1).getText() ) );
				}
			}
			for( int i = 1 ;i < node.children.size() ; i+=2){
				if( node.parent instanceof FuncBodyNode ){
					currentFunction.symbolTable.add( new Variable(node.children.get(i).getText(),VariableType.Array , dimensions) );
				}
				else {
					
					mainFunction.symbolTable.add( new Variable(node.children.get(i).getText(),VariableType.Array , dimensions) );    					
				}
			}
		}


	}

	@Override
	public void enter(FuncDeclNode node) {
		// TODO Auto-generated method stub
		System.out.println("Entering funcdecl node");
		currentFunction = new Function() ;
		currentFunction.name = node.children.get(0).getText();

	}

	@Override
	public void enter(StatSeqNode node) {
		
		
		if ( node.parent instanceof ProgramNode ){
			currentFunction = mainFunction; 
			
			
	        BasicBlock bbl =  new BasicBlock(currentFunction ,"mainbbl") ;
			bblStack.push(bbl);
		}

	}

	@Override
	public void enter(DesignatorNode node) {
		// TODO Auto-generated method stub
		//System.out.println("Entering designator node");

	}

	@Override
	public void enter(ExpressionNode node) {
		// TODO Auto-generated method stub
		//System.out.println("Entering expression node");

	}

	@Override
	public void enter(TermNode node) {
		// TODO Auto-generated method stub
		//System.out.println("Entering term node");

	}

	@Override
	public void enter(FactorNode node) {
		// TODO Auto-generated method stub
		//System.out.println("Entering factor node");

	}

	@Override
	public void enter(RelationNode node) {
		// TODO Auto-generated method stub
		//System.out.println("Entering relation node");

	}

	@Override
	public void enter(AssignmentNode node) {
		// TODO Auto-generated method stub
		//System.out.println("Entering assignment node");

	}

	@Override
	public void enter(WhileStmtNode node) {
		// TODO Auto-generated method stub
		System.out.println("Entering whilestmt node");
		BasicBlock bblOld = bblStack.peek();
		
		
		BasicBlock bblLoopBody =  new BasicBlock(currentFunction , "Loop body");
		BasicBlock bblAfterWhile = new BasicBlock(currentFunction , "after while");

		bblAfterWhile.successors.addAll( bblOld.successors ) ;
		bblOld.successors.clear();

		
		if( bblOld.getNodes().size() == 0 ){
			System.out.println("**********Top BBL is empty************");
			bblOld.taken = bblLoopBody;
			bblOld.fallThrough = bblAfterWhile;
			bblOld.successors.add(bblLoopBody);
			bblOld.successors.add(bblAfterWhile);
			
			bblLoopBody.successors.add(bblOld);
			
			
			BasicBlock top = bblStack.pop() ;
			bblStack.push( bblAfterWhile );
			bblStack.push( bblLoopBody );
			bblStack.push(top);
			
		}
		else {
			BasicBlock bblCheck = new BasicBlock(currentFunction , "while check");
			bblCheck.taken = bblLoopBody;
			bblCheck.fallThrough = bblAfterWhile;
			bblOld.successors.add(bblCheck);
			bblCheck.successors.add(bblLoopBody);
			bblCheck.successors.add(bblAfterWhile);
			bblLoopBody.successors.add(bblCheck);
			
			currentFunction.blocks.add( bblStack.peek() );
			bblStack.pop();


			
			

			bblStack.push( bblAfterWhile );
			bblStack.push( bblLoopBody );
			bblStack.push( bblCheck );
		}
		
		

		


		
	}

	@Override
	public void enter(IfStmtNode node) {
		// TODO Auto-generated method stub
		System.out.println("Entering ifstmt node");
		// Check if or if-else
		System.out.println(node.getText());
		// Compute relation and add it to current basicblock
		//bblStack.peek().nodes.add(new AbstractNode("Relation"));
		boolean lvalue = false;
		if( node.parent instanceof AssignmentNode ){
			lvalue = true;
		}
		if ( node.children.size() > 1 ){
			// Array
			ArrayList<Integer> strides = new ArrayList<Integer>();
			System.out.println("pushing 1");
			expressionStack.push(new LoadNode( node.getChild(0).getText()  ) ) ; // "load Array l:" + lvalue + ctx.getText() ));
		} else {
			// Var
			if( lvalue ){
				/// TODO pop stack here?
				System.out.println("pushing 2");
				expressionStack.push(new MoveNode( node.getChild(0).getText() ) );
			} else{
				System.out.println("pushing 3");
				expressionStack.push(new LoadNode( node.getChild(0).getText() ) ) ; // "load Var l:" + lvalue +  ctx.getText() ) );
			}
			
		}
		/// TODO add if condition
		BasicBlock bblOld = bblStack.peek();
		BasicBlock bblAfterIf = new BasicBlock(currentFunction ,"after if");
		bblAfterIf.successors.addAll( bblOld.successors ) ;
		bblOld.successors.clear();

		if ( node.children.size() == 5 ){  // If-Then
			// Taken node
			// New BBL
			BasicBlock bblThen = new BasicBlock(currentFunction ,"then");
			bblOld.successors.add(bblThen);
			bblOld.successors.add(bblAfterIf);
			bblThen.successors.add(bblAfterIf);



			bblStack.pop();


			bblOld.fallThrough = bblAfterIf ;
			bblOld.taken = bblThen;

			bblStack.push( bblAfterIf );
			bblStack.push( bblThen );
			bblStack.push(bblOld);
			// Fall-through node => Null	

		} else if ( node.children.size() == 7 ) { // If-Then-Else
			// Taken node
			BasicBlock bblThen = new BasicBlock(currentFunction ,"then");
			BasicBlock bblElse = new BasicBlock(currentFunction ,"else");
			bblOld.successors.add(bblThen);
			bblOld.successors.add(bblElse);

			bblStack.pop();


			bblStack.push( bblAfterIf );
			bblStack.push( bblElse );
			bblStack.push( bblThen );
			bblThen.successors.add(bblAfterIf);
			bblElse.successors.add(bblAfterIf);

			bblOld.fallThrough = bblElse ;
			bblOld.taken = bblThen;

			bblStack.push(bblOld);
			// Fall-through node => else
		}

		//currentFunction.blocks.add( bblOld );

	}

	@Override
	public void enter(ReturnStmtNode node) {
		// TODO Auto-generated method stub
		System.out.println("Entering returnstmt node");

	}

	@Override
	public void enter(FuncBodyNode node) {
		System.out.println("Entering funcbody node");
		// NEw bbl
		BasicBlock bbl =  new BasicBlock(currentFunction ,"func body") ;
		bblStack.push(bbl);

	}

	@Override
	public void enter(FormalParamNode node) {
		// TODO Auto-generated method stub
		System.out.println("Entering param node");

	}

	@Override
	public void enter(StatementNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exit(ProgramNode node) {
		// TODO Auto-generated method stub
			
		

	}

	@Override
	public void exit(VarNode node) {
		// TODO Auto-generated method stub
		//System.out.println("Exiting var node");

	}

	@Override
	public void exit(TypeDeclNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exit(NumberNode node) {
		// TODO Auto-generated method stub
		//System.out.println("Exiting number node");

	}

	@Override
	public void exit(VarDeclNode node) {
		// TODO Auto-generated method stub
		//System.out.println("Exiting vardecl node");


	}

	@Override
	public void exit(FuncDeclNode node) {
		program.addFunction(currentFunction);
		
	}

	@Override
	public void exit(StatSeqNode node) {
		System.out.println("Exiting Statseq node");
		if( node.parent instanceof WhileStmtNode ){
			bblStack.peek().addNode(new  BranchNode());
		}
		if( node.parent instanceof ProgramNode ){
			bblStack.peek().addNode(new  AbstractNode("end"));
		}
		currentFunction.blocks.add( bblStack.peek() );
		bblStack.pop();
		if( node.parent instanceof ProgramNode){
		System.out.println("Exiting program node");	
		program.addFunction(currentFunction);
		}
		
		

	}

	@Override
	public void exit(DesignatorNode node) throws Exception {
		

		boolean lvalue = false;
		if( node.parent instanceof AssignmentNode ){
			lvalue = true;
		}
		
		String varName = node.getChild(0).getText() ;
		Variable var = currentFunction.symbolTable.getVar(varName);
		if( var == null )
			var = mainFunction.symbolTable.getVar(varName);
		if( var == null )
			throw new Exception("Var " + varName + " not found in symbol table");
		if ( node.children.size() > 1 ){
			// Array
			
			// Caluclate offset
			int index = 0 ;
			AbstractNode offsetCalcNode = null ; 
			AbstractNode tempOffsetCalcNode = null ; 
			
			for(ParseTreeNode child : node.children){
				if( child instanceof ExpressionNode ){
					// Add expression
					bblStack.peek().addNode( expressionStack.peek() );
					// Manipulate it further
					int offset = 1 ;
					for( int i = 0 ; i < index ; ++i ){
						int innd = var.dimensions.size() - 1 - i  ;
						int here = 0 ;
						if( innd >= var.dimensions.size() ){
							 here = 1 ;
						}
						int there = here + 1 ;
						offset *= var.dimensions.get(innd);
					}
					tempOffsetCalcNode = offsetCalcNode ;
					if( offset > 1){
						offsetCalcNode = new AbstractNode(expressionStack.peek().label, Integer.toString(offset) , "MUL" ) ;
						bblStack.peek().addNode( offsetCalcNode );
					}else {
						offsetCalcNode =  expressionStack.peek();
					}
					System.out.println("popping xx" );
					expressionStack.pop();
					
					if( tempOffsetCalcNode != null )
					{
						offsetCalcNode = new AbstractNode(tempOffsetCalcNode.label, offsetCalcNode.label , "ADD" ) ;
						bblStack.peek().addNode( offsetCalcNode );
					}
					
					++index ;
				}
			}
			
			
			AbstractNode calcAddress = new AbstractNode(varName, offsetCalcNode.label , "ADDA" ) ;
			bblStack.peek().addNode( calcAddress );
			if( lvalue ){
				System.out.println("pushing xx" );
				expressionStack.push(new StoreNode(calcAddress.label  ) );
			
			} else{
				System.out.println("pushing yy" );
				expressionStack.push(new LoadNode( calcAddress.label ) ) ; 
			}
			
		} else {
			// Var
			if( lvalue ){
				System.out.println("pushing yy1" );
				expressionStack.push(new StoreNode(varName) );
				
			} else{
				System.out.println("pushing yy2" );
				expressionStack.push(new LoadNode( varName ) ) ; 
			}
			
		}

	}

	@Override
	public void exit(ExpressionNode node) {
		// Push an expression to expression stack
		// Parent should pop them and insert proper ir code
			if( node.children.size() > 2 ){
				ListIterator<ParseTreeNode> li =  node.children.listIterator(node.children.size());
				li.previous();
				ParseTreeNode operator = li.previous();
				if( expressionStack.peek() instanceof BranchNode == false )	
					bblStack.peek().addNode( expressionStack.peek() );

				String label1 = expressionStack.pop().label ;
				System.out.println("popping a node " + label1 );
				if( expressionStack.peek() instanceof BranchNode == false )
					bblStack.peek().addNode( expressionStack.peek() );
				
				String label2 = expressionStack.pop().label ;
				System.out.println("popping a node " + label2 );
				
				
				
				String operatorText = operator.getText() ;
				AbstractNode anode = new AbstractNode(label1, label2 ,  AbstractNode.operatorMap.get(operatorText) );
				System.out.println("pushing an operator node " + operator.getText() );
				expressionStack.push(anode);
				
				li.previous();
				while( li.hasPrevious() ){
					operator = li.previous();
					li.previous();
					if( expressionStack.peek() instanceof BranchNode == false )
						bblStack.peek().addNode( expressionStack.peek() );
					String label = expressionStack.pop().label ;
					System.out.println("popping a node " + label );
					String operatorTexts = operator.getText() ;
					
					if( expressionStack.peek() instanceof BranchNode == false )
						bblStack.peek().addNode( expressionStack.peek() );
					String op1Label = expressionStack.pop().label ;
					System.out.println("popping a node " + op1Label );
					
					
					anode = new AbstractNode( op1Label , label ,  AbstractNode.operatorMap.get(operatorTexts) );
					System.out.println("pushing an operator node " + operator.getText() );
					expressionStack.push(anode);
				}
				
			}else{
				//System.out.println("HERERERERERERE");
			}

			
	//	}
		
		
	}

	@Override
	public void exit(TermNode node) {
		
		if( node.children.size() > 2 ){  // Process two or more factors
			ListIterator<ParseTreeNode> li =  node.children.listIterator(node.children.size());
			li.previous(); //term already on stack
			ParseTreeNode operator = li.previous();
			if( expressionStack.peek() instanceof BranchNode == false )
				bblStack.peek().addNode( expressionStack.peek() );
			System.out.println("poping4");
			String label1 = expressionStack.pop().label ;
			
			if( expressionStack.peek() instanceof BranchNode == false )
				bblStack.peek().addNode( expressionStack.peek() );
			System.out.println("poping5");
			String label2 = expressionStack.pop().label ;
			
			AbstractNode anode = new AbstractNode(label1, label2 ,  AbstractNode.operatorMap.get(operator.getText()) );
			System.out.println("pushing an operator " + operator.getText() );
			expressionStack.push(anode);
			li.previous();
			while( li.hasPrevious() ){
				operator =li.previous();
				li.previous();
				if( expressionStack.peek() instanceof BranchNode == false )
					bblStack.peek().addNode( expressionStack.peek() );
				System.out.println("poping6");
				String label = expressionStack.pop().label ;
				
				
				if( expressionStack.peek() instanceof BranchNode == false )
					bblStack.peek().addNode( expressionStack.peek() );
				String op1Label = expressionStack.pop().label ;
				System.out.println("popping a node " + op1Label );
				
				
				
				anode = new AbstractNode( op1Label, label ,   AbstractNode.operatorMap.get( operator.getText() ) );
				System.out.println("pushing an operator " + operator.getText() );
				expressionStack.push(anode);
			}	
			
		}

	}

	@Override
	public void exit(FactorNode node) {
		if ( node.children.size() == 1 
				&& node.children.get(0) instanceof TerminalNode
				)
		{
			
			System.out.println("pushing an immediate " + node.children.get(0).getText() );
			expressionStack.push( new ImmediateNode(node.children.get(0).getText()));
		}
		
		
	}

	@Override
	public void exit(RelationNode node) {
		bblStack.peek().addNode( expressionStack.peek() );
		System.out.println("poping10");
		String label1 = expressionStack.pop().label ;
		
		bblStack.peek().addNode( expressionStack.peek() );
		System.out.println("poping10");
		String label2 = expressionStack.pop().label ;
		
		AbstractNode nNode = new AbstractNode(label1, label2 , "cmp" );
		bblStack.peek().addNode( nNode );
		bblStack.peek().addNode( new BranchNode( AbstractNode.operatorMap.get(node.children.get(1).getText() )  , nNode.label)  );
		
    	// TODO Auto-generated method stub
    	//if ( ctx.parent instanceof WhilestatementContext ){
    		currentFunction.blocks.add( bblStack.peek() );
    		bblStack.pop();
    	//}

	}

	@Override
	public void exit(AssignmentNode node) {
		
		System.out.println("Exiting Assignment node");
		//bblStack.peek().addNode( expressionStack.peek() );
		//System.out.println("poping");
		
		AbstractNode exp = expressionStack.pop() ;
		AbstractNode des = expressionStack.pop() ;
		
		System.out.println("popping des " + des );
		System.out.println("pushing exp " + exp );
		
		
		bblStack.peek().addNode( exp ) ;
		((StoreNode)des).setValue( exp.label );
		bblStack.peek().addNode( des ) ;
		//bblStack.peek().addNode( new AbstractNode( des.label , exp.label , des.operator ) );
		
		//System.out.println(des.label + exp.label);
		
		//bblStack.peek().addNode( expressionStack.peek() );
		//AbstractNode anode = expressionStack.peek() ;
	
		//try{
		//((MoveNode) anode).setRightOperand(label1) ;
	//	}
	//	catch(Exception e){
	//		System.out.println("here");
	//	}

	}

	@Override
	public void exit(WhileStmtNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exit(IfStmtNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exit(ReturnStmtNode node) {
		//Pop ExpStack
		if( node.children.size() > 1 ) {
			System.out.println("================poping ret" );
			
			AbstractNode exp = expressionStack.pop() ;
			
			//System.out.println("popping des " + des );
			//System.out.println("pushing exp " + exp );
			
			
			bblStack.peek().addNode( exp ) ;
			//((StoreNode)des).setValue( exp.label );
			//bblStack.peek().addNode( des ) ;
			
			ReturnNode retNode = new ReturnNode(exp.label);
			bblStack.peek().addNode(retNode);
			
			
		} else {
			System.out.println("===============Not poping ret" );
		}

	}

	@Override
	public void exit(FuncBodyNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exit(FormalParamNode node) {
		// TODO Auto-generated method stub
		System.out.println("HERE");
		if( node.children.size() > 2 ){
			for( int i = 1 ; i < node.children.size(); i+=2){
				currentFunction.symbolTable.add( new Variable( node.children.get(i).getText() , VariableType.Integer ) );
			}
		}
	}

	@Override
	public void exit(StatementNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enter(FuncCallNode node) {
	}

	@Override
	public void exit(FuncCallNode node) {
		System.out.println("Exiting Func Call node");

		
		// Handle Args( as Expressions on the Stack )
		// Pop per each argument
		for(ParseTreeNode child:node.children ){
			if( child instanceof ExpressionNode ){
				System.out.println("poping seri" );
				AbstractNode aanode =  expressionStack.pop();
				bblStack.peek().addNode(aanode);
			}
		}
		
		
		
		String functionName = node.getChild(1).getText() ;
		boolean noCall = functionName.equals("InputNum") || functionName.equals("OutputNum") || functionName.equals("OutputNewLine");
		
		
		
		AbstractNode anode  = null ;
		if( !noCall ){
			
			
			
			
			
			// Split the Current Basic Block
			BasicBlock bblAfterCall = new BasicBlock(currentFunction ,"After Call");
			bblAfterCall.successors.addAll(bblStack.peek().successors);
			bblStack.peek().successors.clear();
			bblStack.peek().successors.add(bblAfterCall);
			BasicBlock bblOld = bblStack.pop();
			bblStack.push(bblAfterCall);
			bblStack.push(bblOld);
			
			
			
			// Add a Branch to Function address
			
			anode = new BranchNode() ;
			((BranchNode)anode).isCall = true ;
			((BranchNode)anode).jumpTarget = functionName;
			
			
			
			bblStack.peek().addNode( anode );
			currentFunction.blocks.add( bblStack.peek() );
			bblStack.pop();
			
		}
		else {
			// Special DLX Instructions
		
			if( functionName.equals("InputNum") ){
				anode = new IONode(IOType.READ,null);
			} else if ( functionName.equals("OutputNum") ){
				anode = new IONode(IOType.WRITE,node.getChild(3).getText());
			} else if ( functionName.equals("OutputNewLine") ){
				anode = new IONode(IOType.WRITELINE,null);
			}
			bblStack.peek().addNode( anode );
		}
		
		
		if( node.parent instanceof FactorNode 
				){ 
			System.out.println("pushing fet");
			expressionStack.push(anode); 
		} 

		
		
		

	}

}
