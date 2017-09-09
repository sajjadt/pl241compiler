package org.pl241.ir;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Stack;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.pl241.Program;
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

public class IRBuilderVisitor implements ParseTreeNodeVisitor {

	private static final Logger LOGGER = Logger.getLogger(IRBuilderVisitor.class.getName());
	private Stack<BasicBlock> bblStack; 
	private Stack<AbstractNode> expressionStack;
    private Stack<AbstractNode> functionCallStack;
    private Function currentFunction;
	private Program program;
	private Function mainFunction;

	public IRBuilderVisitor() {
		bblStack = new Stack<>();
		expressionStack = new Stack<>();
        functionCallStack = new Stack<>();
		program = new Program() ;
		mainFunction = new Function();
		mainFunction.name = "main";
		LOGGER.addHandler(new ConsoleHandler());
	}

	public Program getProgram(){
		return program;
	}

	@Override
	public void enter(ProgramNode node) {
		LOGGER.log( Level.FINE,"Entering program node");
	}

	@Override
	public void enter(VarNode node) {}

	@Override
	public void enter(TypeDeclNode node) {}

	@Override
	public void enter(NumberNode node) {}

	@Override
	public void enter(VarDeclNode node) {
		LOGGER.log( Level.FINER,"Entering program node");
		if ( node.children.get(0).children.get(0).getText().equals("var") ){ //Variable
			LOGGER.log( Level.FINER, node.children.get(0).getText());
			for( int i = 1 ;i < node.children.size() ; i+=2 ){
				LOGGER.log( Level.FINEST,"Adding "+ node.children.get(i).getText());
				if( node.parent instanceof FuncBodyNode ){
					currentFunction.localVariables.add( new Variable(node.children.get(i).getText(),VariableType.INTEGER) );
				}
				else {
					mainFunction.localVariables.add( new Variable(node.children.get(i).getText(),VariableType.INTEGER) );
				}
			}
		} else { // ARRAY
			ArrayList<Integer> dimensions = new ArrayList<>();
			TypeDeclNode tctx = (TypeDeclNode) node.getChild(0) ;
			for( int i = 1 ;i < tctx.children.size() ; ++i ){
				if( tctx.getChild(i).getText().equals("[") ){
					dimensions.add( Integer.parseInt(tctx.getChild(i+1).getText() ) );
				}
			}
			for( int i = 1 ;i < node.children.size() ; i+=2){
				if( node.parent instanceof FuncBodyNode ){
					currentFunction.localVariables.add( new Variable(node.children.get(i).getText(),VariableType.ARRAY, dimensions) );
				}
				else {
					mainFunction.localVariables.add( new Variable(node.children.get(i).getText(),VariableType.ARRAY, dimensions) );
				}
			}
		}
	}

	@Override
	public void enter(FuncDeclNode node) {
		LOGGER.log( Level.FINE,"Entering funcdecl node");
		currentFunction = new Function();
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
	public void enter(DesignatorNode node) {}

	@Override
	public void enter(ExpressionNode node) {}

	@Override
	public void enter(TermNode node) {}

	@Override
	public void enter(FactorNode node) {}

	@Override
	public void enter(RelationNode node) {}

	@Override
	public void enter(AssignmentNode node) {}

	@Override
	public void enter(WhileStmtNode node) {
		LOGGER.log( Level.FINE,"Entering whilestmt node");
		BasicBlock bblOld = bblStack.peek();

		BasicBlock bblLoopBody =  new BasicBlock(currentFunction , "Loop body");
		BasicBlock bblAfterWhile = new BasicBlock(currentFunction , "after while");

		bblAfterWhile.successors.addAll( bblOld.successors ) ;
		bblOld.successors.clear();

		if( bblOld.getNodes().size() == 0 ){
			bblOld.fallThrough = bblLoopBody;
			bblOld.taken = bblAfterWhile;
			bblOld.successors.add(bblLoopBody);
			bblOld.successors.add(bblAfterWhile);
			
			bblLoopBody.successors.add(bblOld);
            bblLoopBody.fallThrough = bblOld;

			BasicBlock top = bblStack.pop() ;
			bblStack.push( bblAfterWhile );
			bblStack.push( bblLoopBody );
			bblStack.push(top);
			
		}
		else {
			BasicBlock bblCheck = new BasicBlock(currentFunction , "while check");
			bblCheck.fallThrough = bblLoopBody;
			bblCheck.taken = bblAfterWhile;
			bblOld.successors.add(bblCheck);
			bblCheck.successors.add(bblLoopBody);
			bblCheck.successors.add(bblAfterWhile);
			bblLoopBody.successors.add(bblCheck);
			bblLoopBody.fallThrough = bblCheck;
			
			currentFunction.basicBlocks.add( bblStack.peek() );
			bblStack.pop();

			bblStack.push( bblAfterWhile );
			bblStack.push( bblLoopBody );
			bblStack.push( bblCheck );
		}
	}

	@Override
	public void enter(IfStmtNode node) {
		// Check if or if-else
		// Compute relation and add it to current basicblock
		boolean lvalue = false;
		if (node.parent instanceof AssignmentNode) {
			lvalue = true;
		}
		if (node.children.size() > 1) {
			// ARRAY
			ArrayList<Integer> strides = new ArrayList<>();
			expressionStack.push(new VarGetNode( node.getChild(0).getText()  ) ) ; // "load ARRAY l:" + lvalue + ctx.getText() ));
		} else {
			// Var
			if( lvalue ){
				/// TODO pop stack here?
				//expressionStack.push(new CopyNode( node.getChild(0).getText() ) );
			} else{
				expressionStack.push(new VarGetNode( node.getChild(0).getText() ) ) ; // "load Var l:" + lvalue +  ctx.getText() ) );
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

			bblOld.fallThrough = bblThen;
			bblOld.taken = bblAfterIf;

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

			bblOld.fallThrough = bblThen;
			bblOld.taken = bblElse;

			bblStack.push(bblOld);
			// Fall-through node => else
		}
		//currentFunction.basicBlocks.add( bblOld );
	}

	@Override
	public void enter(ReturnStmtNode node) {}

	@Override
	public void enter(FuncBodyNode node) {
		BasicBlock bbl =  new BasicBlock(currentFunction ,"func body") ;
		bblStack.push(bbl);
	}

	@Override
	public void enter(FormalParamNode node) {}

	@Override
	public void enter(StatementNode node) {}

	@Override
	public void exit(ProgramNode node) {}

	@Override
	public void exit(VarNode node) {}

	@Override
	public void exit(TypeDeclNode node) {}

	@Override
	public void exit(NumberNode node) {}

	@Override
	public void exit(VarDeclNode node) {}

	@Override
	public void exit(FuncDeclNode node) {
		program.addFunction(currentFunction);
	}

	@Override
	public void exit(StatSeqNode node) {
		if (node.parent instanceof WhileStmtNode) {
			bblStack.peek().addNode(new BranchNode());
		}
		if (node.parent instanceof ProgramNode) {
			bblStack.peek().addNode(new ReturnNode(null));
		}
		currentFunction.basicBlocks.add(bblStack.peek());
		bblStack.pop();
		if (node.parent instanceof ProgramNode) {
			program.addFunction(currentFunction);
		}
	}

	@Override
	public void exit(DesignatorNode node) throws Exception {

		// Whether it is being used in left side of the assignment
	    boolean lvalue = false;
		if (node.parent instanceof AssignmentNode)
			lvalue = true;

		String varName = node.getChild(0).getText() ;

        // params, local vars, global vars
        Variable var = currentFunction.parameters.getVar(varName);
        if( var == null )
            var = currentFunction.localVariables.getVar(varName);
		if( var == null )
			var = mainFunction.localVariables.getVar(varName);
		assert var != null;

        // Array of Integers
		if (node.children.size() > 1) {
			// Need to calculate the access jumpAddress first
			int index = 0;
			AbstractNode offsetCalcNode = null;
			AbstractNode tempOffsetCalcNode = null;
			
			for (ParseTreeNode child : node.children) {
				if (child instanceof ExpressionNode) {
					// Add expression
					bblStack.peek().addNode( expressionStack.peek() );
					// Manipulate it further
					int offset = 1 ;
					for( int i = 0 ; i < index ; ++i ){
						int innd = var.getDimensions().size() - 1 - i  ;
						int here = 0 ;
						if( innd >= var.getDimensions().size() ){
							 here = 1 ;
						}
						int there = here + 1 ;
						offset *= var.getDimensions().get(innd);
					}
					tempOffsetCalcNode = offsetCalcNode ;
					if( offset > 1){
						offsetCalcNode = new ArithmeticNode(expressionStack.peek(), new ImmediateNode(Integer.toString(offset)), ArithmeticNode.Type.MUL) ;
						bblStack.peek().addNode( offsetCalcNode );
					}else {
						offsetCalcNode =  expressionStack.peek();
					}
					expressionStack.pop();
					
					if( tempOffsetCalcNode != null )
					{
						offsetCalcNode = new ArithmeticNode(tempOffsetCalcNode, offsetCalcNode , ArithmeticNode.Type.ADD) ;
						bblStack.peek().addNode( offsetCalcNode );
					}
					++index ;
				}
			}
			AbstractNode calcAddress = new AddressCalcNode(varName, offsetCalcNode) ;

			bblStack.peek().addNode(calcAddress);
			if (lvalue) {
				expressionStack.push(new MemoryStoreNode(calcAddress));
			} else{
				expressionStack.push(new MemoryLoadNode(calcAddress)) ;
			}
		} else {
			// Integer variable
            // It is not clear if this is being allocated on the stack or a register
            // For now we emit regular load/store nodes
			if (lvalue)
				expressionStack.push(new VarSetNode(varName));
			else
				expressionStack.push(new VarGetNode(varName));
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
            if(!(expressionStack.peek() instanceof BranchNode) &&
                    !(expressionStack.peek() instanceof FunctionCallNode))
                bblStack.peek().addNode( expressionStack.peek() );

            AbstractNode label1 = expressionStack.pop() ;

            if(!(expressionStack.peek() instanceof BranchNode) &&
                    !(expressionStack.peek() instanceof FunctionCallNode))
                bblStack.peek().addNode( expressionStack.peek() );

            AbstractNode label2 = expressionStack.pop() ;

            String operatorText = operator.getText() ;
            // Coming from stack, reverse order
            AbstractNode anode = new ArithmeticNode(label2, label1,  ArithmeticNode.operatorMap.get(operatorText) );
            expressionStack.push(anode);

            li.previous();
            while( li.hasPrevious() ){
                operator = li.previous();
                li.previous();
                if(!(expressionStack.peek() instanceof BranchNode))
                    bblStack.peek().addNode( expressionStack.peek() );
                AbstractNode label = expressionStack.pop();
                String operatorTexts = operator.getText() ;

                if(!(expressionStack.peek() instanceof BranchNode))
                    bblStack.peek().addNode( expressionStack.peek() );
                AbstractNode op1Label = expressionStack.pop();

                anode = new ArithmeticNode( op1Label , label ,  ArithmeticNode.operatorMap.get(operatorTexts) );
                expressionStack.push(anode);
            }
        }
	}

	@Override
	public void exit(TermNode node) {
		if (node.children.size() > 2) {  // Process two or more factors
			ListIterator<ParseTreeNode> li =  node.children.listIterator(node.children.size());
			li.previous(); //term already on stack
			ParseTreeNode operator = li.previous();

			if (!(expressionStack.peek() instanceof BranchNode) &&
                    !(expressionStack.peek() instanceof FunctionCallNode))
				bblStack.peek().addNode(expressionStack.peek());
			AbstractNode label1 = expressionStack.pop() ;
			
			if (!(expressionStack.peek() instanceof BranchNode) &&
                    !(expressionStack.peek() instanceof FunctionCallNode))
				bblStack.peek().addNode(expressionStack.peek());
			AbstractNode label2 = expressionStack.pop();
			
			AbstractNode anode = new ArithmeticNode(label2, label1, ArithmeticNode.operatorMap.get(operator.getText()));
			expressionStack.push(anode);
			li.previous();

			while (li.hasPrevious()) {
				operator =li.previous();
				li.previous();

				if (!(expressionStack.peek() instanceof BranchNode))
					bblStack.peek().addNode(expressionStack.peek());
				AbstractNode label = expressionStack.pop() ;
				
				if (!(expressionStack.peek() instanceof BranchNode))
					bblStack.peek().addNode( expressionStack.peek());
				AbstractNode op1Label = expressionStack.pop();

				anode = new ArithmeticNode(op1Label, label, ArithmeticNode.operatorMap.get(operator.getText()));
				expressionStack.push(anode);
			}
		}
	}

	@Override
	public void exit(FactorNode node) {
		if (node.children.size() == 1 && node.children.get(0) instanceof TerminalNode)
			expressionStack.push( new ImmediateNode(node.children.get(0).getText()));
	}

	@Override
	public void exit(RelationNode node) {
		bblStack.peek().addNode( expressionStack.peek() );
		AbstractNode label1 = expressionStack.pop() ;
		
		bblStack.peek().addNode( expressionStack.peek() );
		AbstractNode label2 = expressionStack.pop() ;

		AbstractNode nNode = new ArithmeticNode(label2, label1,  ArithmeticNode.Type.CMP);
		bblStack.peek().addNode( nNode );
		bblStack.peek().addNode( new BranchNode( BranchNode.branchMapReversed.get(node.children.get(1).getText() ), nNode)  );

        currentFunction.basicBlocks.add( bblStack.peek() );
		bblStack.pop();
	}

	@Override
	public void exit(AssignmentNode node) {
		AbstractNode exp = expressionStack.pop();
		AbstractNode des = expressionStack.pop();

		assert des instanceof VarSetNode || des instanceof MemoryStoreNode;

        // Function call has been added to the previous block
		if (!(exp instanceof FunctionCallNode))
		    bblStack.peek().addNode(exp);

		// Function is used in the right side
        // It's return values is used
		if(exp instanceof  FunctionCallNode)
            ((FunctionCallNode)exp).returnsStuff = true;

        if (des instanceof VarSetNode)
		    ((VarSetNode)des).setSrcOperand(exp);
        else
            ((MemoryStoreNode)des).setSrcOperand(exp);

		bblStack.peek().addNode(des) ;
	}

	@Override
	public void exit(WhileStmtNode node) {}

	@Override
	public void exit(IfStmtNode node) {}

	@Override
	public void exit(ReturnStmtNode node) {
		//Pop ExpStack
		if( node.children.size() > 1 ) {
			LOGGER.log(Level.FINE,"poping ret" );
			
			AbstractNode exp = expressionStack.pop() ;

            // Function call has been added to the previous block
            if (!(exp instanceof FunctionCallNode))
			    bblStack.peek().addNode(exp) ;
            else {
                ((FunctionCallNode)functionCallStack.peek()).returnsStuff = true;
                functionCallStack.pop();
            }

			ReturnNode retNode = new ReturnNode(exp);
			bblStack.peek().addNode(retNode);

		} else {
			LOGGER.log( Level.FINE,"Not poping ret" );
		}

	}

	@Override
	public void exit(FuncBodyNode node) {}

	@Override
	public void exit(FormalParamNode node) {
		if( node.children.size() > 2 )
			for( int i = 1 ; i < node.children.size(); i+=2)
				currentFunction.parameters.add( new Variable( node.children.get(i).getText() , VariableType.INTEGER));
	}

	@Override
	public void exit(StatementNode node) {}

	@Override
	public void enter(FuncCallNode node) {}

	@Override
	public void exit(FuncCallNode node) {
		LOGGER.log(Level.FINE,"Exiting Func Call node");
        AbstractNode callNode  = null;

        String functionName = node.getChild(1).getText() ;
        boolean specialCall = functionName.equals("InputNum") || functionName.equals("OutputNum") || functionName.equals("OutputNewLine");

        if (!specialCall) {
            callNode = new FunctionCallNode(functionName);

        } else {
            callNode = new IONode();
        }

        // Handle Args( as Expressions on the Stack )
		// Pop per each argument
		for(ParseTreeNode child: node.children ){
			if (child instanceof ExpressionNode) {
				AbstractNode aanode =  expressionStack.pop();

                // Function call has been added to the previous block
                if (!(aanode instanceof FunctionCallNode))
                    bblStack.peek().addNode(aanode);
                else {
                    ((FunctionCallNode) functionCallStack.peek()).returnsStuff = true;
                    functionCallStack.pop();
                }

                // Adds operands for function calls
                // Write takes one parameter which will be handled later
				callNode.addOperand(aanode);
			}
		}

		if (!specialCall) {
			// Split the Current Basic Block
			BasicBlock bblAfterCall = new BasicBlock(currentFunction ,"After Call");
			bblAfterCall.successors.addAll(bblStack.peek().successors);
			bblStack.peek().successors.clear();
			bblStack.peek().successors.add(bblAfterCall);
			BasicBlock bblOld = bblStack.pop();
			bblStack.push(bblAfterCall);
			bblStack.push(bblOld);

			bblStack.peek().addNode(callNode);
			currentFunction.basicBlocks.add( bblStack.peek() );
			bblStack.pop();
		}
		else {
			// Special Instructions
			if( functionName.equals("InputNum") ){
                ((IONode)callNode).setParams(IOType.READ, null);
			} else if ( functionName.equals("OutputNum") ){
                if (bblStack.peek().getNodes().size() > 0)
                    ((IONode)callNode).setParams(IOType.WRITE, bblStack.peek().getLastNode());
                // Function call has been added to the previous block
                // This is the first op in new block with no other ops
                else
                    ((IONode)callNode).setParams(IOType.WRITE, functionCallStack.peek());

                bblStack.peek().addNode(callNode);
			} else if ( functionName.equals("OutputNewLine") ){
                ((IONode)callNode).setParams(IOType.WRITELINE, null);
                bblStack.peek().addNode(callNode);
			}
		}
		functionCallStack.push(callNode);

		if (node.parent instanceof FactorNode) {
			expressionStack.push(callNode);
		}
	}

}
