package org.pl241.optimization;

import java.util.HashMap;
import org.pl241.ir.Function;
import org.pl241.ir.*;


//
// CSE deals with Arith, store nodes
//

public class CSE {
	public CSE(){
		expressionMap = new HashMap<>();
		matchMap = new HashMap<>();
	}

	public void apply(Function function){
		fixBlock(function.getEntryBlock());
	}

	public void fixBlock(BasicBlock block){
		// Foreach phi function
/*
		// Foreach Statement
		for (AbstractNode node: block.getNodes()) {

		    if (node instanceof StoreNode) { // Handle copies

            }
			else if(node instanceof ArithmeticNode) {
				// look for operands
				AbstractNode node1 = node.getOperandAtIndex(0) ;
				AbstractNode node2 = node.getOperandAtIndex(1) ;

				String operand1 = null;
				String operand2 = null; 
				
				if (node1 instanceof LoadNode) {
					operand1 = ((LoadNode)node1).variableId;
				} else if ( node1 instanceof ArithmeticNode &&
                        (node1.operator.equals("ADD") || node1.operator.equals("MUL")) ){ // TODO
					// find it in the matchmap
					if( matchMap.containsKey(node1.nodeId)){
						operand1 = matchMap.get(node1.nodeId);
					}else {
						operand1 = node1.nodeId ;
					}
				} else{
					
				}
				
				if( node2 instanceof LoadNode ){
					operand2 = ((LoadNode)node2).variableName; //TODO
				} else if ( node2.operator.equals("ADD") || node2.operator.equals("MUL") ){ //TODO
					if( matchMap.containsKey(node2.nodeId)){
						operand2 = matchMap.get(node2.nodeId);
					} else {
						operand2 = node2.nodeId ;
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
						matchMap.put(node.nodeId, expressionMap.get(exp) );
						System.out.println("1 mm putting " + node.nodeId + ":" + expressionMap.get(exp) );
						//node.nodeId = expressionMap.get(exp);
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
						expressionMap.put(exp,node.nodeId);
						System.out.println("em putting " + exp.operator +"," + exp.op1 +"," + exp.op2 + ":" +  node.nodeId );
						//matchMap.put(node.nodeId, node.nodeId );
					}
				}
			} else if (node instanceof MoveNode) {
				MoveNode mNode = (MoveNode)node;
				String nodeLable = mNode.getInputOperands().get(0);
				//System.out.println("2 mm checking " + 	nodeLable);
				while( matchMap.containsKey(nodeLable) ){
					//System.out.println("found " + 	nodeLable + " " + matchMap.get(nodeLable));
					nodeLable = matchMap.get(nodeLable);	
					//System.out.println("2 mm checking " + 	nodeLable);
				}
				mNode.setRightOperand(nodeLable) ;
				matchMap.put( mNode.variableName, nodeLable  );
				System.out.println("2 mm putting " + 	mNode.variableName + ":" +  nodeLable );
			}
		}

		// Foreach successor
		for (BasicBlock nextBlock: block.getSuccessors()) {
			// handle phi functions
		}
		
		// Foreach child in DFT
		for (BasicBlock iBlock: block.immediateDominants) {
			fixBlock(iBlock);
		}
		
		Iterator<AbstractNode> i = block.getNodes().iterator() ;
		while (i.hasNext()) {
			AbstractNode node = i.next();
			if (node.removed)
			 	i.remove();
		}
*/
	}
	private HashMap<Expression, String> expressionMap;
	private HashMap<String, String> matchMap ;

}
