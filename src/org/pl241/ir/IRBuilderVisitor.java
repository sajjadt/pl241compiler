package org.pl241.ir;

import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;


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
import org.pl241.ir.AtomicFunctionNode.IOType;
import org.pl241.ir.Variable.VariableType;

public class IRBuilderVisitor implements ParseTreeNodeVisitor {

	private static final Logger LOGGER = Logger.getLogger(IRBuilderVisitor.class.getName());
	private Stack<BasicBlock> bblStack; 
	private Stack<NodeContainer> expressionStack;
    private Stack<NodeContainer> functionCallStack;
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

		BasicBlock bblLoopBody =  new BasicBlock(currentFunction, "Loop body");
		BasicBlock bblAfterWhile = new BasicBlock(currentFunction, "after while");

		bblAfterWhile.successors.addAll(bblOld.successors);
		bblOld.successors.clear();

		if (bblOld.getNodes().size() == 0) {
		    bblOld.loopHeader = true;
			bblOld.fallThrough = bblLoopBody;
			bblOld.taken = bblAfterWhile;
			bblOld.successors.add(bblLoopBody);
			bblOld.successors.add(bblAfterWhile);
			
			bblLoopBody.successors.add(bblOld);
            bblLoopBody.fallThrough = bblOld;

			BasicBlock top = bblStack.pop();
			bblStack.push(bblAfterWhile);
			bblStack.push(bblLoopBody);
			bblStack.push(top);
			
		} else {
			BasicBlock bblCheck = new BasicBlock(currentFunction, "while check");
			bblCheck.loopHeader = true;
			bblCheck.fallThrough = bblLoopBody;
			bblCheck.taken = bblAfterWhile;
			bblOld.successors.add(bblCheck);
			bblCheck.successors.add(bblLoopBody);
			bblCheck.successors.add(bblAfterWhile);
			bblLoopBody.successors.add(bblCheck);
			bblLoopBody.fallThrough = bblCheck;
			
			currentFunction.basicBlocks.add(bblStack.peek());
			bblStack.pop();

			bblStack.push(bblAfterWhile);
			bblStack.push(bblLoopBody);
			bblStack.push(bblCheck);
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
			expressionStack.push(new NodeContainer(new VarGetNode( node.getChild(0).getText()))); // "load ARRAY l:" + lvalue + ctx.getText() ));
		} else {
			// Var
			if(!lvalue)
				expressionStack.push(new NodeContainer(new VarGetNode( node.getChild(0).getText()))); // "load Var l:" + lvalue +  ctx.getText() ) );
		}
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
			bblStack.peek().addNode(new NodeContainer(new BranchNode()));
		}
		if (node.parent instanceof ProgramNode) {
			bblStack.peek().addNode(new NodeContainer(new ReturnNode(null)));
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

		String varName = node.getChild(0).getText();

        // Fetch local/global vars dimension info
        Variable var = currentFunction.localVariables.getVar(varName);
		if( var == null )
			var = mainFunction.localVariables.getVar(varName);

        // Array of Integers
		if (node.children.size() > 1) {

            assert var != null: "Array variable " + varName + " not found";

            // Need to calculate the access jumpAddress first
			int index = 0;
			NodeContainer offsetCalcNode = null;
			NodeContainer tempOffsetCalcNode = null;
			
			for (ParseTreeNode child : node.children) {
				if (child instanceof ExpressionNode) {
					// Add expression
					bblStack.peek().addNode(expressionStack.peek());
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
						offsetCalcNode = new NodeContainer(new ArithmeticNode(expressionStack.peek(), new NodeContainer(new ImmediateNode(Integer.toString(offset))), ArithmeticNode.Type.MUL));
						bblStack.peek().addNode( offsetCalcNode );
					}else {
						offsetCalcNode = expressionStack.peek();
					}
					expressionStack.pop();
					
					if( tempOffsetCalcNode != null )
					{
						offsetCalcNode = new NodeContainer(new ArithmeticNode(tempOffsetCalcNode, offsetCalcNode , ArithmeticNode.Type.ADD));
						bblStack.peek().addNode(offsetCalcNode);
					}
					++index ;
				}
			}

            NodeContainer offsetMulNode = new NodeContainer(new ArithmeticNode(offsetCalcNode, new NodeContainer(new ImmediateNode("4")), ArithmeticNode.Type.MUL));
			NodeContainer fetchBase = new NodeContainer(new VarGetNode(varName));
			NodeContainer calcAddress = new NodeContainer(new ArithmeticNode(fetchBase, offsetMulNode, ArithmeticNode.Type.ADDA));

			bblStack.peek().addNode(offsetMulNode);
            bblStack.peek().addNode(fetchBase);
			bblStack.peek().addNode(calcAddress);
			if (lvalue) {
                NodeContainer n = new NodeContainer(new MemoryStoreNode(calcAddress, null));
                // This node should be inserted after right hand side
                //bblStack.peek().addNode(n);
                expressionStack.push(n);
			} else{
			    NodeContainer n = new NodeContainer(new MemoryLoadNode(calcAddress));
			    bblStack.peek().addNode(n);
				expressionStack.push(n) ;
			}
		} else {
			// Integer variable
            // It is not clear if this is being allocated on the stack or a register
            // For now we emit regular load/store nodes
            if (lvalue)
				expressionStack.push(new NodeContainer(new VarSetNode(varName)));
			else
				expressionStack.push(new NodeContainer(new VarGetNode(varName)));
		}
	}

    @Override
    public void exit(ExpressionNode node) {
        List<NodeContainer> nodesList = new ArrayList<>();
        List<String> operatorsList = new ArrayList<>();

        if (node.children.size() > 2) {
            ListIterator<ParseTreeNode> li = node.children.listIterator(node.children.size());
            nodesList.add(expressionStack.pop());
            li.previous();
            while (li.hasPrevious()) {
                ParseTreeNode opNode = li.previous();
                assert opNode instanceof TerminalNode;
                operatorsList.add(opNode.getText());

                nodesList.add(expressionStack.pop());
                li.previous();
            }

            Collections.reverse(nodesList);
            Collections.reverse(operatorsList);
            assert nodesList.size() == operatorsList.size() + 1;

            for (int i=0; i < operatorsList.size(); i++) {
                NodeContainer op1;
                NodeContainer op2 = nodesList.get(i + 1);

                if (i == 0) {
                    op1 = nodesList.get(i);
                    // They are added to the previous block
                    if (!op1.isControlFlow())
                        bblStack.peek().addNode(op1);
                    if (!op2.isControlFlow())
                        bblStack.peek().addNode(op2);
                } else {
                    op1 = expressionStack.pop();
                    if (!op2.isControlFlow())
                        bblStack.peek().addNode(op2);
                }

                NodeContainer anode = new NodeContainer(new ArithmeticNode(op1, op2, ArithmeticNode.operatorMap.get(operatorsList.get(i))));
                bblStack.peek().addNode(anode);
                // push the final into exp stack
                expressionStack.push(anode);
            }
        }
    }

    @Override
    public void exit(TermNode node) {
        List<NodeContainer> nodesList = new ArrayList<>();
        List<String> operatorsList = new ArrayList<>();

        if (node.children.size() > 2) {
            ListIterator<ParseTreeNode> li = node.children.listIterator(node.children.size());
            nodesList.add(expressionStack.pop());
            li.previous();
            while (li.hasPrevious()) {
                ParseTreeNode opNode = li.previous();
                assert opNode instanceof TerminalNode;
                operatorsList.add(opNode.getText());

                nodesList.add(expressionStack.pop());
                li.previous();
            }

            Collections.reverse(nodesList);
            Collections.reverse(operatorsList);
            assert nodesList.size() == operatorsList.size() + 1;

            for (int i=0; i < operatorsList.size(); i++) {
                NodeContainer op1;
                NodeContainer op2 = nodesList.get(i+1);

                if (i == 0) {
                    op1 = nodesList.get(i);

                    if (!op1.isControlFlow()) // They are added to the previous block
                        bblStack.peek().addNode(op1);
                    if (!op2.isControlFlow())
                        bblStack.peek().addNode(op2);
                }
                else {
                    op1 = expressionStack.pop();
                    if (!op2.isControlFlow())
                        bblStack.peek().addNode(op2);
                }

                NodeContainer anode = new NodeContainer(new ArithmeticNode(op1, op2, ArithmeticNode.operatorMap.get(operatorsList.get(i))));
                bblStack.peek().addNode(anode);
                // push the final into exp stack
                expressionStack.push(anode);
            }
        }
    }


	/*
	@Override
	public void exit(ExpressionNode node) {
		// Push an expression to expression stack
		// Parent should pop them and insert proper ir code

        List<AbstractNode> tempList = new ArrayList<>();

        if (node.children.size() > 2) {
            ListIterator<ParseTreeNode> li = node.children.listIterator(node.children.size());
            li.previous();
            ParseTreeNode operator = li.previous();
            if(!(expressionStack.peek() instanceof BranchNode) &&
                    !(expressionStack.peek() instanceof FunctionCallNode))
                bblStack.peek().addNode(expressionStack.peek());

            AbstractNode label1 = expressionStack.pop();

            if(!(expressionStack.peek() instanceof BranchNode) &&
                    !(expressionStack.peek() instanceof FunctionCallNode))
                bblStack.peek().addNode(expressionStack.peek());

            AbstractNode label2 = expressionStack.pop() ;

            String operatorText = operator.getText() ;
            // Coming from stack, reverse order


            tempList.add(label1);
            tempList.add(label2);

            //AbstractNode anode = new ArithmeticNode(label2, label1, ArithmeticNode.operatorMap.get(operatorText));
            //expressionStack.push(anode);

            li.previous();
            while (li.hasPrevious()) {
                operator = li.previous();
                li.previous();
                if(!(expressionStack.peek() instanceof BranchNode))
                    bblStack.peek().addNode( expressionStack.peek() );
                AbstractNode label = expressionStack.pop();
                //String operatorTexts = operator.getText() ;

                if(!(expressionStack.peek() instanceof BranchNode))
                    bblStack.peek().addNode( expressionStack.peek() );
                AbstractNode op1Label = expressionStack.pop();

                anode = new ArithmeticNode( op1Label , label ,  ArithmeticNode.operatorMap.get(operatorTexts) );
                expressionStack.push(anode);
            }
        }
	}*/

	/*
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
	*/

	@Override
	public void exit(FactorNode node) {
		if (node.children.size() == 1 && node.children.get(0) instanceof TerminalNode)
			expressionStack.push(new NodeContainer(new ImmediateNode(node.children.get(0).getText())));
	}

	@Override
	public void exit(RelationNode node) {
		bblStack.peek().addNode(expressionStack.peek());
		NodeContainer label1 = expressionStack.pop();
		
		bblStack.peek().addNode(expressionStack.peek());
        NodeContainer label2 = expressionStack.pop();

        bblStack.peek().addNode(label1);
        bblStack.peek().addNode(label2);


		NodeContainer nNode = new NodeContainer(new ArithmeticNode(label2, label1, ArithmeticNode.Type.CMP));
		bblStack.peek().addNode(nNode);
		bblStack.peek().addNode(new NodeContainer(new BranchNode(BranchNode.branchMapReversed.get(node.children.get(1).getText()), nNode)));

        currentFunction.basicBlocks.add(bblStack.peek());
		bblStack.pop();
	}

	@Override
	public void exit(AssignmentNode node) {
		NodeContainer exp = expressionStack.pop();
        NodeContainer des = expressionStack.pop();

		assert des.node instanceof VarSetNode || des.node instanceof MemoryStoreNode;

		// Function is used in the right side
        // It's return values is used
		if(exp.node instanceof  FunctionCallNode)
            ((FunctionCallNode)exp.node).hasReturnValue = true;

        if (des.node instanceof VarSetNode)
		    ((VarSetNode)des.node).setSrcOperand(exp);
        else
            ((MemoryStoreNode)des.node).setValueNode(exp);

        // They have been added to the previous block
        if (!exp.isControlFlow())
            bblStack.peek().addNode(exp);
		bblStack.peek().addNode(des);
	}

	@Override
	public void exit(WhileStmtNode node) {}

	@Override
	public void exit(IfStmtNode node) {}

	@Override
	public void exit(ReturnStmtNode node) {
		//Pop ExpStack
		if (node.children.size() > 1) {
			NodeContainer exp = expressionStack.pop();

            // Function call has been added to the previous block
            if (!(exp.node instanceof FunctionCallNode))
			    bblStack.peek().addNode(exp) ;
            else {
               // ((FunctionCallNode)functionCallStack.peek()).hasReturnValue = true;
                functionCallStack.pop();
            }

			ReturnNode retNode = new ReturnNode(exp);
			bblStack.peek().addNode(new NodeContainer(retNode));

		}
	}

	@Override
	public void exit(FuncBodyNode node) {}

	@Override
	public void exit(FormalParamNode node) {
		if( node.children.size() > 2 )
			for( int i = 1 ; i < node.children.size(); i+=2)
				currentFunction.parameters.put(node.children.get(i).getText(), i/2);
	}

	@Override
	public void exit(StatementNode node) {}

	@Override
	public void enter(FuncCallNode node) {}

	@Override
	public void exit(FuncCallNode node) {
        AbstractNode callNode  = null;
        NodeContainer callContainer = null;

        String functionName = node.getChild(1).getText() ;
        boolean atomicCall = functionName.equals("InputNum") || functionName.equals("OutputNum") || functionName.equals("OutputNewLine");

        if (atomicCall)
            callNode = new AtomicFunctionNode();
        else {
            callNode = new FunctionCallNode(functionName);
            if (node.parent instanceof StatementNode)
                ((FunctionCallNode)callNode).hasReturnValue = false;
        }

        if (atomicCall) {
            if( functionName.equals("InputNum") ){
                ((AtomicFunctionNode)callNode).setParams(IOType.READ, null);
            } else if ( functionName.equals("OutputNum") ){
            } else if ( functionName.equals("OutputNewLine") ){
                ((AtomicFunctionNode)callNode).setParams(IOType.WRITELINE, null);
            }
        }

        // Handle Args( as Expressions on the Stack )
		// Pop per each argument
		for(ParseTreeNode child: node.children ){
			if (child instanceof ExpressionNode) {
				NodeContainer aanode =  expressionStack.pop();

				// They are added in exit(expression)
				if (!(aanode.node instanceof ArithmeticNode) &&
						!(aanode.node instanceof FunctionCallNode))
                    bblStack.peek().addNode(aanode);

                // Function call has been added to the previous block
                if ((aanode.node instanceof FunctionCallNode))
                    functionCallStack.pop();

                // Adds operands for function calls
                // Write takes one parameter which will be handled later
				if (callNode instanceof AtomicFunctionNode) {
                    ((AtomicFunctionNode)callNode).setParams(IOType.WRITE, aanode);
                }
                else
                    callNode.addOperand(aanode);
			}
		}

		callContainer = new NodeContainer(callNode);

		if (!atomicCall) {
			// Split the Current Basic Block
			BasicBlock bblAfterCall = new BasicBlock(currentFunction ,"After Call");
			bblAfterCall.successors.addAll(bblStack.peek().successors);
			bblStack.peek().successors.clear();
			bblStack.peek().successors.add(bblAfterCall);
			BasicBlock bblOld = bblStack.pop();
			bblStack.push(bblAfterCall);
			bblStack.push(bblOld);

			bblStack.peek().addNode(callContainer);
			currentFunction.basicBlocks.add(bblStack.peek());
			bblStack.pop();
		}
		else {
            bblStack.peek().addNode(callContainer);
		}

		functionCallStack.push(callContainer);
		if (node.parent instanceof FactorNode) {
			expressionStack.push(callContainer);
		}
	}

}
