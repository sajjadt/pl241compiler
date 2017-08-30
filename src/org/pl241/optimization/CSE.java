package org.pl241.optimization;

import java.util.HashMap;

import org.pl241.Function;

public class CSE {
	private HashMap<Expression,String> expressionMap;
	private HashMap<String,String> matchMap ;
	public CSE(){
		expressionMap = new HashMap<Expression,String>();
		matchMap = new HashMap<String, String>();
	}
	public void apply(Function function){
		//fixBlock( function.getEntryBlock() );
	}
/*
	public void fixBlock(BasicBlock block){
		// Foreach phi function
		
		
		// Foreach Statement
		for( AbstractNode node: block.getNodes() ){
			// TODO NEW 	CAREFULL HERE ADD,MUL BUT LOAD?
			if(node instanceof ArithmaticNode && ( ((ArithmaticNode)node).operator.equals("ADD") || ((ArithmaticNode)node).operator.equals("MUL")) ){ ///TODO add more div, ...
				// look for operands
			
				AbstractNode node1Label = node.getOperandAtIndex(0) ;
				AbstractNode node2Label = node.getOperandAtIndex(1) ;
								
				node.setOperands(node1Label,node2Label);
				//node.operand1Label = node1Label ;
				//node.operand2Label = node2Label ;
				
				AbstractNode node1 = block.parentFunction.irMap.get(node1Label);
				AbstractNode node2 = block.parentFunction.irMap.get(node2Label);
				
				String operand1 = null;
				String operand2 = null; 
				
				if( node1 instanceof LoadNode ){
					operand1 = ((LoadNode)node1).memAddress; // TODO with stride ?
				} else if ( node1.operator.equals("ADD") || node1.operator.equals("MUL") ){ // TODO 
					// find it in the matchmap
					if( matchMap.containsKey(node1.uniqueLabel)){
						operand1 = matchMap.get(node1.uniqueLabel);
					}else {
						operand1 = node1.uniqueLabel ;
					}
				} else{
					
				}
				
				if( node2 instanceof LoadNode ){
					operand2 = ((LoadNode)node2).memAddress; //TODO 
				} else if ( node2.operator.equals("ADD") || node2.operator.equals("MUL") ){ //TODO
					if( matchMap.containsKey(node2.uniqueLabel)){
						operand2 = matchMap.get(node2.uniqueLabel);
					} else {
						operand2 = node2.uniqueLabel ;
					}
				} else{
					
				}
				while( matchMap.containsKey(operand1) ){
					operand1 = matchMap.get(operand1);	
				}
				
				while( matchMap.containsKey(operand2) ){
					operand2 = matchMap.get(operand2);
				}
				if( operand1 != null && operand2 != null ){
					Expression exp = new Expression(operand1,operand2, node.operator);
					if( expressionMap.containsKey(exp) ){
						matchMap.put(node.uniqueLabel, expressionMap.get(exp) );
						System.out.println("1 mm putting " + node.uniqueLabel + ":" + expressionMap.get(exp) );
						//node.uniqueLabel = expressionMap.get(exp);
						node.removed = true ; 
						node.removeReason = "CSE" ;
						
						node1.removed = true ; 
						node1.removeReason = "CSE" ;
						
						node2.removed = true ; 
						node2.removeReason = "CSE" ;
						
					} else {
						node.setOperands(operand1, operand2);
						//node.operand1Label = operand1 ;
						//node.operand2Label = operand2 ;
						expressionMap.put(exp,node.uniqueLabel);
						System.out.println("em putting " + exp.operator +"," + exp.op1 +"," + exp.op2 + ":" +  node.uniqueLabel );
						//matchMap.put(node.uniqueLabel, node.uniqueLabel );
					}
				}
				
			}else if (node instanceof MoveNode) {
				
				MoveNode mNode = (MoveNode)node;
				String nodeLable = mNode.getInputOperands().get(0);
				//System.out.println("2 mm checking " + 	nodeLable);
				while( matchMap.containsKey(nodeLable) ){
					//System.out.println("found " + 	nodeLable + " " + matchMap.get(nodeLable));
					nodeLable = matchMap.get(nodeLable);	
					//System.out.println("2 mm checking " + 	nodeLable);
				}
				mNode.setRightOperand(nodeLable) ;
				matchMap.put( mNode.memAddress, nodeLable  );
				System.out.println("2 mm putting " + 	mNode.memAddress + ":" +  nodeLable );
			}
		}
		
		
		// Foreach successor
		for( BasicBlock nextBlock: block.getSuccessors() )
		{
			// handle phi functions
		}
		
		// Foreach child in DFT
		for( BasicBlock iBlock: block.immediateDominants ){
			fixBlock(iBlock);
		}
		
		Iterator<AbstractNode> i = block.getNodes().iterator() ;
		while(i.hasNext()){
			AbstractNode node = i.next();
			if( node.removed )
				i.remove();
		}
	}
*/
}
